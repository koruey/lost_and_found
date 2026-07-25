package com.campus.lostfound.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.Constant;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.dto.request.ItemPublishRequest;
import com.campus.lostfound.dto.request.ItemQueryRequest;
import com.campus.lostfound.dto.response.ItemDetailResponse;
import com.campus.lostfound.dto.response.ItemListResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.*;
import com.campus.lostfound.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 物品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;
    private final ItemImageMapper itemImageMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Long publishItem(ItemPublishRequest request, Long userId) {
        // 1. 保存物品信息
        Item item = new Item();
        item.setUserId(userId);
        item.setType(request.getType());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategoryId(request.getCategoryId());
        item.setLocation(request.getLocation());
        item.setItemDate(request.getItemDate());
        item.setContact(request.getContact() != null ? request.getContact() : "");
        item.setStatus(Constant.ITEM_STATUS_PUBLISHED); // 直接发布，后续可改为待审核
        item.setAiCategory("");
        item.setAiDescription("");
        item.setAiOcrText("");
        item.setViewCount(0);
        itemMapper.insert(item);

        // 2. 保存图片
        List<String> images = request.getImages();
        for (int i = 0; i < images.size(); i++) {
            ItemImage image = new ItemImage();
            image.setItemId(item.getId());
            image.setUrl(images.get(i));
            image.setSortOrder(i);
            itemImageMapper.insert(image);
        }

        log.info("物品发布成功: id={}, type={}, userId={}", item.getId(), request.getType(), userId);
        return item.getId();
    }

    @Override
    public ItemDetailResponse getItemDetail(Long itemId, Long currentUserId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }

        // 增加浏览次数
        item.setViewCount(item.getViewCount() + 1);
        itemMapper.updateById(item);

        // 构建详情响应
        ItemDetailResponse detail = new ItemDetailResponse();
        BeanUtil.copyProperties(item, detail);

        // 分类名称
        Category category = categoryMapper.selectById(item.getCategoryId());
        detail.setCategoryName(category != null ? category.getName() : "");

        // 图片列表
        List<ItemImage> images = itemImageMapper.selectList(
                new LambdaQueryWrapper<ItemImage>()
                        .eq(ItemImage::getItemId, itemId)
                        .orderByAsc(ItemImage::getSortOrder));
        detail.setImages(images.stream().map(ItemImage::getUrl).collect(Collectors.toList()));

        // 发布者信息
        User publisher = userMapper.selectById(item.getUserId());
        if (publisher != null) {
            detail.setUserNickname(publisher.getNickname());
            detail.setUserAvatar(publisher.getAvatarUrl());
        }

        // 评论数
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getItemId, itemId)
                        .eq(Comment::getStatus, 0));
        detail.setCommentCount(commentCount.intValue());

        // 当前用户是否已收藏
        detail.setIsFavorited(false);
        if (currentUserId != null) {
            Long count = favoriteMapper.selectCount(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, currentUserId)
                            .eq(Favorite::getItemId, itemId));
            detail.setIsFavorited(count > 0);
        }

        return detail;
    }

    @Override
    public PageResult<ItemListResponse> queryItems(ItemQueryRequest request) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();

        // 类型筛选
        if (request.getType() != null) {
            wrapper.eq(Item::getType, request.getType());
        }

        // 分类筛选
        if (request.getCategoryId() != null) {
            wrapper.eq(Item::getCategoryId, request.getCategoryId());
        }

        // 地点筛选（模糊匹配）
        if (StrUtil.isNotBlank(request.getLocation())) {
            wrapper.like(Item::getLocation, request.getLocation());
        }

        // 时间范围筛选
        if (StrUtil.isNotBlank(request.getStartDate())) {
            wrapper.ge(Item::getItemDate, request.getStartDate());
        }
        if (StrUtil.isNotBlank(request.getEndDate())) {
            wrapper.le(Item::getItemDate, request.getEndDate());
        }

        // 只查询已发布的物品
        wrapper.eq(Item::getStatus, Constant.ITEM_STATUS_PUBLISHED);

        // 关键词搜索（标题+描述）
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(Item::getTitle, request.getKeyword())
                    .or()
                    .like(Item::getDescription, request.getKeyword()));
        }

        // 排序
        String sortBy = request.getSortBy();
        if ("view_count".equals(sortBy)) {
            wrapper.orderByDesc(Item::getViewCount);
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(Item::getCreatedAt);
        }

        // 分页
        Page<Item> page = new Page<>(request.getPage(), request.getSize());
        Page<Item> result = itemMapper.selectPage(page, wrapper);

        // 转换VO
        List<ItemListResponse> records = result.getRecords().stream().map(item -> {
            ItemListResponse resp = new ItemListResponse();
            BeanUtil.copyProperties(item, resp);

            // 分类名
            Category category = categoryMapper.selectById(item.getCategoryId());
            resp.setCategoryName(category != null ? category.getName() : "");

            // 第一张图片
            List<ItemImage> images = itemImageMapper.selectList(
                    new LambdaQueryWrapper<ItemImage>()
                            .eq(ItemImage::getItemId, item.getId())
                            .orderByAsc(ItemImage::getSortOrder));
            resp.setFirstImage(images.isEmpty() ? "" : images.get(0).getUrl());

            // 发布者昵称
            User publisher = userMapper.selectById(item.getUserId());
            resp.setUserNickname(publisher != null ? publisher.getNickname() : "");

            // 评论数
            Long commentCount = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getItemId, item.getId())
                            .eq(Comment::getStatus, 0));
            resp.setCommentCount(commentCount.intValue());

            return resp;
        }).collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    @Transactional
    public void updateItem(Long itemId, ItemPublishRequest request, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ITEM_ACCESS_DENIED);
        }

        // 更新基本信息
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategoryId(request.getCategoryId());
        item.setLocation(request.getLocation());
        item.setItemDate(request.getItemDate());
        item.setContact(request.getContact() != null ? request.getContact() : "");
        itemMapper.updateById(item);

        // 更新图片（先删再增）
        itemImageMapper.delete(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, itemId));
        List<String> images = request.getImages();
        for (int i = 0; i < images.size(); i++) {
            ItemImage image = new ItemImage();
            image.setItemId(itemId);
            image.setUrl(images.get(i));
            image.setSortOrder(i);
            itemImageMapper.insert(image);
        }
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ITEM_ACCESS_DENIED);
        }

        // 删除图片记录
        itemImageMapper.delete(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, itemId));
        // 删除收藏
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().eq(Favorite::getItemId, itemId));
        // 删除评论
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getItemId, itemId));
        // 删除物品
        itemMapper.deleteById(itemId);
    }

    @Override
    public void resolveItem(Long itemId, Long userId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ITEM_ACCESS_DENIED);
        }
        item.setStatus(Constant.ITEM_STATUS_RESOLVED);
        itemMapper.updateById(item);
    }

    @Override
    public PageResult<ItemListResponse> getMyItems(Long userId, Integer type, Integer page, Integer size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<Item>()
                .eq(Item::getUserId, userId)
                .orderByDesc(Item::getCreatedAt);

        if (type != null) {
            wrapper.eq(Item::getType, type);
        }

        Page<Item> pageResult = itemMapper.selectPage(new Page<>(page, size), wrapper);

        List<ItemListResponse> records = pageResult.getRecords().stream().map(item -> {
            ItemListResponse resp = new ItemListResponse();
            BeanUtil.copyProperties(item, resp);

            Category category = categoryMapper.selectById(item.getCategoryId());
            resp.setCategoryName(category != null ? category.getName() : "");

            List<ItemImage> images = itemImageMapper.selectList(
                    new LambdaQueryWrapper<ItemImage>()
                            .eq(ItemImage::getItemId, item.getId())
                            .orderByAsc(ItemImage::getSortOrder));
            resp.setFirstImage(images.isEmpty() ? "" : images.get(0).getUrl());

            return resp;
        }).collect(Collectors.toList());

        return PageResult.of(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), records);
    }
}
