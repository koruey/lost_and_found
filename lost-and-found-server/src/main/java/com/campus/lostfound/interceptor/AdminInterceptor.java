package com.campus.lostfound.interceptor;

import com.campus.lostfound.common.Constant;
import com.campus.lostfound.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员权限拦截器
 * 校验当前用户是否为管理员
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return false;
        }

        // 从Redis获取用户角色
        String roleKey = Constant.REDIS_USER_PREFIX + userId + ":role";
        Object roleObj = redisTemplate.opsForValue().get(roleKey);
        Integer role = roleObj != null ? Integer.parseInt(roleObj.toString()) : null;

        if (role == null || role != Constant.ROLE_ADMIN) {
            log.warn("非管理员访问: userId={}", userId);
            writeErrorResponse(response, ErrorCode.ADMIN_REQUIRED);
            return false;
        }

        return true;
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        Map<String, Object> result = new HashMap<>();
        result.put("code", errorCode.getCode());
        result.put("message", errorCode.getMessage());
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
