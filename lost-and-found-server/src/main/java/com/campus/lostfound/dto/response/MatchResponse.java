package com.campus.lostfound.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MatchResponse {

    private Long id;
    private Long lostItemId;
    private Long foundItemId;
    private String lostItemTitle;     // 失物标题
    private String foundItemTitle;    // 招领标题
    private Long itemId;              // 匹配到的物品ID(单物品视角)
    private String itemTitle;
    private String itemType;          // 失物/招领
    private String firstImage;
    private BigDecimal totalScore;
    private BigDecimal imageScore;
    private BigDecimal textScore;
    private BigDecimal ocrScore;
    private BigDecimal categoryScore;
    private BigDecimal locationScore;
    private BigDecimal timeScore;
    private String reason;
    private LocalDateTime createdAt;
}
