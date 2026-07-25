package com.campus.lostfound.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDetailResponse {

    private Long id;
    private Integer type;
    private String title;
    private String description;
    private String categoryName;
    private Integer categoryId;
    private String location;
    private LocalDate itemDate;
    private String contact;
    private Integer status;

    // AI相关
    private String aiCategory;
    private String aiDescription;
    private String aiOcrText;

    // 图片
    private List<String> images;

    // 发布者信息
    private Long userId;
    private String userNickname;
    private String userAvatar;

    // 统计
    private Integer viewCount;
    private Integer commentCount;

    // 当前用户是否已收藏
    private Boolean isFavorited;

    private LocalDateTime createdAt;
}
