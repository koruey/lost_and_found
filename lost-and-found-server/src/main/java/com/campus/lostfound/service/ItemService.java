package com.campus.lostfound.service;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.dto.request.ItemPublishRequest;
import com.campus.lostfound.dto.request.ItemQueryRequest;
import com.campus.lostfound.dto.response.ItemDetailResponse;
import com.campus.lostfound.dto.response.ItemListResponse;

/**
 * 物品服务
 */
public interface ItemService {

    /**
     * 发布物品
     */
    Long publishItem(ItemPublishRequest request, Long userId);

    /**
     * 获取物品详情
     */
    ItemDetailResponse getItemDetail(Long itemId, Long currentUserId);

    /**
     * 分页查询物品列表
     */
    PageResult<ItemListResponse> queryItems(ItemQueryRequest request);

    /**
     * 编辑物品
     */
    void updateItem(Long itemId, ItemPublishRequest request, Long userId);

    /**
     * 删除物品
     */
    void deleteItem(Long itemId, Long userId);

    /**
     * 标记物品已解决
     */
    void resolveItem(Long itemId, Long userId);

    /**
     * 查询我的发布
     */
    PageResult<ItemListResponse> getMyItems(Long userId, Integer type, Integer page, Integer size);
}
