package com.campusmarket.community.controller;

import com.campusmarket.common.response.ApiResult;
import com.campusmarket.community.dto.TopicVO;
import com.campusmarket.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/topics")
public class TopicController {

    private final TopicService topicService;

    /** 获取所有社区主题（公开） */
    @GetMapping
    public ApiResult<List<TopicVO>> list() {
        return ApiResult.success(topicService.listTopics());
    }

    /** 热搜话题（按最近7天帖子数排序） */
    @GetMapping("/hot")
    public ApiResult<List<Map<String, Object>>> hotTopics() {
        return ApiResult.success(topicService.hotTopics());
    }
}
