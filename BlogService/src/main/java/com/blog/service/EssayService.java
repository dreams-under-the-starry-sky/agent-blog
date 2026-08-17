package com.blog.service;

import com.blog.common.IdGenerator;
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

import java.util.HashSet;
import java.util.List;
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
        list.forEach(e -> e.setImages(essayImgMapper.selectByEssayId(e.getId())));
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
            img.setImgUrl(miscService.clipUrl(item.getImgUrl()));
            img.setThumbnailUrl(miscService.clipUrl(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getImgUrl()));
            essayImgMapper.insert(img);
        }
        return essay.getId();
    }

    @Transactional
    public void delete(Long id) {
        List<EssayImg> images = essayImgMapper.selectByEssayId(id);
        for (EssayImg img : images) {
            miscService.tryDeleteFiles(img.getImgUrl(), img.getThumbnailUrl());
        }
        essayImgMapper.deleteByEssayId(id);
        essayMapper.deleteById(id);
    }

    private void deleteUnused(List<EssayImg> oldImages, List<ImageSaveItem> next) {
        Set<String> keep = new HashSet<>();
        for (ImageSaveItem item : next) {
            if (item.getImgUrl() != null) {
                keep.add(item.getImgUrl());
            }
            if (item.getThumbnailUrl() != null) {
                keep.add(item.getThumbnailUrl());
            }
        }
        for (EssayImg img : oldImages) {
            if (!keep.contains(img.getImgUrl())) {
                miscService.tryDeleteFile(img.getImgUrl());
            }
            if (!keep.contains(img.getThumbnailUrl())) {
                miscService.tryDeleteFile(img.getThumbnailUrl());
            }
        }
    }
}
