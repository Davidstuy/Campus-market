package com.campusmarket.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.comment.dto.CommentVO;
import com.campusmarket.comment.entity.Comment;
import com.campusmarket.comment.entity.CommentVote;
import com.campusmarket.comment.mapper.CommentMapper;
import com.campusmarket.comment.mapper.CommentVoteMapper;
import com.campusmarket.comment.service.CommentService;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.mapper.ProductMapper;
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
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final CommentVoteMapper voteMapper;

    @Override
    public Page<CommentVO> listByProduct(Long productId, int pageNum, int size, Long currentUserId) {
        // 1. 只查顶级评论（parent_id IS NULL），按时间倒序
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getProductId, productId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        List<Comment> comments = this.list(wrapper);

        if (comments.isEmpty()) {
            Page<CommentVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        List<Long> parentIds = comments.stream().map(Comment::getId).toList();

        // 2. 查所有回复（按时间正序，让对话看起来自然）
        List<Comment> allReplies = this.list(new LambdaQueryWrapper<Comment>()
                .in(Comment::getParentId, parentIds)
                .orderByAsc(Comment::getCreatedAt));

        // 3. 收集所有需要查询的用户 ID
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

        // 4. 收集所有评论 ID（顶级 + 回复），批量查投票
        List<Long> allCommentIds = new ArrayList<>(parentIds);
        allCommentIds.addAll(allReplies.stream().map(Comment::getId).toList());

        Map<Long, long[]> countMap = new HashMap<>();
        if (!allCommentIds.isEmpty()) {
            List<CommentVote> allVotes = voteMapper.selectList(
                    new LambdaQueryWrapper<CommentVote>().in(CommentVote::getCommentId, allCommentIds));
            for (CommentVote v : allVotes) {
                long[] counts = countMap.computeIfAbsent(v.getCommentId(), k -> new long[2]);
                if (v.getVote() == 1) counts[0]++;
                else counts[1]++;
            }
        }

        Map<Long, Integer> myVoteMap = new HashMap<>();
        if (currentUserId != null && !allCommentIds.isEmpty()) {
            List<CommentVote> myVotes = voteMapper.selectList(
                    new LambdaQueryWrapper<CommentVote>()
                            .eq(CommentVote::getUserId, currentUserId)
                            .in(CommentVote::getCommentId, allCommentIds));
            for (CommentVote v : myVotes) {
                myVoteMap.put(v.getCommentId(), v.getVote());
            }
        }

        // 5. 回复按 parentId 分组
        Map<Long, List<Comment>> repliesByParent = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 6. 组装 VO（顶级评论 + 嵌套回复）
        List<CommentVO> voList = comments.stream().map(c -> {
            CommentVO vo = toVO(c, userMap, countMap, myVoteMap);

            List<Comment> childReplies = repliesByParent.getOrDefault(c.getId(), List.of());
            vo.setReplies(childReplies.stream()
                    .map(r -> {
                        CommentVO rvo = toVO(r, userMap, countMap, myVoteMap);
                        rvo.setReplyToUser(userMap.get(r.getReplyToUserId()));
                        return rvo;
                    })
                    .toList());
            return vo;
        }).toList();

        Page<CommentVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public CommentVO create(Long userId, Long productId, String content,
                             String imageUrl, String videoUrl,
                             Long parentId, Long replyToUserId) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }
        if (content.length() > 2000) {
            throw new BusinessException(400, "评论内容不能超过2000字");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 如果是回复，校验父评论存在且属于同一商品
        if (parentId != null) {
            Comment parent = this.getById(parentId);
            if (parent == null || !parent.getProductId().equals(productId)) {
                throw new BusinessException(404, "父评论不存在");
            }
        }

        Comment comment = new Comment();
        comment.setProductId(productId);
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

        CommentVO vo = toVO(comment, Map.of(userId, user), Map.of(), Map.of());
        if (replyToUserId != null) {
            User replyTo = userMapper.selectById(replyToUserId);
            if (replyTo != null) replyTo.setPassword(null);
            vo.setReplyToUser(replyTo);
        }
        return vo;
    }

    @Override
    public void vote(Long userId, Long commentId, int vote) {
        if (vote != 1 && vote != -1) {
            throw new BusinessException(400, "vote 必须为 1（赞）或 -1（踩）");
        }
        Comment comment = this.getById(commentId);
        if (comment == null) throw new BusinessException(404, "评论不存在");

        CommentVote existing = voteMapper.selectOne(new LambdaQueryWrapper<CommentVote>()
                .eq(CommentVote::getCommentId, commentId)
                .eq(CommentVote::getUserId, userId));
        if (existing != null) {
            existing.setVote(vote);
            voteMapper.updateById(existing);
        } else {
            CommentVote cv = new CommentVote();
            cv.setCommentId(commentId);
            cv.setUserId(userId);
            cv.setVote(vote);
            voteMapper.insert(cv);
        }
    }

    @Override
    public void cancelVote(Long userId, Long commentId) {
        voteMapper.delete(new LambdaQueryWrapper<CommentVote>()
                .eq(CommentVote::getCommentId, commentId)
                .eq(CommentVote::getUserId, userId));
    }

    // ---------- 私有工具方法 ----------

    private CommentVO toVO(Comment c, Map<Long, User> userMap,
                           Map<Long, long[]> countMap, Map<Long, Integer> myVoteMap) {
        CommentVO vo = new CommentVO();
        vo.setId(c.getId());
        vo.setProductId(c.getProductId());
        vo.setUserId(c.getUserId());
        vo.setContent(c.getContent());
        vo.setImageUrl(c.getImageUrl());
        vo.setVideoUrl(c.getVideoUrl());
        vo.setParentId(c.getParentId());
        vo.setReplyToUserId(c.getReplyToUserId());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUser(userMap.get(c.getUserId()));

        long[] counts = countMap.getOrDefault(c.getId(), new long[2]);
        vo.setUpCount(counts[0]);
        vo.setDownCount(counts[1]);
        vo.setMyVote(myVoteMap.get(c.getId()));
        return vo;
    }
}
