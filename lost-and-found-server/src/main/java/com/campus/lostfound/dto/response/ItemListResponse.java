package com.campus.lostfound.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ItemListResponse {

    private Long id;
    private Integer type;
    private String title;
    private String description;
    private String categoryName;
    private String location;
    private LocalDate itemDate;
    private Integer status;
    private String firstImage;       // 列表展示第一张图
    private String userNickname;
    private Integer viewCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
}
