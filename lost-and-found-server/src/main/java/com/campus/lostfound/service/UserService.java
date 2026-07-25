package com.campus.lostfound.service;

import com.campus.lostfound.dto.request.LoginRequest;
import com.campus.lostfound.dto.response.LoginResponse;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.vo.UserVO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 微信登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据ID获取用户
     */
    User getById(Long userId);

    /**
     * 获取用户VO
     */
    UserVO getUserVO(Long userId);

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, String nickname, String avatarUrl, String phone);
}
