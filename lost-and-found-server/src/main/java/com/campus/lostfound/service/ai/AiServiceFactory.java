package com.campus.lostfound.service.ai;

import com.campus.lostfound.config.AiProperties;
import com.campus.lostfound.service.ai.impl.ClaudeServiceImpl;
import com.campus.lostfound.service.ai.impl.OpenAiServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI服务工厂 - 根据配置选择提供商
 */
@Component
@RequiredArgsConstructor
public class AiServiceFactory {

    private final OpenAiServiceImpl openAiService;
    private final ClaudeServiceImpl claudeService;
    private final AiProperties aiProperties;

    /**
     * 获取当前配置的AI服务
     */
    public AiService getService() {
        String provider = aiProperties.getProvider();
        return switch (provider.toLowerCase()) {
            case "claude" -> claudeService;
            case "openai", "deepseek", "qwen" -> openAiService; // 都使用OpenAI兼容API
            default -> openAiService;
        };
    }
}
