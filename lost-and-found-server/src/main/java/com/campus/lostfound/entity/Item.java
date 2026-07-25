package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物品信息实体（失物/招领统一）
 */
@Data
@TableName("item")
public class Item {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者ID */
    private Long userId;

    /** 类型: 0-失物, 1-招领 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 物品描述 */
    private String description;

    /** 分类ID */
    private Integer categoryId;

    /** 丢失/拾取地点 */
    private String location;

    /** 丢失/拾取日期 */
    private LocalDate itemDate;

    /** 联系方式 */
    private String contact;

    /** 状态: 0-待审核, 1-已发布, 2-审核不通过, 3-已解决 */
    private Integer status;

    /** AI识别的分类 */
    private String aiCategory;

    /** AI生成的增强描述 */
    private String aiDescription;

    /** OCR识别文字 */
    private String aiOcrText;

    /** AI提取的特征标签(JSON) */
    private String aiFeatures;

    /** 浏览次数 */
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
