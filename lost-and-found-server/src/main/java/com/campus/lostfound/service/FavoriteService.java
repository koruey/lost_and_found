package com.campus.lostfound.service;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.dto.response.ItemListResponse;

public interface FavoriteService {

    void addFavorite(Long userId, Long itemId);

    void removeFavorite(Long userId, Long itemId);

    boolean isFavorited(Long userId, Long itemId);

    PageResult<ItemListResponse> getMyFavorites(Long userId, Integer page, Integer size);
}
