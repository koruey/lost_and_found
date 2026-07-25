-- ============================================
-- 迁移：评论表添加回复功能字段
-- 执行方式：在 lost_and_found 数据库中执行
-- ============================================

ALTER TABLE `comment`
    ADD COLUMN `parent_id` BIGINT NULL COMMENT '父评论ID(回复时使用)' AFTER `content`,
    ADD COLUMN `reply_to_user_id` BIGINT NULL COMMENT '被回复用户ID' AFTER `parent_id`,
    ADD KEY `idx_parent_id` (`parent_id`);
