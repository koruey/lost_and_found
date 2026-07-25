package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.entity.Comment;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.CommentMapper;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.CommentService;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void addComment(Long userId, Long itemId, String content, Long parentId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }

        Comment comment = new Comment();
        comment.setItemId(itemId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setStatus(0);

        Long replyToUserId = null;
        // 回复评论
        if (parentId != null) {
            Comment parent = commentMapper.selectById(parentId);
            if (parent == null || !parent.getItemId().equals(itemId)) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
            comment.setParentId(parentId);
            replyToUserId = parent.getUserId();
            comment.setReplyToUserId(replyToUserId);
        }

        commentMapper.insert(comment);

        // 通知
        if (parentId != null && replyToUserId != null && !replyToUserId.equals(userId)) {
            // 回复评论：通知被回复者
            User replyUser = userMapper.selectById(userId);
            String notifyContent = (replyUser != null ? replyUser.getNickname() : "用户")
                    + " 回复了你的评论: " + content;
            notificationService.createNotification(
                    replyToUserId, 2, "新回复", notifyContent, itemId);
        } else if (!item.getUserId().equals(userId)) {
            // 直接评论：通知物品发布者
            User commentUser = userMapper.selectById(userId);
            String notifyContent = (commentUser != null ? commentUser.getNickname() : "用户")
                    + " 评论了你的物品: " + content;
            notificationService.createNotification(
                    item.getUserId(), 1, "新评论", notifyContent, itemId);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_DELETE_DENIED);
        }
        comment.setStatus(1); // 软删除
        commentMapper.updateById(comment);
    }

    @Override
    public List<CommentVO> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getItemId, itemId)
                        .eq(Comment::getStatus, 0)
                        .orderByAsc(Comment::getCreatedAt));

        // 转换为VO
        List<CommentVO> allVos = comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setParentId(comment.getParentId());

            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatarUrl());
            }

            // 回复目标昵称
            if (comment.getReplyToUserId() != null) {
                User replyTo = userMapper.selectById(comment.getReplyToUserId());
                if (replyTo != null) {
                    vo.setReplyToNickname(replyTo.getNickname());
                }
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构：顶级评论 + 子回复
        return allVos.stream()
                .filter(vo -> vo.getParentId() == null)
                .peek(vo -> vo.setReplies(
                        allVos.stream()
                                .filter(child -> vo.getId().equals(child.getParentId()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
