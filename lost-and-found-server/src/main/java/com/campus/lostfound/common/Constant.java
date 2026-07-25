package com.campus.lostfound.common;

/**
 * 系统常量
 */
public final class Constant {

    private Constant() {}

    // ===== 物品类型 =====
    /** 失物 */
    public static final int ITEM_TYPE_LOST = 0;
    /** 招领 */
    public static final int ITEM_TYPE_FOUND = 1;

    // ===== 物品状态 =====
    /** 待审核 */
    public static final int ITEM_STATUS_PENDING = 0;
    /** 已发布 */
    public static final int ITEM_STATUS_PUBLISHED = 1;
    /** 审核不通过 */
    public static final int ITEM_STATUS_REJECTED = 2;
    /** 已解决 */
    public static final int ITEM_STATUS_RESOLVED = 3;

    // ===== 用户角色 =====
    /** 普通用户 */
    public static final int ROLE_USER = 0;
    /** 管理员 */
    public static final int ROLE_ADMIN = 1;

    // ===== 用户状态 =====
    /** 正常 */
    public static final int USER_STATUS_ENABLED = 1;
    /** 禁用 */
    public static final int USER_STATUS_DISABLED = 0;

    // ===== 通知类型 =====
    /** 匹配通知 */
    public static final int NOTIFY_TYPE_MATCH = 0;
    /** 评论通知 */
    public static final int NOTIFY_TYPE_COMMENT = 1;
    /** 审核通知 */
    public static final int NOTIFY_TYPE_AUDIT = 2;
    /** 系统公告 */
    public static final int NOTIFY_TYPE_SYSTEM = 3;

    // ===== 匹配记录状态 =====
    /** 待确认 */
    public static final int MATCH_STATUS_PENDING = 0;
    /** 已通知 */
    public static final int MATCH_STATUS_NOTIFIED = 1;
    /** 已确认 */
    public static final int MATCH_STATUS_CONFIRMED = 2;
    /** 已驳回 */
    public static final int MATCH_STATUS_REJECTED = 3;

    // ===== AI审核结果 =====
    /** 通过 */
    public static final int AUDIT_PASS = 0;
    /** 违规 */
    public static final int AUDIT_VIOLATION = 1;

    // ===== 审核目标类型 =====
    /** 物品 */
    public static final int AUDIT_TARGET_ITEM = 0;
    /** 评论 */
    public static final int AUDIT_TARGET_COMMENT = 1;

    // ===== 匹配阈值 =====
    public static final double MATCH_THRESHOLD = 60.0;

    // ===== Redis Key前缀 =====
    public static final String REDIS_TOKEN_PREFIX = "token:";
    public static final String REDIS_USER_PREFIX = "user:";

    // ===== 分页默认值 =====
    public static final int PAGE_DEFAULT = 1;
    public static final int SIZE_DEFAULT = 10;
    public static final int SIZE_MAX = 50;
}
