package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.MatchingService;
import com.campus.lostfound.service.ai.AiService;
import com.campus.lostfound.service.ai.AiServiceFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI服务控制器
 */
@Tag(name = "AI智能模块")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceFactory aiServiceFactory;
    private final MatchingService matchingService;

    private AiService ai() {
        return aiServiceFactory.getService();
    }

    @Operation(summary = "图片识别(分类)")
    @PostMapping("/recognize")
    public Result<Map<String, String>> recognize(
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        String category = ai().recognizeImage(body.get("imageUrl"));
        return Result.success(Map.of("category", category != null ? category : "其他"));
    }

    @Operation(summary = "图片描述生成")
    @PostMapping("/describe")
    public Result<Map<String, String>> describe(
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        String description = ai().describeImage(body.get("imageUrl"));
        return Result.success(Map.of("description", description != null ? description : ""));
    }

    @Operation(summary = "OCR文字识别")
    @PostMapping("/ocr")
    public Result<Map<String, String>> ocr(
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        String text = ai().ocr(body.get("imageUrl"));
        return Result.success(Map.of("text", text != null ? text : ""));
    }

    @Operation(summary = "图文联合理解")
    @PostMapping("/enhance")
    public Result<Map<String, String>> enhance(
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        String imageUrl = body.get("imageUrl");
        String userText = body.getOrDefault("userText", "");
        String enhanced = ai().enhanceDescription(imageUrl, userText);
        return Result.success(Map.of("enhanced", enhanced != null ? enhanced : userText));
    }

    @Operation(summary = "内容审核")
    @PostMapping("/audit")
    public Result<Map<String, String>> audit(
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        String result = ai().auditContent(body.get("content"));
        return Result.success(Map.of("result", result != null ? result : "PASS"));
    }

    @Operation(summary = "触发智能匹配")
    @PostMapping("/match/{itemId}")
    public Result<?> triggerMatch(
            @PathVariable Long itemId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        checkLogin(userId);
        matchingService.triggerMatch(itemId);
        return Result.success("匹配任务已触发，请稍后查看结果");
    }

    private void checkLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}
