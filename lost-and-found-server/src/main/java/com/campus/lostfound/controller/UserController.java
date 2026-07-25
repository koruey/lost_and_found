package com.campus.lostfound.controller;

import com.campus.lostfound.common.Result;
import com.campus.lostfound.dto.request.LoginRequest;
import com.campus.lostfound.dto.response.LoginResponse;
import com.campus.lostfound.service.UserService;
import com.campus.lostfound.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "微信登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestAttribute("userId") Long userId) {
        UserVO userVO = userService.getUserVO(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/info")
    public Result<?> updateUserInfo(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> params) {
        userService.updateUser(
                userId,
                params.get("nickname"),
                params.get("avatarUrl"),
                params.get("phone"));
        return Result.success();
    }
}
