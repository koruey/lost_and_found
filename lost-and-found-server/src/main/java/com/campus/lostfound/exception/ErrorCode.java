package com.campus.lostfound.exception;

import lombok.Getter;

/**
 * 统一错误码枚举
 */
@Getter
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误 (1xxx)
    BUSINESS_ERROR(1001, "业务处理异常"),

    // 用户模块 (2xxx)
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_DISABLED(2002, "账号已被禁用"),
    LOGIN_FAILED(2003, "登录失败"),

    // 物品模块 (3xxx)
    ITEM_NOT_FOUND(3001, "物品信息不存在"),
    ITEM_ACCESS_DENIED(3002, "无权操作该物品"),
    ITEM_STATUS_ERROR(3003, "物品状态异常"),

    // 文件模块 (4xxx)
    FILE_UPLOAD_FAILED(4001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(4002, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),

    // 评论模块 (5xxx)
    COMMENT_NOT_FOUND(5001, "评论不存在"),
    COMMENT_DELETE_DENIED(5002, "无权删除该评论"),

    // AI模块 (6xxx)
    AI_SERVICE_ERROR(6001, "AI服务调用失败"),
    AI_RECOGNIZE_FAILED(6002, "图片识别失败"),
    AI_AUDIT_FAILED(6003, "内容审核失败"),

    // 管理员模块 (7xxx)
    ADMIN_REQUIRED(7001, "需要管理员权限"),
    AUDIT_ALREADY_DONE(7002, "该信息已审核过");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
