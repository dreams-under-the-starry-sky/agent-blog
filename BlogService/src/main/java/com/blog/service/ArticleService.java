package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.common.MarkdownExcerpt;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.ArticleSaveRequest;
import com.blog.dto.ImageSaveItem;
import com.blog.entity.Article;
import com.blog.entity.ArticleContent;
import com.blog.entity.ArticleImg;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleContentMapper;
import com.blog.mapper.ArticleImgMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ArticleService {
    /** 与 blog_article.description varchar(80) 一致，不要 ALTER。 */
    static final int DESCRIPTION_MAX = 80;

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleContentMapper contentMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private ArticleImgMapper articleImgMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private LogService logService;
    @Resource
    private MiscService miscService;

    private static final Pattern MD_IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\(([^)\\s]+)");

    public PageResult<Article> page(PageQuery query) {
        List<Article> list = articleMapper.selectPage(query);
        return new PageResult<>(articleMapper.countPage(query), list);
    }

    public Article detail(Long id, boolean increasePv) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BizException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        fillTags(article);
        article.setImages(articleImgMapper.selectByArticleId(id));
        if (increasePv && Integer.valueOf(1).equals(article.getStatus())) {
            articleMapper.incrementPv(id);
            article.setPv((article.getPv() == null ? 0 : article.getPv()) + 1);
        }
        return article;
    }

    @Transactional
    public Long save(ArticleSaveRequest req) {
        boolean creating = req.getId() == null;
        Long oldCategory = null;
        Article old = null;
        if (!creating) {
            old = articleMapper.selectById(req.getId());
            if (old == null) {
                throw new BizException(ErrorCode.ARTICLE_NOT_FOUND);
            }
            oldCategory = old.getCategoryId();
        }
        Article article = new Article();
        article.setId(creating ? IdGenerator.nextId() : req.getId());
        article.setCategoryId(req.getCategoryId());
        article.setTitle(req.getTitle());
        article.setDescription(resolveDescription(req.getDescription(), req.getContent()));
        article.setCover(trimUrl(req.getCover()));
        article.setThumbnail(trimUrl(req.getThumbnail() != null ? req.getThumbnail() : req.getCover()));
        article.setComment(req.getComment() == null ? 1 : req.getComment());
        article.setStatus(req.getStatus() == null ? 0 : req.getStatus());
        article.setRecommend(req.getRecommend() == null ? 0 : req.getRecommend());
        LocalDateTime now = LocalDateTime.now();
        article.setYearTime(now.getYear() % 100);
        article.setMonthTime(now.getMonthValue());
        List<ArticleImg> oldImages = creating ? List.of() : articleImgMapper.selectByArticleId(article.getId());
        List<ImageSaveItem> uploaded = ImageSaveItem.normalize(req.getImages(), req.getImageUrls());
        List<ImageSaveItem> nextImages = contentImages(req.getContent(), uploaded);
        deleteOrphanUploads(uploaded, nextImages, article.getCover(), article.getThumbnail());
        if (creating) {
            article.setComments(0);
            article.setPv(0);
            articleMapper.insert(article);
            ArticleContent content = new ArticleContent();
            content.setArticleId(article.getId());
            content.setContent(req.getContent());
            contentMapper.insert(content);
        } else {
            if (!Objects.equals(old.getCover(), article.getCover()) || !Objects.equals(old.getThumbnail(), article.getThumbnail())) {
                if (!stillUsed(article.getCover(), article.getThumbnail(), nextImages, old.getCover())) {
                    miscService.tryDeleteFile(old.getCover());
                }
                if (!stillUsed(article.getCover(), article.getThumbnail(), nextImages, old.getThumbnail())) {
                    miscService.tryDeleteFile(old.getThumbnail());
                }
            }
            deleteUnusedImages(oldImages, nextImages, article.getCover(), article.getThumbnail());
            articleMapper.update(article);
            ArticleContent existing = contentMapper.selectByArticleId(article.getId());
            ArticleContent content = new ArticleContent();
            content.setArticleId(article.getId());
            content.setContent(req.getContent());
            if (existing == null) {
                contentMapper.insert(content);
            } else {
                contentMapper.updateByArticleId(content);
            }
            articleTagMapper.deleteByArticleId(article.getId());
            articleImgMapper.deleteByArticleId(article.getId());
        }
        if (req.getTagIds() != null) {
            for (Long tagId : req.getTagIds()) {
                ArticleTag rel = new ArticleTag();
                rel.setArticleId(article.getId());
                rel.setTagId(tagId);
                articleTagMapper.insert(rel);
            }
        }
        for (ImageSaveItem item : nextImages) {
            ArticleImg img = new ArticleImg();
            img.setArticleId(article.getId());
            img.setImgUrl(trimUrl(item.getImgUrl()));
            img.setThumbnailUrl(trimUrl(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getImgUrl()));
            articleImgMapper.insert(img);
        }
        refreshCategoryCount(req.getCategoryId());
        if (oldCategory != null && !oldCategory.equals(req.getCategoryId())) {
            refreshCategoryCount(oldCategory);
        }
        logService.record("保存文章", "成功", req.getTitle());
        return article.getId();
    }

    @Transactional
    public void delete(Long id) {
        Article old = articleMapper.selectById(id);
        if (old == null) {
            return;
        }
        List<ArticleImg> images = articleImgMapper.selectByArticleId(id);
        for (ArticleImg img : images) {
            miscService.tryDeleteFiles(img.getImgUrl(), img.getThumbnailUrl());
        }
        miscService.tryDeleteFiles(old.getCover(), old.getThumbnail());
        contentMapper.deleteByArticleId(id);
        articleTagMapper.deleteByArticleId(id);
        articleImgMapper.deleteByArticleId(id);
        articleMapper.deleteById(id);
        refreshCategoryCount(old.getCategoryId());
        logService.record("删除文章", "成功", old.getTitle());
    }

    public List<Article> archive() {
        return articleMapper.selectArchive().stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .toList();
    }

    public List<Article> hot(int limit) {
        return articleMapper.selectHot(limit);
    }

    private void fillTags(Article article) {
        List<Tag> tags = articleTagMapper.selectTagsByArticleId(article.getId());
        article.setTags(tags);
        article.setTagIds(tags.stream().map(Tag::getId).toList());
        article.setTagNames(tags.stream().map(Tag::getName).toList());
    }

    private void refreshCategoryCount(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        int count = articleMapper.recountCategory(categoryId);
        categoryMapper.updateCount(categoryId, count);
    }

    private List<ImageSaveItem> contentImages(String content, List<ImageSaveItem> uploaded) {
        Map<String, ImageSaveItem> byUrl = new LinkedHashMap<>();
        if (uploaded != null) {
            for (ImageSaveItem item : uploaded) {
                if (item.getImgUrl() != null) {
                    byUrl.put(item.getImgUrl(), item);
                }
                if (item.getThumbnailUrl() != null) {
                    byUrl.putIfAbsent(item.getThumbnailUrl(), item);
                }
            }
        }
        List<ImageSaveItem> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Matcher matcher = MD_IMAGE.matcher(content == null ? "" : content);
        while (matcher.find()) {
            String url = trimUrl(matcher.group(1));
            if (url == null || !seen.add(url)) {
                continue;
            }
            ImageSaveItem uploadedItem = byUrl.get(url);
            ImageSaveItem item = new ImageSaveItem();
            if (uploadedItem != null) {
                item.setImgUrl(trimUrl(uploadedItem.getImgUrl()));
                item.setThumbnailUrl(trimUrl(uploadedItem.getThumbnailUrl() != null
                        ? uploadedItem.getThumbnailUrl() : uploadedItem.getImgUrl()));
            } else {
                item.setImgUrl(url);
                item.setThumbnailUrl(url);
            }
            result.add(item);
        }
        return result;
    }

    private void deleteOrphanUploads(List<ImageSaveItem> uploaded, List<ImageSaveItem> nextImages, String cover, String thumbnail) {
        Set<String> keep = keepSet(nextImages, cover, thumbnail);
        if (uploaded == null) {
            return;
        }
        for (ImageSaveItem item : uploaded) {
            if (item.getImgUrl() != null && !keep.contains(item.getImgUrl())) {
                miscService.tryDeleteFile(item.getImgUrl());
            }
            if (item.getThumbnailUrl() != null && !keep.contains(item.getThumbnailUrl())) {
                miscService.tryDeleteFile(item.getThumbnailUrl());
            }
        }
    }

    private void deleteUnusedImages(List<ArticleImg> oldImages, List<ImageSaveItem> nextImages, String cover, String thumbnail) {
        Set<String> keep = keepSet(nextImages, cover, thumbnail);
        for (ArticleImg img : oldImages) {
            if (!keep.contains(img.getImgUrl())) {
                miscService.tryDeleteFile(img.getImgUrl());
            }
            if (!keep.contains(img.getThumbnailUrl())) {
                miscService.tryDeleteFile(img.getThumbnailUrl());
            }
        }
    }

    private boolean stillUsed(String cover, String thumbnail, List<ImageSaveItem> nextImages, String url) {
        return keepSet(nextImages, cover, thumbnail).contains(url);
    }

    private Set<String> keepSet(List<ImageSaveItem> nextImages, String cover, String thumbnail) {
        Set<String> keep = new HashSet<>();
        if (cover != null) {
            keep.add(cover);
        }
        if (thumbnail != null) {
            keep.add(thumbnail);
        }
        if (nextImages != null) {
            for (ImageSaveItem item : nextImages) {
                if (item.getImgUrl() != null) {
                    keep.add(item.getImgUrl());
                }
                if (item.getThumbnailUrl() != null) {
                    keep.add(item.getThumbnailUrl());
                }
            }
        }
        return keep;
    }

    private String resolveDescription(String description, String content) {
        String value = description == null ? "" : description.trim();
        if (value.isEmpty()) {
            value = MarkdownExcerpt.from(content, DESCRIPTION_MAX);
        }
        if (value.isEmpty()) {
            return null;
        }
        return value.length() > DESCRIPTION_MAX ? value.substring(0, DESCRIPTION_MAX) : value;
    }

    private String trimUrl(String url) {
        if (url == null) {
            return null;
        }
        return url.length() > 80 ? url.substring(0, 80) : url;
    }
}
