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
import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI兼容API实现
 * 支持: OpenAI、DeepSeek、通义千问(阿里云DashScope)
 * 这三个厂商都提供OpenAI兼容的API格式
 */
@Slf4j
@Service
public class OpenAiServiceImpl implements AiService {

    private final AiProperties aiProperties;
    private final HttpClient httpClient;

    public OpenAiServiceImpl(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.httpClient = HttpClient.newHttpClient();
    }

    // 获取当前配置的API参数
    private String getBaseUrl() {
        return switch (aiProperties.getProvider().toLowerCase()) {
            case "openai" -> aiProperties.getOpenai().getBaseUrl();
            case "deepseek" -> aiProperties.getDeepseek().getBaseUrl();
            case "qwen" -> aiProperties.getQwen().getBaseUrl();
            default -> aiProperties.getDeepseek().getBaseUrl();
        };
    }

    private String getApiKey() {
        return switch (aiProperties.getProvider().toLowerCase()) {
            case "openai" -> aiProperties.getOpenai().getApiKey();
            case "deepseek" -> aiProperties.getDeepseek().getApiKey();
            case "qwen" -> aiProperties.getQwen().getApiKey();
            default -> aiProperties.getDeepseek().getApiKey();
        };
    }

    private String getModel() {
        return switch (aiProperties.getProvider().toLowerCase()) {
            case "openai" -> aiProperties.getOpenai().getModel();
            case "deepseek" -> aiProperties.getDeepseek().getModel();
            case "qwen" -> aiProperties.getQwen().getModel();
            default -> aiProperties.getDeepseek().getModel();
        };
    }

    // ===== 核心API调用方法 =====

    /**
     * 调用Chat Completions API (支持图片)
     */
    private String chatWithVision(String systemPrompt, String userPrompt, String imageUrl) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", getModel());
            body.set("max_tokens", 1000);

            JSONArray messages = new JSONArray();

            // System message
            JSONObject sysMsg = new JSONObject();
            sysMsg.set("role", "system");
            sysMsg.set("content", systemPrompt);
            messages.add(sysMsg);

            // User message (text + image)
            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");

            JSONArray contentParts = new JSONArray();

            // Text part
            JSONObject textPart = new JSONObject();
            textPart.set("type", "text");
            textPart.set("text", userPrompt);
            contentParts.add(textPart);

            // Image part
            if (imageUrl != null && !imageUrl.isEmpty()) {
                JSONObject imagePart = new JSONObject();
                imagePart.set("type", "image_url");
                JSONObject imageUrlObj = new JSONObject();
                imageUrlObj.set("url", imageUrl);
                imagePart.set("image_url", imageUrlObj);
                contentParts.add(imagePart);
            }

            userMsg.set("content", contentParts);
            messages.add(userMsg);

            body.set("messages", messages);

            // 发送请求
            String apiUrl = getBaseUrl() + "/chat/completions";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject result = JSONUtil.parseObj(response.body());

            if (result.containsKey("error")) {
                log.error("AI API调用失败: {}", result.getJSONObject("error").getStr("message"));
                return null;
            }

            JSONArray choices = result.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                return choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
            }

            return null;
        } catch (Exception e) {
            log.error("AI API调用异常", e);
            return null;
        }
    }

    /**
     * 纯文本对话（不含图片）
     */
    private String chat(String systemPrompt, String userPrompt) {
        return chatWithVision(systemPrompt, userPrompt, null);
    }

    // ===== AiService接口实现 =====

    @Override
    public String recognizeImage(String imageUrl) {
        String systemPrompt = "你是一个物品识别专家。请识别图片中的物品属于以下哪个类别之一：" +
                "手机、耳机、钥匙、校园卡、钱包、书籍、雨伞、电脑、眼镜、水杯、身份证、衣物、背包、文具、其他。" +
                "只需要回复分类名称，不要解释。";

        String userPrompt = "请识别这张图片中的物品类别。";

        String result = chatWithVision(systemPrompt, userPrompt, imageUrl);
        return result != null ? result.trim() : "其他";
    }

    @Override
    public String describeImage(String imageUrl) {
        String systemPrompt = "你是一个物品描述专家。请详细描述图片中物品的外观特征，包括颜色、品牌、材质、大小、新旧程度等。" +
                "用2-3句话简洁描述，不要添加任何前缀说明。";

        String userPrompt = "请描述这张图片中的物品外观特征。";

        return chatWithVision(systemPrompt, userPrompt, imageUrl);
    }

    @Override
    public String ocr(String imageUrl) {
        String systemPrompt = "你是一个OCR文字识别专家。请识别并输出图片中所有的文字内容。" +
                "如果图片中没有文字，回复'未检测到文字'。只输出识别到的文字，不要添加任何解释。";

        String userPrompt = "请识别这张图片中的所有文字。";

        return chatWithVision(systemPrompt, userPrompt, imageUrl);
    }

    @Override
    public String enhanceDescription(String imageUrl, String userText) {
        String systemPrompt = "你是一个失物招领信息整理专家。请结合图片内容和用户的文字描述，" +
                "生成一份完整、规范的失物/招领描述。描述应包括：物品名称、颜色、品牌、外观特征、" +
                "可能的重要标识等。用简洁清晰的语言，控制在100字以内。不要添加'根据图片'等前缀。";

        String userPrompt = "用户描述：" + userText + "\n请结合图片，生成完整的物品描述。";

        return chatWithVision(systemPrompt, userPrompt, imageUrl);
    }

    @Override
    public String auditContent(String content) {
        String systemPrompt = "你是一个内容审核专家。请审核以下内容是否包含违规信息：" +
                "广告推广、色情低俗、辱骂攻击、垃圾信息、涉政敏感等。" +
                "如果内容正常，只回复'PASS'；如果违规，回复'REJECT: <违规原因>'。";

        String userPrompt = "请审核以下内容：\n" + content;

        String result = chat(systemPrompt, userPrompt);
        return result != null ? result.trim() : "PASS";
    }

    @Override
    public List<Double> getTextEmbedding(String text) {
        try {
            JSONObject body = new JSONObject();
            body.set("model", "text-embedding-ada-002"); // OpenAI embedding模型
            body.set("input", text);

            String apiUrl = getBaseUrl() + "/embeddings";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject result = JSONUtil.parseObj(response.body());

            if (result.containsKey("error")) {
                log.error("Embedding API调用失败: {}", result.getJSONObject("error").getStr("message"));
                return List.of();
            }

            JSONArray data = result.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                JSONArray embedding = data.getJSONObject(0).getJSONArray("embedding");
                List<Double> vector = new ArrayList<>();
                for (int i = 0; i < embedding.size(); i++) {
                    vector.add(embedding.getDouble(i));
                }
                return vector;
            }

            return List.of();
        } catch (Exception e) {
            log.error("Embedding API调用异常", e);
            return List.of();
        }
    }
}
