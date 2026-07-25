package com.campus.lostfound.controller;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.response.ItemListResponse;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "收藏模块")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏物品")
    @PostMapping("/{itemId}")
    public Result<?> addFavorite(
            @PathVariable Long itemId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        favoriteService.addFavorite(userId, itemId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{itemId}")
    public Result<?> removeFavorite(
            @PathVariable Long itemId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        favoriteService.removeFavorite(userId, itemId);
        return Result.success();
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/check/{itemId}")
    public Result<Map<String, Boolean>> checkFavorite(
            @PathVariable Long itemId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        boolean favorited = userId != null && favoriteService.isFavorited(userId, itemId);
        return Result.success(Map.of("favorited", favorited));
    }

    @Operation(summary = "我的收藏")
    @GetMapping
    public Result<PageResult<ItemListResponse>> myFavorites(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        checkLogin(userId);
        return Result.success(favoriteService.getMyFavorites(userId, page, size));
    }

    private void checkLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
