package com.blog.service;

import com.blog.entity.Black;
import com.blog.mapper.BlackMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlackService {
    @Resource
    private BlackMapper blackMapper;

    public List<Black> list() {
        return blackMapper.selectAll();
    }

    public Integer save(Black black) {
        if (black.getId() == null) {
            black.setId((int) (System.currentTimeMillis() / 1000));
        }
        blackMapper.insert(black);
        return black.getId();
    }

    public void delete(Integer id) {
        blackMapper.deleteById(id);
    }
}
