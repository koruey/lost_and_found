package com.campus.lostfound.service;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.vo.NotificationVO;

public interface NotificationService {

    PageResult<NotificationVO> getNotifications(Long userId, Integer page, Integer size);

    Long getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void createNotification(Long userId, Integer type, String title, String content, Long relatedId);
}
