package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_audit_log")
public class AiAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标类型: 0-物品, 1-评论 */
    private Integer targetType;

    private Long targetId;

    /** 审核结果: 0-通过, 1-违规 */
    private Integer result;

    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
