package com.campusmarket.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.community.dto.TopicVO;
import com.campusmarket.community.entity.Post;
import com.campusmarket.community.entity.Topic;
import com.campusmarket.community.mapper.PostMapper;
import com.campusmarket.community.mapper.TopicMapper;
import com.campusmarket.community.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    private final PostMapper postMapper;

    @Override
    public List<TopicVO> listTopics() {
        List<Topic> topics = this.list(new LambdaQueryWrapper<Topic>().orderByAsc(Topic::getSortOrder));

        // 统计每个主题的帖子数
        List<Post> allPosts = postMapper.selectList(null);
        Map<Long, Long> countMap = allPosts.stream()
                .collect(Collectors.groupingBy(Post::getTopicId, Collectors.counting()));

        return topics.stream().map(t -> {
            TopicVO vo = new TopicVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setIcon(t.getIcon());
            vo.setSortOrder(t.getSortOrder());
            vo.setPostCount(countMap.getOrDefault(t.getId(), 0L));
            return vo;
        }).toList();
    }

    @Override
    public List<Map<String, Object>> hotTopics() {
        // 统计最近7天各主题的帖子数，按热度排序取前8
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Post> recentPosts = postMapper.selectList(
                new LambdaQueryWrapper<Post>().ge(Post::getCreatedAt, sevenDaysAgo));

        Map<Long, Long> countMap = recentPosts.stream()
                .collect(Collectors.groupingBy(Post::getTopicId, Collectors.counting()));

        // 按帖子数降序排序
        List<Map.Entry<Long, Long>> sorted = new ArrayList<>(countMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // 取前8个热点主题
        List<Topic> allTopics = this.list();
        Map<Long, Topic> topicMap = allTopics.stream()
                .collect(Collectors.toMap(Topic::getId, t -> t));

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, Long> entry : sorted) {
            if (rank > 8) break;
            Topic t = topicMap.get(entry.getKey());
            if (t == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", rank++);
            item.put("topicId", t.getId());
            item.put("name", t.getName());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }
}
