package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("match_record")
public class MatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lostItemId;

    private Long foundItemId;

    /** 综合匹配度(0-100) */
    private BigDecimal totalScore;

    /** 图片相似度 */
    private BigDecimal imageScore;

    /** 文本语义相似度 */
    private BigDecimal textScore;

    /** OCR匹配度 */
    private BigDecimal ocrScore;

    /** 分类匹配度 */
    private BigDecimal categoryScore;

    /** 地点匹配度 */
    private BigDecimal locationScore;

    /** 时间匹配度 */
    private BigDecimal timeScore;

    /** 匹配理由说明 */
    private String reason;

    /** 状态: 0-待确认, 1-已通知, 2-已确认, 3-已驳回 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
