package com.campus.lostfound.controller;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.ItemPublishRequest;
import com.campus.lostfound.dto.request.ItemQueryRequest;
import com.campus.lostfound.dto.response.ItemDetailResponse;
import com.campus.lostfound.dto.response.ItemListResponse;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "物品模块")
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "发布物品")
    @PostMapping
    public Result<Map<String, Long>> publish(
            @Valid @RequestBody ItemPublishRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        Long itemId = itemService.publishItem(request, userId);
        return Result.success(Map.of("id", itemId));
    }

    @Operation(summary = "获取物品详情")
    @GetMapping("/{id}")
    public Result<ItemDetailResponse> detail(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        return Result.success(itemService.getItemDetail(id, userId));
    }

    @Operation(summary = "查询物品列表")
    @GetMapping
    public Result<PageResult<ItemListResponse>> list(ItemQueryRequest request) {
        return Result.success(itemService.queryItems(request));
    }

    @Operation(summary = "编辑物品")
    @PutMapping("/{id}")
    public Result<?> update(
            @PathVariable Long id,
            @Valid @RequestBody ItemPublishRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        itemService.updateItem(id, request, userId);
        return Result.success();
    }

    @Operation(summary = "删除物品")
    @DeleteMapping("/{id}")
    public Result<?> delete(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        itemService.deleteItem(id, userId);
        return Result.success();
    }

    @Operation(summary = "标记已解决")
    @PutMapping("/{id}/resolved")
    public Result<?> resolve(
            @PathVariable Long id,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        itemService.resolveItem(id, userId);
        return Result.success();
    }

    @Operation(summary = "我的物品")
    @GetMapping("/my")
    public Result<PageResult<ItemListResponse>> myItems(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        checkLogin(userId);
        return Result.success(itemService.getMyItems(userId, type, page, size));
    }

    private void checkLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
