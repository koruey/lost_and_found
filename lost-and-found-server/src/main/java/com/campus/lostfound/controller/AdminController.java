package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.common.Constant;
import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.AuditRequest;
import com.campus.lostfound.dto.response.LoginResponse;
import com.campus.lostfound.dto.response.MatchResponse;
import com.campus.lostfound.dto.response.StatisticsResponse;
import com.campus.lostfound.entity.*;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.*;
import com.campus.lostfound.service.AnnouncementService;
import com.campus.lostfound.utils.JwtUtil;
import com.campus.lostfound.utils.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理后台")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final AnnouncementService announcementService;
    private final CategoryMapper categoryMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Value("${admin.password}")
    private String adminPassword;

    // ===== 管理员登录 =====

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<LoginResponse> adminLogin(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (password == null || !password.equals(adminPassword)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "管理员密码错误");
        }

        // 查找或创建管理员用户
        User adminUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getRole, Constant.ROLE_ADMIN));
        if (adminUser == null) {
            try {
                adminUser = new User();
                adminUser.setOpenid("admin_user");
                adminUser.setNickname("管理员");
                adminUser.setRole(Constant.ROLE_ADMIN);
                adminUser.setStatus(1);
                userMapper.insert(adminUser);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发场景下其他线程已创建，重新查询
                adminUser = userMapper.selectOne(
                        new LambdaQueryWrapper<User>().eq(User::getRole, Constant.ROLE_ADMIN));
            }
        }

        // 生成JWT
        String token = jwtUtil.generateToken(adminUser.getId());
        // 缓存token和角色
        redisUtil.set(Constant.REDIS_TOKEN_PREFIX + adminUser.getId(), token,
                jwtUtil.parseToken(token).getExpiration().getTime() - System.currentTimeMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        redisUtil.set(Constant.REDIS_USER_PREFIX + adminUser.getId() + ":role",
                String.valueOf(Constant.ROLE_ADMIN));

        return Result.success(LoginResponse.builder()
                .token(token)
                .userId(adminUser.getId())
                .nickname(adminUser.getNickname())
                .avatarUrl(adminUser.getAvatarUrl())
                .role(Constant.ROLE_ADMIN)
                .build());
    }

    // ===== 用户管理 =====

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<PageResult<User>> userList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<User> result = userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        // 脱敏
        result.getRecords().forEach(u -> u.setOpenid(u.getOpenid().substring(0, Math.min(8, u.getOpenid().length())) + "***"));
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{id}/status")
    public Result<?> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return Result.success();
    }

    // ===== 物品审核 =====

    @Operation(summary = "待审核物品列表")
    @GetMapping("/items/pending")
    public Result<PageResult<Item>> pendingItems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Item> result = itemMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getStatus, Constant.ITEM_STATUS_PENDING)
                        .orderByDesc(Item::getCreatedAt));
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "审核物品")
    @PutMapping("/items/{id}/audit")
    public Result<?> auditItem(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        Item item = itemMapper.selectById(id);
        if (item == null) throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        if (item.getStatus() != Constant.ITEM_STATUS_PENDING) {
            throw new BusinessException(ErrorCode.AUDIT_ALREADY_DONE);
        }
        item.setStatus(request.getStatus());
        itemMapper.updateById(item);
        return Result.success();
    }

    // ===== 评论管理 =====

    @Operation(summary = "评论列表")
    @GetMapping("/comments")
    public Result<PageResult<Comment>> commentList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Comment> result = commentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt));
        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        comment.setStatus(1);
        commentMapper.updateById(comment);
        return Result.success();
    }

    // ===== 公告管理 =====

    @Operation(summary = "发布公告")
    @PostMapping("/announcements")
    public Result<Map<String, Long>> createAnnouncement(@RequestBody Map<String, String> body) {
        announcementService.createAnnouncement(body.get("title"), body.get("content"));
        return Result.success(Map.of());
    }

    @Operation(summary = "编辑公告")
    @PutMapping("/announcements/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Map<String, String> body) {
        announcementService.updateAnnouncement(id, body.get("title"), body.get("content"));
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/announcements/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @Operation(summary = "切换公告显示/隐藏")
    @PutMapping("/announcements/{id}/status")
    public Result<?> toggleAnnouncementStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        announcementService.toggleStatus(id, body.get("status"));
        return Result.success();
    }

    @Operation(summary = "公告列表(含隐藏)")
    @GetMapping("/announcements")
    public Result<List<Announcement>> announcementList() {
        List<Announcement> list = announcementService.getAllAnnouncements();
        return Result.success(list);
    }

    // ===== 匹配记录 =====

    @Operation(summary = "物品匹配记录")
    @GetMapping("/items/{itemId}/matches")
    public Result<List<MatchResponse>> itemMatches(@PathVariable Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);

        List<MatchRecord> records;
        if (item.getType() == Constant.ITEM_TYPE_LOST) {
            records = matchRecordMapper.selectList(
                    new LambdaQueryWrapper<MatchRecord>()
                            .eq(MatchRecord::getLostItemId, itemId)
                            .orderByDesc(MatchRecord::getTotalScore));
        } else {
            records = matchRecordMapper.selectList(
                    new LambdaQueryWrapper<MatchRecord>()
                            .eq(MatchRecord::getFoundItemId, itemId)
                            .orderByDesc(MatchRecord::getTotalScore));
        }

        List<MatchResponse> responses = records.stream().map(r -> {
            MatchResponse resp = new MatchResponse();
            resp.setId(r.getId());
            resp.setTotalScore(r.getTotalScore());
            resp.setImageScore(r.getImageScore());
            resp.setTextScore(r.getTextScore());
            resp.setOcrScore(r.getOcrScore());
            resp.setCategoryScore(r.getCategoryScore());
            resp.setLocationScore(r.getLocationScore());
            resp.setTimeScore(r.getTimeScore());
            resp.setReason(r.getReason());
            resp.setCreatedAt(r.getCreatedAt());

            // 找到匹配的物品
            Long matchedItemId = item.getType() == Constant.ITEM_TYPE_LOST
                    ? r.getFoundItemId() : r.getLostItemId();
            Item matched = itemMapper.selectById(matchedItemId);
            if (matched != null) {
                resp.setItemId(matched.getId());
                resp.setItemTitle(matched.getTitle());
                resp.setItemType(matched.getType() == Constant.ITEM_TYPE_LOST ? "失物" : "招领");
                List<ItemImage> images = new ArrayList<>();
                // 简单获取第一张图
                resp.setFirstImage("");
            }

            return resp;
        }).collect(Collectors.toList());

        return Result.success(responses);
    }

    @Operation(summary = "全部匹配记录")
    @GetMapping("/matches")
    public Result<PageResult<MatchResponse>> matchList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<MatchRecord> result = matchRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<MatchRecord>()
                        .orderByDesc(MatchRecord::getTotalScore));

        List<MatchResponse> responses = result.getRecords().stream().map(r -> {
            MatchResponse resp = new MatchResponse();
            resp.setId(r.getId());
            resp.setLostItemId(r.getLostItemId());
            resp.setFoundItemId(r.getFoundItemId());
            resp.setTotalScore(r.getTotalScore());
            resp.setImageScore(r.getImageScore());
            resp.setTextScore(r.getTextScore());
            resp.setOcrScore(r.getOcrScore());
            resp.setCategoryScore(r.getCategoryScore());
            resp.setLocationScore(r.getLocationScore());
            resp.setTimeScore(r.getTimeScore());
            resp.setReason(r.getReason());
            resp.setCreatedAt(r.getCreatedAt());

            Item lostItem = itemMapper.selectById(r.getLostItemId());
            if (lostItem != null) {
                resp.setLostItemTitle(lostItem.getTitle());
            }
            Item foundItem = itemMapper.selectById(r.getFoundItemId());
            if (foundItem != null) {
                resp.setFoundItemTitle(foundItem.getTitle());
            }
            return resp;
        }).collect(Collectors.toList());

        return Result.success(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), responses));
    }

    // ===== 数据统计 =====

    @Operation(summary = "数据统计")
    @GetMapping("/statistics")
    public Result<StatisticsResponse> statistics() {
        Long lostCount = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>().eq(Item::getType, Constant.ITEM_TYPE_LOST));
        Long foundCount = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>().eq(Item::getType, Constant.ITEM_TYPE_FOUND));
        Long resolvedCount = itemMapper.selectCount(
                new LambdaQueryWrapper<Item>().eq(Item::getStatus, Constant.ITEM_STATUS_RESOLVED));
        Long matchCount = matchRecordMapper.selectCount(
                new LambdaQueryWrapper<MatchRecord>().eq(MatchRecord::getStatus, Constant.MATCH_STATUS_CONFIRMED));
        Long userCount = userMapper.selectCount(null);

        // 分类统计
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
        for (Category cat : categories) {
            Long count = itemMapper.selectCount(
                    new LambdaQueryWrapper<Item>().eq(Item::getCategoryId, cat.getId()));
            if (count > 0) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", cat.getName());
                stat.put("count", count);
                categoryStats.add(stat);
            }
        }

        // 每日趋势（最近7天）
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = java.time.LocalDate.now().minusDays(i).toString();
            Long count = itemMapper.selectCount(
                    new LambdaQueryWrapper<Item>().apply("DATE(created_at) = {0}", date));
            Map<String, Object> trend = new HashMap<>();
            trend.put("date", date);
            trend.put("count", count);
            dailyTrend.add(trend);
        }

        return Result.success(StatisticsResponse.builder()
                .lostCount(lostCount)
                .foundCount(foundCount)
                .resolvedCount(resolvedCount)
                .matchCount(matchCount)
                .userCount(userCount)
                .categoryStats(categoryStats)
                .dailyTrend(dailyTrend)
                .build());
    }
}
