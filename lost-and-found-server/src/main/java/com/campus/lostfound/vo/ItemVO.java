package com.campus.lostfound.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemVO {

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
    private String aiCategory;
    private String aiDescription;
    private String aiOcrText;
    private List<String> images;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isFavorited;
    private LocalDateTime createdAt;
}
