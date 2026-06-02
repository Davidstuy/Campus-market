package com.campusmarket.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.community.dto.TopicVO;
import com.campusmarket.community.entity.Topic;

import java.util.List;
import java.util.Map;

public interface TopicService extends IService<Topic> {

    /** 获取所有主题（含帖子数） */
    List<TopicVO> listTopics();

    /** 热搜话题（按最近7天帖子数排序，前8个） */
    List<Map<String, Object>> hotTopics();
}
