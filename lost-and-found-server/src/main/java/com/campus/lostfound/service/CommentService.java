package com.campus.lostfound.service;

import com.campus.lostfound.vo.CommentVO;

import java.util.List;

public interface CommentService {

    void addComment(Long userId, Long itemId, String content, Long parentId);

    void deleteComment(Long userId, Long commentId);

    List<CommentVO> getCommentsByItemId(Long itemId);
}
