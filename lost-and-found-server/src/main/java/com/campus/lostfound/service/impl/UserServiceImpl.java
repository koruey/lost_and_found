package com.campus.lostfound.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.Constant;
import com.campus.lostfound.config.WxProperties;
import com.campus.lostfound.dto.request.LoginRequest;
import com.campus.lostfound.dto.response.LoginResponse;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.mapper.UserMapper;
import com.campus.lostfound.service.UserService;
import com.campus.lostfound.utils.JwtUtil;
import com.campus.lostfound.utils.RedisUtil;
import com.campus.lostfound.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final WxProperties wxProperties;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 调用微信接口换取openid
        String openid = getOpenidFromWx(request.getCode());
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "获取微信openid失败");
        }

        // 2. 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            // 新用户注册
            user = new User();
            user.setOpenid(openid);
            user.setNickname(StrUtil.isNotBlank(request.getNickname())
                    ? request.getNickname() : "微信用户");
            user.setAvatarUrl(StrUtil.isNotBlank(request.getAvatarUrl())
                    ? request.getAvatarUrl() : "");
            user.setRole(Constant.ROLE_USER);
            user.setStatus(Constant.USER_STATUS_ENABLED);
            userMapper.insert(user);
            log.info("新用户注册: openid={}, nickname={}", openid, user.getNickname());
        } else {
            // 检查账号状态
            if (user.getStatus() == Constant.USER_STATUS_DISABLED) {
                throw new BusinessException(ErrorCode.USER_DISABLED);
            }
            // 更新昵称和头像（如果提供了新的）
            if (StrUtil.isNotBlank(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StrUtil.isNotBlank(request.getAvatarUrl())) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        // 3. 生成JWT
        String token = jwtUtil.generateToken(user.getId());

        // 4. 将Token存入Redis
        String tokenKey = Constant.REDIS_TOKEN_PREFIX + user.getId();
        redisUtil.set(tokenKey, token, 2, TimeUnit.HOURS);

        // 5. 缓存用户角色
        String roleKey = Constant.REDIS_USER_PREFIX + user.getId() + ":role";
        redisUtil.set(roleKey, user.getRole(), 2, TimeUnit.HOURS);

        // 6. 返回登录结果
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }

    @Override
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public UserVO getUserVO(Long userId) {
        User user = getById(userId);
        return UserVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .phone(maskPhone(user.getPhone()))
                .role(user.getRole())
                .build();
    }

    @Override
    public void updateUser(Long userId, String nickname, String avatarUrl, String phone) {
        User user = getById(userId);
        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(avatarUrl)) {
            user.setAvatarUrl(avatarUrl);
        }
        if (StrUtil.isNotBlank(phone)) {
            user.setPhone(phone);
        }
        userMapper.updateById(user);
    }

    /**
     * 调用微信接口换取openid
     */
    private String getOpenidFromWx(String code) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxProperties.getAppId(), wxProperties.getAppSecret(), code);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            JSONObject json = JSONUtil.parseObj(response.body());

            if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
                log.error("微信接口调用失败: errcode={}, errmsg={}",
                        json.getInt("errcode"), json.getStr("errmsg"));
                return null;
            }

            return json.getStr("openid");
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            return null;
        }
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
