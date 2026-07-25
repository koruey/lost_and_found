package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 类型: 0-匹配通知, 1-评论通知, 2-审核通知, 3-系统公告 */
    private Integer type;

    private String title;

    private String content;

    private Long relatedId;

    /** 已读: 0-未读, 1-已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
