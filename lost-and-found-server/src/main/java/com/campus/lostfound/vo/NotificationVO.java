package com.campus.lostfound.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {

    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
