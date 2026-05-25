package com.campusmarket.category.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.category.entity.Category;
import com.campusmarket.category.mapper.CategoryMapper;
import com.campusmarket.category.service.CategoryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> list() {
        return lambdaQuery().orderByAsc(Category::getSortOrder).list();
    }
}
