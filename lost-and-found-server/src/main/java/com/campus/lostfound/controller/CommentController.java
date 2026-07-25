package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.CommentRequest;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.CommentService;
import com.campus.lostfound.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论模块")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<?> addComment(
            @Valid @RequestBody CommentRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        commentService.addComment(userId, request.getItemId(), request.getContent(), request.getParentId());
        return Result.success();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取物品评论列表")
    @GetMapping("/{itemId}")
    public Result<List<CommentVO>> getComments(@PathVariable Long itemId) {
        return Result.success(commentService.getCommentsByItemId(itemId));
    }

    private void checkLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
