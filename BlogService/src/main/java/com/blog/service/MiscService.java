package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.IdGenerator;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.DashboardVO;
import com.blog.dto.UploadResult;
import com.blog.entity.Black;
import com.blog.entity.BlogLog;
import com.blog.entity.EmailRecord;
import com.blog.entity.FileDelFail;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.entity.Music;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.BlogLogMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.EmailRecordMapper;
import com.blog.mapper.EssayMapper;
import com.blog.mapper.FileDelFailMapper;
import com.blog.mapper.FriendCategoryMapper;
import com.blog.mapper.FriendMapper;
import com.blog.mapper.MessageMapper;
import com.blog.mapper.MusicMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class MiscService {
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private FriendCategoryMapper friendCategoryMapper;
    @Resource
    private MusicMapper musicMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private BlogLogMapper blogLogMapper;
    @Resource
    private EmailRecordMapper emailRecordMapper;
    @Resource
    private FileDelFailMapper fileDelFailMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private EssayMapper essayMapper;
    @Resource
    private AvifCompressor avifCompressor;

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;
    @Value("${blog.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private Path uploadRoot;

    @PostConstruct
    public void initUploadRoot() {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public List<Friend> friends() {
        return friendMapper.selectAll();
    }

    public Long saveFriend(Friend friend) {
        if (friend.getId() == null) {
            friend.setId(IdGenerator.nextId());
            if (friend.getSort() == null) {
                friend.setSort(99);
            }
            friendMapper.insert(friend);
        } else {
            friendMapper.update(friend);
        }
        return friend.getId();
    }

    public void deleteFriend(Long id) {
        friendMapper.deleteById(id);
    }

    public List<FriendCategory> friendCategories() {
        return friendCategoryMapper.selectAll();
    }

    public Long saveFriendCategory(FriendCategory category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BizException("分类名不能为空");
        }
        category.setName(category.getName().trim());
        if (friendCategoryMapper.countByName(category.getName(), category.getId()) > 0) {
            throw new BizException("分类名已存在");
        }
        if (category.getSort() == null) {
            category.setSort(99);
        }
        if (category.getId() == null) {
            category.setId(IdGenerator.nextId());
            friendCategoryMapper.insert(category);
        } else {
            friendCategoryMapper.update(category);
        }
        return category.getId();
    }

    public void deleteFriendCategory(Long id) {
        if (friendMapper.countByCategoryId(id) > 0) {
            throw new BizException("该分类下仍有友链，无法删除");
        }
        friendCategoryMapper.deleteById(id);
    }

    public List<Music> musicList() {
        return musicMapper.selectAll();
    }

    public Long saveMusic(Music music) {
        if (!StringUtils.hasText(music.getName())) {
            throw new BizException("请填写歌名");
        }
        if (!StringUtils.hasText(music.getUrl())) {
            throw new BizException("请填写播放地址");
        }
        music.setName(music.getName().trim());
        music.setAuthor(trimToNull(music.getAuthor()));
        music.setUrl(music.getUrl().trim());
        music.setCover(trimToNull(music.getCover()));
        music.setLrc(trimToNull(music.getLrc()));
        if (music.getId() == null) {
            music.setId(IdGenerator.nextId());
            musicMapper.insert(music);
        } else {
            musicMapper.update(music);
        }
        return music.getId();
    }

    public void deleteMusic(Long id) {
        musicMapper.deleteById(id);
    }

    public List<Black> blacks() {
        return blackMapper.selectAll();
    }

    public Integer saveBlack(Black black) {
        if (black.getId() == null) {
            black.setId((int) (System.currentTimeMillis() / 1000));
        }
        blackMapper.insert(black);
        return black.getId();
    }

    public void deleteBlack(Integer id) {
        blackMapper.deleteById(id);
    }

    public PageResult<BlogLog> logs(PageQuery query) {
        return new PageResult<>(blogLogMapper.countPage(query), blogLogMapper.selectPage(query));
    }

    public PageResult<EmailRecord> emails(PageQuery query) {
        return new PageResult<>(emailRecordMapper.countPage(query), emailRecordMapper.selectPage(query));
    }

    public List<FileDelFail> fileDelFails() {
        return fileDelFailMapper.selectAll();
    }

    public void deleteFileDelFail(Integer id) {
        fileDelFailMapper.deleteById(id);
    }

    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleCount(articleMapper.countAll());
        vo.setPublishedCount(articleMapper.countPublished());
        vo.setCommentCount(commentMapper.countAll());
        vo.setMessageCount(messageMapper.countAll());
        vo.setEssayCount(essayMapper.countAll());
        Integer pv = articleMapper.sumPv();
        vo.setPvTotal(pv == null ? 0 : pv);
        vo.setCategoryStats(articleMapper.countByCategory());
        vo.setRecentArticles(articleMapper.selectRecent(5));
        vo.setRecentComments(commentMapper.selectRecent(5));
        return vo;
    }

    public UploadResult upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path dir = uploadRoot.resolve(month);
        Files.createDirectories(dir);
        String stem = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String name = stem + ext;
        Path dest = dir.resolve(name);
        byte[] bytes = file.getBytes();
        Files.write(dest, bytes);
        String url = clipUrl(urlPrefix + "/" + month + "/" + name);
        String thumbnailUrl = url;
        String avifName = ".avif".equals(ext) ? stem + "_t.avif" : stem + ".avif";
        Path avifDest = dir.resolve(avifName);
        if (avifCompressor.compressToAvif(bytes, avifDest)) {
            thumbnailUrl = clipUrl(urlPrefix + "/" + month + "/" + avifName);
        }
        return UploadResult.of(url, thumbnailUrl);
    }

    public void tryDeleteFiles(String... urls) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            tryDeleteFile(url);
        }
    }

    public void tryDeleteFile(String url) {
        if (url == null || !url.startsWith(urlPrefix)) {
            return;
        }
        String relative = url.substring(urlPrefix.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        Path file = uploadRoot.resolve(relative).normalize();
        if (!file.startsWith(uploadRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (Exception e) {
            FileDelFail fail = new FileDelFail();
            fail.setFileKey(url.length() > 60 ? url.substring(0, 60) : url);
            fail.setExtra(e.getMessage());
            fileDelFailMapper.insert(fail);
        }
    }

    public String clipUrl(String url) {
        if (url == null) {
            return null;
        }
        return url.length() > 80 ? url.substring(0, 80) : url;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
