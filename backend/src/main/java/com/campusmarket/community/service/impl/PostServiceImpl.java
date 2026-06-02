package com.campusmarket.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.community.dto.CreatePostRequest;
import com.campusmarket.community.dto.PostVO;
import com.campusmarket.community.dto.TopicVO;
import com.campusmarket.community.entity.Post;
import com.campusmarket.community.entity.PostComment;
import com.campusmarket.community.entity.PostMedia;
import com.campusmarket.community.entity.Topic;
import com.campusmarket.community.mapper.PostCommentMapper;
import com.campusmarket.community.mapper.PostMapper;
import com.campusmarket.community.mapper.PostMediaMapper;
import com.campusmarket.community.mapper.TopicMapper;
import com.campusmarket.community.service.PostService;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMediaMapper postMediaMapper;
    private final PostCommentMapper postCommentMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    @Override
    public Page<PostVO> listPosts(int page, int size, Long topicId, Long currentUserId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .orderByDesc(Post::getCreatedAt);

        if (topicId != null && topicId > 0) {
            wrapper.eq(Post::getTopicId, topicId);
        }

        long total = this.count(wrapper);
        int offset = (page - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        List<Post> posts = this.list(wrapper);

        if (posts.isEmpty()) {
            Page<PostVO> empty = new Page<>(page, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        // 批量查用户
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 批量查主题
        List<Long> topicIds = posts.stream().map(Post::getTopicId).distinct().toList();
        Map<Long, Topic> topicMap = topicMapper.selectBatchIds(topicIds).stream()
                .collect(Collectors.toMap(Topic::getId, Function.identity()));

        // 批量查媒体
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, List<PostMedia>> mediaMap = loadMediaMap(postIds);

        // 批量查评论数
        Map<Long, Long> commentCountMap = loadCommentCountMap(postIds);

        List<PostVO> vos = posts.stream().map(p -> {
            PostVO vo = new PostVO();
            vo.setId(p.getId());
            vo.setUserId(p.getUserId());
            vo.setTopicId(p.getTopicId());
            vo.setTitle(p.getTitle());
            vo.setContent(p.getContent());
            vo.setCreatedAt(p.getCreatedAt());
            vo.setUpdatedAt(p.getUpdatedAt());
            vo.setUser(userMap.get(p.getUserId()));

            Topic t = topicMap.get(p.getTopicId());
            if (t != null) {
                TopicVO tvo = new TopicVO();
                tvo.setId(t.getId());
                tvo.setName(t.getName());
                tvo.setIcon(t.getIcon());
                vo.setTopic(tvo);
            }

            vo.setMedia(mediaMap.getOrDefault(p.getId(), List.of()));
            vo.setCommentCount(commentCountMap.getOrDefault(p.getId(), 0L));
            return vo;
        }).toList();

        Page<PostVO> voPage = new Page<>(page, size, total);
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        return toVO(post);
    }

    @Override
    @Transactional
    public PostVO createPost(CreatePostRequest request, Long userId) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException(400, "标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "内容不能为空");
        }
        if (request.getTopicId() == null) {
            throw new BusinessException(400, "请选择主题");
        }

        Topic topic = topicMapper.selectById(request.getTopicId());
        if (topic == null) {
            throw new BusinessException(404, "主题不存在");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setTopicId(request.getTopicId());
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        this.save(post);

        // 保存媒体列表
        if (request.getMedia() != null && !request.getMedia().isEmpty()) {
            List<PostMedia> mediaList = new ArrayList<>();
            for (int i = 0; i < Math.min(request.getMedia().size(), 9); i++) {
                String url = request.getMedia().get(i);
                if (url == null || url.isBlank()) continue;

                String mediaType = detectMediaType(url);
                PostMedia pm = new PostMedia();
                pm.setPostId(post.getId());
                pm.setMediaType(mediaType);
                pm.setUrl(url);
                pm.setSortOrder(i);
                mediaList.add(pm);
            }
            if (!mediaList.isEmpty()) {
                for (PostMedia pm : mediaList) {
                    postMediaMapper.insert(pm);
                }
            }
        }

        return toVO(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = this.getById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的帖子");
        }

        // 级联删除媒体和评论
        postMediaMapper.delete(new LambdaQueryWrapper<PostMedia>().eq(PostMedia::getPostId, postId));
        postCommentMapper.delete(new LambdaQueryWrapper<PostComment>().eq(PostComment::getPostId, postId));
        this.removeById(postId);
    }

    // ---------- 私有工具方法 ----------

    private PostVO toVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTopicId(post.getTopicId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        // 作者
        User user = userMapper.selectById(post.getUserId());
        if (user != null) user.setPassword(null);
        vo.setUser(user);

        // 主题
        Topic topic = topicMapper.selectById(post.getTopicId());
        if (topic != null) {
            TopicVO tvo = new TopicVO();
            tvo.setId(topic.getId());
            tvo.setName(topic.getName());
            tvo.setIcon(topic.getIcon());
            vo.setTopic(tvo);
        }

        // 媒体
        List<PostMedia> media = postMediaMapper.selectList(
                new LambdaQueryWrapper<PostMedia>()
                        .eq(PostMedia::getPostId, post.getId())
                        .orderByAsc(PostMedia::getSortOrder));
        vo.setMedia(media);

        // 评论数
        Long commentCount = postCommentMapper.selectCount(
                new LambdaQueryWrapper<PostComment>().eq(PostComment::getPostId, post.getId()));
        vo.setCommentCount(commentCount);

        return vo;
    }

    private Map<Long, List<PostMedia>> loadMediaMap(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<PostMedia> all = postMediaMapper.selectList(
                new LambdaQueryWrapper<PostMedia>()
                        .in(PostMedia::getPostId, postIds)
                        .orderByAsc(PostMedia::getSortOrder));
        return all.stream().collect(Collectors.groupingBy(PostMedia::getPostId));
    }

    private Map<Long, Long> loadCommentCountMap(List<Long> postIds) {
        if (postIds.isEmpty()) return Map.of();
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>().in(PostComment::getPostId, postIds));
        return comments.stream()
                .collect(Collectors.groupingBy(PostComment::getPostId, Collectors.counting()));
    }

    /** 根据文件扩展名判断媒体类型 */
    private String detectMediaType(String url) {
        if (url == null) return "IMAGE";
        String lower = url.toLowerCase();
        if (lower.matches(".*\\.(mp4|mov|avi|webm|mkv)(\\?.*)?$")) {
            return "VIDEO";
        }
        return "IMAGE";
    }
}
