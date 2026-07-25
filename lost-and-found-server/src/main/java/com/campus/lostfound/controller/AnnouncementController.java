package com.campus.lostfound.controller;

import com.campus.lostfound.common.PageResult;
import com.campus.lostfound.common.Result;
import com.campus.lostfound.entity.Announcement;
import com.campus.lostfound.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 公告公开接口控制器
 */
@Tag(name = "公告模块")
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "公告列表")
    @GetMapping
    public Result<PageResult<Announcement>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        var result = announcementService.getAnnouncements(page, size);
        return Result.success(PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        return Result.success(announcement);
    }
}
