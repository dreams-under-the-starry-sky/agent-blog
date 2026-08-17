package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.IdGenerator;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MetaService {
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private LogService logService;

    public List<Category> categories() {
        categoryMapper.syncPublishedCounts();
        return categoryMapper.selectAll();
    }

    public Long saveCategory(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BizException("分类名不能为空");
        }
        category.setName(category.getName().trim());
        if (categoryMapper.countByName(category.getName(), category.getId()) > 0) {
            throw new BizException("分类名已存在");
        }
        if (category.getId() == null) {
            category.setId(IdGenerator.nextId());
            category.setCount(0);
            categoryMapper.insert(category);
            logService.record("新增分类", "成功", category.getName());
        } else {
            categoryMapper.update(category);
            logService.record("修改分类", "成功", category.getName());
        }
        return category.getId();
    }

    public void deleteCategory(Long id) {
        if (articleMapper.countByCategoryId(id) > 0) {
            throw new BizException("该分类下仍有文章，无法删除");
        }
        categoryMapper.deleteById(id);
        logService.record("删除分类", "成功", String.valueOf(id));
    }

    public List<Tag> tags() {
        return tagMapper.selectAll();
    }

    public Long saveTag(Tag tag) {
        if (!StringUtils.hasText(tag.getName())) {
            throw new BizException("标签名不能为空");
        }
        tag.setName(tag.getName().trim());
        if (tagMapper.countByName(tag.getName(), tag.getId()) > 0) {
            throw new BizException("标签名已存在");
        }
        if (tag.getId() == null) {
            tag.setId(IdGenerator.nextId());
            tagMapper.insert(tag);
            logService.record("新增标签", "成功", tag.getName());
        } else {
            tagMapper.update(tag);
            logService.record("修改标签", "成功", tag.getName());
        }
        return tag.getId();
    }

    public void deleteTag(Long id) {
        if (articleTagMapper.countByTagId(id) > 0) {
            throw new BizException("该标签下仍有文章，无法删除");
        }
        tagMapper.deleteById(id);
        logService.record("删除标签", "成功", String.valueOf(id));
    }
}
