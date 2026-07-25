package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "微信code不能为空")
    private String code;

    /** 用户昵称（可选，首次登录时填写） */
    private String nickname;

    /** 头像URL（可选） */
    private String avatarUrl;
}
