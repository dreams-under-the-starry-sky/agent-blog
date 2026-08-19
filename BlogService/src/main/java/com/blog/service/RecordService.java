package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
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

import java.util.HashSet;
import java.util.List;
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
        list.forEach(r -> r.setImages(recordImgMapper.selectByRecordId(r.getId())));
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
            img.setImgUrl(miscService.clipUrl(item.getImgUrl()));
            img.setThumbnailUrl(miscService.clipUrl(item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getImgUrl()));
            recordImgMapper.insert(img);
        }
        return record.getId();
    }

    @Transactional
    public void delete(Long id) {
        List<RecordImg> images = recordImgMapper.selectByRecordId(id);
        for (RecordImg img : images) {
            miscService.tryDeleteFiles(img.getImgUrl(), img.getThumbnailUrl());
        }
        recordImgMapper.deleteByRecordId(id);
        recordMapper.deleteById(id);
    }

    private void deleteUnused(List<RecordImg> oldImages, List<ImageSaveItem> next) {
        Set<String> keep = new HashSet<>();
        for (ImageSaveItem item : next) {
            if (item.getImgUrl() != null) {
                keep.add(item.getImgUrl());
            }
            if (item.getThumbnailUrl() != null) {
                keep.add(item.getThumbnailUrl());
            }
        }
        for (RecordImg img : oldImages) {
            if (!keep.contains(img.getImgUrl())) {
                miscService.tryDeleteFile(img.getImgUrl());
            }
            if (!keep.contains(img.getThumbnailUrl())) {
                miscService.tryDeleteFile(img.getThumbnailUrl());
            }
        }
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
