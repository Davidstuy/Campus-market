package com.campusmarket.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.community.dto.PostCommentVO;
import com.campusmarket.community.entity.Post;
import com.campusmarket.community.entity.PostComment;
import com.campusmarket.community.mapper.PostCommentMapper;
import com.campusmarket.community.mapper.PostMapper;
import com.campusmarket.community.service.PostCommentService;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    public Page<PostCommentVO> listByPost(Long postId, int pageNum, int size, Long currentUserId) {
        // 只查顶级评论
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .isNull(PostComment::getParentId)
                .orderByDesc(PostComment::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        List<PostComment> comments = this.list(wrapper);

        if (comments.isEmpty()) {
            Page<PostCommentVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        List<Long> parentIds = comments.stream().map(PostComment::getId).toList();

        // 查所有回复
        List<PostComment> allReplies = this.list(new LambdaQueryWrapper<PostComment>()
                .in(PostComment::getParentId, parentIds)
                .orderByAsc(PostComment::getCreatedAt));

        // 收集所有用户 ID
        Set<Long> userIdSet = new HashSet<>();
        comments.forEach(c -> userIdSet.add(c.getUserId()));
        allReplies.forEach(r -> {
            userIdSet.add(r.getUserId());
            if (r.getReplyToUserId() != null) userIdSet.add(r.getReplyToUserId());
        });

        Map<Long, User> userMap = userIdSet.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(new ArrayList<>(userIdSet)).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 回复按 parentId 分组
        Map<Long, List<PostComment>> repliesByParent = allReplies.stream()
                .collect(Collectors.groupingBy(PostComment::getParentId));

        // 组装 VO
        List<PostCommentVO> voList = comments.stream().map(c -> {
            PostCommentVO vo = toVO(c, userMap);
            List<PostComment> childReplies = repliesByParent.getOrDefault(c.getId(), List.of());
            vo.setReplies(childReplies.stream()
                    .map(r -> {
                        PostCommentVO rvo = toVO(r, userMap);
                        rvo.setReplyToUser(userMap.get(r.getReplyToUserId()));
                        return rvo;
                    })
                    .toList());
            return vo;
        }).toList();

        Page<PostCommentVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PostCommentVO create(Long userId, Long postId, String content,
                                 String imageUrl, String videoUrl,
                                 Long parentId, Long replyToUserId) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException(400, "评论内容不能超过2000字");
        }

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 如果是回复，校验父评论存在且属于同一帖子
        if (parentId != null) {
            PostComment parent = this.getById(parentId);
            if (parent == null || !parent.getPostId().equals(postId)) {
                throw new BusinessException(404, "父评论不存在");
            }
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setImageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl : null);
        comment.setVideoUrl(videoUrl != null && !videoUrl.isBlank() ? videoUrl : null);
        comment.setParentId(parentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setCreatedAt(LocalDateTime.now());
        this.save(comment);

        User user = userMapper.selectById(userId);
        if (user != null) user.setPassword(null);

        PostCommentVO vo = toVO(comment, Map.of(userId, user));
        if (replyToUserId != null) {
            User replyTo = userMapper.selectById(replyToUserId);
            if (replyTo != null) replyTo.setPassword(null);
            vo.setReplyToUser(replyTo);
        }
        return vo;
    }

    @Override
    public void delete(Long commentId, Long userId) {
        PostComment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        this.removeById(commentId);
    }

    // ---------- 私有工具方法 ----------

    private PostCommentVO toVO(PostComment c, Map<Long, User> userMap) {
        PostCommentVO vo = new PostCommentVO();
        vo.setId(c.getId());
        vo.setPostId(c.getPostId());
        vo.setUserId(c.getUserId());
        vo.setContent(c.getContent());
        vo.setImageUrl(c.getImageUrl());
        vo.setVideoUrl(c.getVideoUrl());
        vo.setParentId(c.getParentId());
        vo.setReplyToUserId(c.getReplyToUserId());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUser(userMap.get(c.getUserId()));
        return vo;
    }
}
