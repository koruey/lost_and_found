-- ============================================
-- 校园失物招领系统 - 数据库初始化脚本
-- MySQL 8.0+
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `lost_and_found`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `lost_and_found`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid`        VARCHAR(64)  NOT NULL COMMENT '微信OpenID',
    `nickname`      VARCHAR(64)  DEFAULT '' COMMENT '用户昵称',
    `avatar_url`    VARCHAR(512) DEFAULT '' COMMENT '头像URL',
    `phone`         VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    `role`          TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员账号（通过 admin.html 使用密码 admin123 登录，首次登录自动创建）
-- 或者手动执行以下SQL预设管理员：
-- INSERT INTO `user` (`openid`, `nickname`, `role`, `status`) VALUES ('admin_user', '管理员', 1, 1);

-- ============================================
-- 2. 分类字典表
-- ============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`       VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon`       VARCHAR(200) DEFAULT '' COMMENT '图标标识',
    `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类字典表';

INSERT INTO `category` (`name`, `sort_order`) VALUES
('手机', 1), ('耳机', 2), ('钥匙', 3), ('校园卡', 4),
('钱包', 5), ('书籍', 6), ('雨伞', 7), ('电脑', 8),
('眼镜', 9), ('水杯', 10), ('身份证', 11), ('衣物', 12),
('背包', 13), ('文具', 14), ('其他', 15);

-- ============================================
-- 3. 物品信息表
-- ============================================
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id`         BIGINT       NOT NULL COMMENT '发布者ID',
    `type`            TINYINT      NOT NULL COMMENT '类型: 0-失物, 1-招领',
    `title`           VARCHAR(100) NOT NULL COMMENT '标题',
    `description`     TEXT         NOT NULL COMMENT '物品描述',
    `category_id`     INT          NOT NULL COMMENT '分类ID',
    `location`        VARCHAR(200) NOT NULL COMMENT '丢失/拾取地点',
    `item_date`       DATE         NOT NULL COMMENT '丢失/拾取日期',
    `contact`         VARCHAR(100) DEFAULT '' COMMENT '联系方式',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已发布, 2-审核不通过, 3-已解决',
    `ai_category`     VARCHAR(50)  DEFAULT '' COMMENT 'AI识别的分类',
    `ai_description`  TEXT         DEFAULT NULL COMMENT 'AI生成的增强描述',
    `ai_ocr_text`     TEXT         DEFAULT NULL COMMENT 'OCR识别文字',
    `ai_features`     JSON         DEFAULT NULL COMMENT 'AI提取的特征标签',
    `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_location` (`location`),
    KEY `idx_item_date` (`item_date`),
    KEY `idx_created_at` (`created_at`),
    FULLTEXT KEY `ft_title_desc` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品信息表';

-- ============================================
-- 4. 物品图片表
-- ============================================
DROP TABLE IF EXISTS `item_image`;
CREATE TABLE `item_image` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `item_id`       BIGINT       NOT NULL COMMENT '物品ID',
    `url`           VARCHAR(512) NOT NULL COMMENT '图片URL',
    `thumbnail_url` VARCHAR(512) DEFAULT '' COMMENT '缩略图URL',
    `sort_order`    INT          NOT NULL DEFAULT 0 COMMENT '排序序号',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品图片表';

-- ============================================
-- 5. 收藏表
-- ============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `item_id`    BIGINT   NOT NULL COMMENT '物品ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item_id` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================
-- 6. 评论表
-- ============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `item_id`    BIGINT       NOT NULL COMMENT '物品ID',
    `user_id`    BIGINT       NOT NULL COMMENT '评论者ID',
    `content`          VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id`        BIGINT       NULL COMMENT '父评论ID(回复时使用)',
    `reply_to_user_id` BIGINT       NULL COMMENT '被回复用户ID',
    `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-正常, 1-已删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================
-- 7. 消息通知表
-- ============================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`    BIGINT       NOT NULL COMMENT '接收用户ID',
    `type`       TINYINT      NOT NULL COMMENT '类型: 0-匹配, 1-评论, 2-审核, 3-系统',
    `title`      VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content`    TEXT         NOT NULL COMMENT '通知内容',
    `related_id` BIGINT       DEFAULT NULL COMMENT '关联物品/评论ID',
    `is_read`    TINYINT      NOT NULL DEFAULT 0 COMMENT '已读: 0-未读, 1-已读',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ============================================
-- 8. 公告表
-- ============================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`      VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`    TEXT         NOT NULL COMMENT '公告内容',
    `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0-隐藏, 1-显示',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================
-- 9. 智能匹配记录表
-- ============================================
DROP TABLE IF EXISTS `match_record`;
CREATE TABLE `match_record` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `lost_item_id`   BIGINT       NOT NULL COMMENT '失物ID',
    `found_item_id`  BIGINT       NOT NULL COMMENT '招领ID',
    `total_score`    DECIMAL(5,2) NOT NULL COMMENT '综合匹配度(0-100)',
    `image_score`    DECIMAL(5,2) DEFAULT 0 COMMENT '图片相似度',
    `text_score`     DECIMAL(5,2) DEFAULT 0 COMMENT '文本语义相似度',
    `ocr_score`      DECIMAL(5,2) DEFAULT 0 COMMENT 'OCR匹配度',
    `category_score` DECIMAL(5,2) DEFAULT 0 COMMENT '分类匹配度',
    `location_score` DECIMAL(5,2) DEFAULT 0 COMMENT '地点匹配度',
    `time_score`     DECIMAL(5,2) DEFAULT 0 COMMENT '时间匹配度',
    `reason`         TEXT         DEFAULT NULL COMMENT '匹配理由说明',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0-待确认, 1-已通知, 2-已确认, 3-已驳回',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_lost_item` (`lost_item_id`),
    KEY `idx_found_item` (`found_item_id`),
    KEY `idx_score` (`total_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能匹配记录表';

-- ============================================
-- 10. AI审核日志表
-- ============================================
DROP TABLE IF EXISTS `ai_audit_log`;
CREATE TABLE `ai_audit_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `target_type` TINYINT      NOT NULL COMMENT '目标类型: 0-物品, 1-评论',
    `target_id`   BIGINT       NOT NULL COMMENT '目标ID',
    `result`      TINYINT      NOT NULL COMMENT '审核结果: 0-通过, 1-违规',
    `reason`      VARCHAR(500) DEFAULT '' COMMENT '审核理由',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核日志表';

-- ============================================
-- 初始化完成
-- ============================================
SELECT '数据库初始化完成！' AS message;
SELECT TABLE_NAME, TABLE_COMMENT FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'lost_and_found' ORDER BY TABLE_NAME;
