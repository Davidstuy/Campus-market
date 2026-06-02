package com.campusmarket.faq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.faq.entity.Faq;

import java.util.List;

public interface FaqService extends IService<Faq> {

    /** 按排序返回所有 FAQ */
    List<Faq> listAllSorted();
}
