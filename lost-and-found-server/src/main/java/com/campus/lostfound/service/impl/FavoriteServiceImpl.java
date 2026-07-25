package com.campus.lostfound.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.dto.response.ItemListResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.*;
import com.campus.lostfound.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void addFavorite(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getItemId, itemId));
        if (count > 0) {
            return; // 已收藏，幂等处理
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long itemId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId));
    }

    @Override
    public boolean isFavorited(Long userId, Long itemId) {
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getItemId, itemId));
        return count > 0;
    }

    @Override
    public PageResult<ItemListResponse> getMyFavorites(Long userId, Integer page, Integer size) {
        Page<Favorite> favPage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreatedAt));

        List<ItemListResponse> records = new ArrayList<>();
        for (Favorite fav : favPage.getRecords()) {
            Item item = itemMapper.selectById(fav.getItemId());
            if (item == null) continue;

            ItemListResponse resp = new ItemListResponse();
            BeanUtil.copyProperties(item, resp);

            Category category = categoryMapper.selectById(item.getCategoryId());
            resp.setCategoryName(category != null ? category.getName() : "");

            List<ItemImage> images = itemImageMapper.selectList(
                    new LambdaQueryWrapper<ItemImage>()
                            .eq(ItemImage::getItemId, item.getId())
                            .orderByAsc(ItemImage::getSortOrder));
            resp.setFirstImage(images.isEmpty() ? "" : images.get(0).getUrl());

            User publisher = userMapper.selectById(item.getUserId());
            resp.setUserNickname(publisher != null ? publisher.getNickname() : "");

            records.add(resp);
        }
        return PageResult.of(favPage.getTotal(), favPage.getCurrent(), favPage.getSize(), records);
    }
}
