package com.blog.service;

import com.blog.common.IdGenerator;
import com.blog.common.ImageUrls;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.ImageSaveItem;
import com.blog.entity.Essay;
import com.blog.entity.EssayImg;
import com.blog.mapper.EssayImgMapper;
import com.blog.mapper.EssayMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class EssayService {
    @Resource
    private EssayMapper essayMapper;
    @Resource
    private EssayImgMapper essayImgMapper;
    @Resource
    private MiscService miscService;

    public PageResult<Essay> page(PageQuery query) {
        List<Essay> list = essayMapper.selectPage(query);
        fillImages(list);
        return new PageResult<>(essayMapper.countPage(query), list);
    }

    public Essay detail(Long id) {
        Essay essay = essayMapper.selectById(id);
        if (essay != null) {
            essay.setImages(essayImgMapper.selectByEssayId(id));
        }
        return essay;
    }

    @Transactional
    public Long save(Essay essay, List<ImageSaveItem> images) {
        boolean creating = essay.getId() == null;
        List<EssayImg> oldImages = creating ? List.of() : essayImgMapper.selectByEssayId(essay.getId());
        List<ImageSaveItem> next = images == null ? List.of() : images;
        if (creating) {
            essay.setId(IdGenerator.nextId());
            if (essay.getStatus() == null) {
                essay.setStatus(1);
            }
            essayMapper.insert(essay);
        } else {
            deleteUnused(oldImages, next);
            essayMapper.update(essay);
            essayImgMapper.deleteByEssayId(essay.getId());
        }
        for (ImageSaveItem item : next) {
            if (item.getImgUrl() == null || item.getImgUrl().isBlank()) {
                continue;
            }
            EssayImg img = new EssayImg();
            img.setEssayId(essay.getId());
            img.setImgUrl(ImageUrls.clip(item.getImgUrl()));
            img.setThumbnailUrl(ImageUrls.clip(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getImgUrl()));
            essayImgMapper.insert(img);
        }
        return essay.getId();
    }

    @Transactional
    public void delete(Long id) {
        List<EssayImg> images = essayImgMapper.selectByEssayId(id);
        List<String> urls = new ArrayList<>();
        for (EssayImg img : images) {
            urls.add(img.getImgUrl());
            urls.add(img.getThumbnailUrl());
        }
        miscService.tryDeleteFiles(urls);
        essayImgMapper.deleteByEssayId(id);
        essayMapper.deleteById(id);
    }

    private void fillImages(List<Essay> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(Essay::getId).toList();
        List<EssayImg> images = essayImgMapper.selectByEssayIds(ids);
        Map<Long, List<EssayImg>> grouped = new LinkedHashMap<>();
        for (EssayImg img : images) {
            grouped.computeIfAbsent(img.getEssayId(), key -> new ArrayList<>()).add(img);
        }
        for (Essay essay : list) {
            essay.setImages(grouped.getOrDefault(essay.getId(), List.of()));
        }
    }

    private void deleteUnused(List<EssayImg> oldImages, List<ImageSaveItem> next) {
        Set<String> keep = ImageUrls.keepSet(next);
        List<String> urls = new ArrayList<>();
        for (EssayImg img : oldImages) {
            ImageUrls.addUnused(urls, img.getImgUrl(), img.getThumbnailUrl(), keep);
        }
        miscService.tryDeleteFiles(urls);
    }
}
