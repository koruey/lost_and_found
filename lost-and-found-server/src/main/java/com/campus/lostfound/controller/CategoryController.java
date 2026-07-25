package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.Category;
import com.campus.lostfound.mapper.CategoryMapper;
import com.campus.lostfound.utils.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final RedisUtil redisUtil;

    private static final String CACHE_KEY = "categories:all";
    private static final long CACHE_TTL = 30; // 分钟

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<Category>> listCategories() {
        // 尝试从Redis获取
        @SuppressWarnings("unchecked")
        List<Category> cached = (List<Category>) redisUtil.get(CACHE_KEY);
        if (cached != null) {
            return Result.success(cached);
        }

        // 查询数据库
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));

        // 写入Redis缓存
        redisUtil.set(CACHE_KEY, (java.io.Serializable) categories, CACHE_TTL, TimeUnit.MINUTES);

        return Result.success(categories);
    }
}
