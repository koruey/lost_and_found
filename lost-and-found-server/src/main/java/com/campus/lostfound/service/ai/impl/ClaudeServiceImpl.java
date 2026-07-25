package com.campus.lostfound.service.ai.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.campus.lostfound.config.AiProperties;
import com.campus.lostfound.service.ai.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Claude API实现（Anthropic Messages API）
 * Claude使用不同于OpenAI的API格式
 */
@Slf4j
@Service
public class ClaudeServiceImpl implements AiService {

    private final AiProperties aiProperties;
    private final HttpClient httpClient;

    public ClaudeServiceImpl(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.httpClient = HttpClient.newHttpClient();
    }

    private AiProperties.ClaudeConfig config() {
        return aiProperties.getClaude();
    }

    /**
     * 调用Claude Messages API (支持图片)
     */
    private String messageWithVision(String systemPrompt, String userPrompt, String imageUrl) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", config().getModel());
            body.set("max_tokens", 1000);
            body.set("system", systemPrompt);

            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");

            JSONArray contentParts = new JSONArray();

            // Image part (if provided)
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // 下载图片并转为base64
                String base64Image = downloadImageAsBase64(imageUrl);
                if (base64Image != null) {
                    JSONObject imagePart = new JSONObject();
                    imagePart.set("type", "image");
                    JSONObject source = new JSONObject();
                    source.set("type", "base64");
                    source.set("media_type", "image/jpeg");
                    source.set("data", base64Image);
                    imagePart.set("source", source);
                    contentParts.add(imagePart);
                }
            }

            // Text part
            JSONObject textPart = new JSONObject();
            textPart.set("type", "text");
            textPart.set("text", userPrompt);
            contentParts.add(textPart);

            userMsg.set("content", contentParts);
            messages.add(userMsg);
            body.set("messages", messages);

            String apiUrl = config().getBaseUrl() + "/v1/messages";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", config().getApiKey())
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject result = JSONUtil.parseObj(response.body());

            if (result.containsKey("error")) {
                log.error("Claude API调用失败: {}", result.getJSONObject("error").getStr("message"));
                return null;
            }

            JSONArray content = result.getJSONArray("content");
            if (content != null && !content.isEmpty()) {
                return content.getJSONObject(0).getStr("text");
            }

            return null;
        } catch (Exception e) {
            log.error("Claude API调用异常", e);
            return null;
        }
    }

    private String message(String systemPrompt, String userPrompt) {
        return messageWithVision(systemPrompt, userPrompt, null);
    }

    private String downloadImageAsBase64(String imageUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return java.util.Base64.getEncoder().encodeToString(response.body());
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return null;
        }
    }

    // ===== AiService 接口实现 =====

    @Override
    public String recognizeImage(String imageUrl) {
        return messageWithVision(
                "你是一个物品识别专家。请识别图片中的物品类别。只需要回复分类名称。",
                "请识别图片中的物品类别（手机/耳机/钥匙/校园卡/钱包/书籍/雨伞/电脑/眼镜/水杯/身份证/衣物/背包/文具/其他）",
                imageUrl);
    }

    @Override
    public String describeImage(String imageUrl) {
        return messageWithVision(
                "详细描述图片中物品的外观特征（颜色、品牌、材质、大小等），用2-3句话，不要添加前缀。",
                "请描述这张图片中的物品。",
                imageUrl);
    }

    @Override
    public String ocr(String imageUrl) {
        return messageWithVision(
                "识别并输出图片中所有文字。没有文字则回复'未检测到文字'。只输出文字，不要解释。",
                "请识别图片中的文字。",
                imageUrl);
    }

    @Override
    public String enhanceDescription(String imageUrl, String userText) {
        return messageWithVision(
                "结合图片和用户描述，生成完整规范的失物/招领描述。包括物品名、颜色、品牌、特征等。控制在100字以内。",
                "用户描述：" + userText + "\n请生成完整描述。",
                imageUrl);
    }

    @Override
    public String auditContent(String content) {
        return message(
                "审核以下内容是否违规（广告/色情/辱骂/垃圾/敏感）。正常回复'PASS'，违规回复'REJECT: 原因'。",
                "审核内容：\n" + content);
    }

    @Override
    public List<Double> getTextEmbedding(String text) {
        // Claude 不提供独立的 Embedding API
        // 对于文本匹配，直接使用文本相似度比较
        log.warn("Claude不支持Embedding API，返回空向量");
        return List.of();
    }
}
