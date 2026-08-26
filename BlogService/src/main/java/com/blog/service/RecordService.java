package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.common.ImageUrls;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.ImageSaveItem;
import com.blog.entity.Record;
import com.blog.entity.RecordCategory;
import com.blog.entity.RecordImg;
import com.blog.mapper.RecordCategoryMapper;
import com.blog.mapper.RecordImgMapper;
import com.blog.mapper.RecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecordService {
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private RecordImgMapper recordImgMapper;
    @Resource
    private RecordCategoryMapper recordCategoryMapper;
    @Resource
    private MiscService miscService;

    public PageResult<Record> page(PageQuery query) {
        List<Record> list = recordMapper.selectPage(query);
        fillImages(list);
        return new PageResult<>(recordMapper.countPage(query), list);
    }

    public Record detail(Long id) {
        Record record = recordMapper.selectById(id);
        if (record != null) {
            record.setImages(recordImgMapper.selectByRecordId(id));
        }
        return record;
    }

    @Transactional
    public Long save(Record record, List<ImageSaveItem> images) {
        boolean creating = record.getId() == null;
        List<RecordImg> oldImages = creating ? List.of() : recordImgMapper.selectByRecordId(record.getId());
        List<ImageSaveItem> next = images == null ? List.of() : images;
        if (creating) {
            record.setId(IdGenerator.nextId());
            if (record.getStatus() == null) {
                record.setStatus(1);
            }
            recordMapper.insert(record);
        } else {
            deleteUnused(oldImages, next);
            recordMapper.update(record);
            recordImgMapper.deleteByRecordId(record.getId());
        }
        for (ImageSaveItem item : next) {
            if (item.getImgUrl() == null || item.getImgUrl().isBlank()) {
                continue;
            }
            RecordImg img = new RecordImg();
            img.setRecordId(record.getId());
            img.setImgUrl(ImageUrls.clip(item.getImgUrl()));
            img.setThumbnailUrl(ImageUrls.clip(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getImgUrl()));
            recordImgMapper.insert(img);
        }
        return record.getId();
    }

    @Transactional
    public void delete(Long id) {
        List<RecordImg> images = recordImgMapper.selectByRecordId(id);
        List<String> urls = new ArrayList<>();
        for (RecordImg img : images) {
            urls.add(img.getImgUrl());
            urls.add(img.getThumbnailUrl());
        }
        miscService.tryDeleteFiles(urls);
        recordImgMapper.deleteByRecordId(id);
        recordMapper.deleteById(id);
    }

    private void fillImages(List<Record> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(Record::getId).toList();
        List<RecordImg> images = recordImgMapper.selectByRecordIds(ids);
        Map<Long, List<RecordImg>> grouped = new LinkedHashMap<>();
        for (RecordImg img : images) {
            grouped.computeIfAbsent(img.getRecordId(), key -> new ArrayList<>()).add(img);
        }
        for (Record record : list) {
            record.setImages(grouped.getOrDefault(record.getId(), List.of()));
        }
    }

    private void deleteUnused(List<RecordImg> oldImages, List<ImageSaveItem> next) {
        Set<String> keep = ImageUrls.keepSet(next);
        List<String> urls = new ArrayList<>();
        for (RecordImg img : oldImages) {
            ImageUrls.addUnused(urls, img.getImgUrl(), img.getThumbnailUrl(), keep);
        }
        miscService.tryDeleteFiles(urls);
    }

    public List<RecordCategory> categories() {
        return recordCategoryMapper.selectAll();
    }

    public Long saveCategory(RecordCategory category) {
        category.setName(category.getName().trim());
        if (recordCategoryMapper.countByName(category.getName(), category.getId()) > 0) {
            throw new BizException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        if (category.getId() == null) {
            category.setId(IdGenerator.nextId());
            recordCategoryMapper.insert(category);
        } else {
            recordCategoryMapper.update(category);
        }
        return category.getId();
    }

    public void deleteCategory(Long id) {
        if (recordMapper.countByCategoryId(id) > 0) {
            throw new BizException(ErrorCode.CATEGORY_HAS_RECORDS);
        }
        recordCategoryMapper.deleteById(id);
    }
}
