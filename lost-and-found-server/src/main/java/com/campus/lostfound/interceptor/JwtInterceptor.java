package com.campus.lostfound.interceptor;

import com.campus.lostfound.common.Constant;
import com.campus.lostfound.utils.JwtUtil;
import com.campus.lostfound.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器（可选模式）
 * 有Token则解析并存入userId，无Token也放行
 * 需要登录的接口由Controller自行校验 @RequestAttribute 是否为null
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            // 验证Redis中的Token
            String redisKey = Constant.REDIS_TOKEN_PREFIX + userId;
            String redisToken = redisUtil.getString(redisKey);
            if (redisToken != null && redisToken.equals(token)) {
                request.setAttribute("userId", userId);
                log.debug("JWT认证成功: userId={}", userId);
            }
        }
        // 无论是否带Token，都放行
        return true;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
