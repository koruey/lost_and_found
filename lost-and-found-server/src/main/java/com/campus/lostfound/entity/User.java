package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信OpenID */
    private String openid;

    /** 用户昵称 */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 手机号 */
    private String phone;

    /** 角色: 0-普通用户, 1-管理员 */
    private Integer role;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
