package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private Long userId;

    private String content;

    /** 父评论ID(回复时使用), null表示顶级评论 */
    private Long parentId;

    /** 被回复用户ID */
    private Long replyToUserId;

    /** 状态: 0-正常, 1-已删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
