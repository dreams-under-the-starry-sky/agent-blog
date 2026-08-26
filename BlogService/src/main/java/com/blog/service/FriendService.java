package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.mapper.FriendCategoryMapper;
import com.blog.mapper.FriendMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private FriendCategoryMapper friendCategoryMapper;

    public List<Friend> friends() {
        return friendMapper.selectAll();
    }

    public Long saveFriend(Friend friend) {
        if (friend.getCategoryId() != null) {
            FriendCategory category = friendCategoryMapper.selectById(friend.getCategoryId());
            if (category != null && category.getSort() != null) {
                friend.setSort(category.getSort());
            }
        }
        if (friend.getSort() == null) {
            friend.setSort(99);
        }
        if (friend.getId() == null) {
            friend.setId(IdGenerator.nextId());
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
        category.setName(category.getName().trim());
        if (friendCategoryMapper.countByName(category.getName(), category.getId()) > 0) {
            throw new BizException(ErrorCode.CATEGORY_NAME_EXISTS);
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
            throw new BizException(ErrorCode.CATEGORY_HAS_FRIENDS);
        }
        friendCategoryMapper.deleteById(id);
    }
}
