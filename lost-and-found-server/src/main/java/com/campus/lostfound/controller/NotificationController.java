package com.campus.lostfound.controller;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "通知模块")
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping
    public Result<PageResult<NotificationVO>> list(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        checkLogin(userId);
        return Result.success(notificationService.getNotifications(userId, page, size));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread")
    public Result<Map<String, Long>> unreadCount(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        return Result.success(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<?> markRead(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<?> markAllRead(
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    private void checkLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
