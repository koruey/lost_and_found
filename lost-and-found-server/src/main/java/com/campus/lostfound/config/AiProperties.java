package com.campus.lostfound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** AI提供商: openai / claude / deepseek / qwen */
    private String provider = "deepseek";

    private OpenAiConfig openai = new OpenAiConfig();
    private ClaudeConfig claude = new ClaudeConfig();
    private DeepSeekConfig deepseek = new DeepSeekConfig();
    private QwenConfig qwen = new QwenConfig();

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o";
    }

    @Data
    public static class ClaudeConfig {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com";
        private String model = "claude-sonnet-5-20251001";
    }

    @Data
    public static class DeepSeekConfig {
        private String apiKey;
        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
    }

    @Data
    public static class QwenConfig {
        private String apiKey;
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen-vl-max";
    }
}
