package com.campusmarket.faq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.faq.entity.Faq;
import com.campusmarket.faq.mapper.FaqMapper;
import com.campusmarket.faq.service.FaqService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqServiceImpl extends ServiceImpl<FaqMapper, Faq> implements FaqService {

    @Override
    public List<Faq> listAllSorted() {
        LambdaQueryWrapper<Faq> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Faq::getSortOrder);
        return this.list(wrapper);
    }
}
