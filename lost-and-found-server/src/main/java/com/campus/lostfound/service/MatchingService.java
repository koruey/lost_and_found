package com.campus.lostfound.service;

/**
 * 智能匹配服务
 */
public interface MatchingService {

    /**
     * 对新发布的物品执行智能匹配
     * @param itemId 物品ID
     */
    void matchItem(Long itemId);

    /**
     * 手动触发匹配（管理员/用户）
     * @param itemId 物品ID
     */
    void triggerMatch(Long itemId);
}
