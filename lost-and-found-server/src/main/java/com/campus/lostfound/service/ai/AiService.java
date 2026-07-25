package com.campus.lostfound.service.ai;

import java.util.List;

/**
 * AI服务统一接口
 * 支持多厂商: OpenAI / Claude / DeepSeek / 通义千问
 */
public interface AiService {

    /**
     * 图片识别 - 识别物品类别
     * @param imageUrl 图片URL
     * @return 分类名称
     */
    String recognizeImage(String imageUrl);

    /**
     * 图片描述生成
     * @param imageUrl 图片URL
     * @return 外观描述文字
     */
    String describeImage(String imageUrl);

    /**
     * OCR文字识别
     * @param imageUrl 图片URL
     * @return 识别文字
     */
    String ocr(String imageUrl);

    /**
     * 图文联合理解 - 结合图片和用户描述生成完整说明
     * @param imageUrl 图片URL
     * @param userText 用户输入文字
     * @return 增强后的完整描述
     */
    String enhanceDescription(String imageUrl, String userText);

    /**
     * 内容审核
     * @param content 待审核文本
     * @return "PASS" 或违规原因
     */
    String auditContent(String content);

    /**
     * 获取文本向量（用于语义相似度计算）
     * @param text 文本
     * @return 向量数组
     */
    List<Double> getTextEmbedding(String text);
}
