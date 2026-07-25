package com.campus.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 物品发布请求
 */
@Data
public class ItemPublishRequest {

    @NotNull(message = "物品类型不能为空")
    private Integer type;

    @NotBlank(message = "标题不能为空")
    @Size(min = 2, max = 100, message = "标题长度为2-100字")
    private String title;

    @NotBlank(message = "描述不能为空")
    @Size(min = 10, max = 500, message = "描述长度为10-500字")
    private String description;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotBlank(message = "地点不能为空")
    private String location;

    @NotNull(message = "日期不能为空")
    private LocalDate itemDate;

    private String contact;

    /** 图片URL列表（先上传图片再提交） */
    @NotNull(message = "至少上传一张图片")
    @Size(min = 1, max = 9, message = "图片数量为1-9张")
    private List<String> images;
}
