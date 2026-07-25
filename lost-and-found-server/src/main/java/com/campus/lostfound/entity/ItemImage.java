package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物品图片
 */
@Data
@TableName("item_image")
public class ItemImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private String url;

    private String thumbnailUrl;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
