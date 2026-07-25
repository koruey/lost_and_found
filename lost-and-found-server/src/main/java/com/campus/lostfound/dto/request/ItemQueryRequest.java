package com.campus.lostfound.dto.request;

import lombok.Data;

/**
 * 物品查询请求
 */
@Data
public class ItemQueryRequest {

    /** 类型: 0-失物, 1-招领, null-全部 */
    private Integer type;

    /** 分类ID */
    private Integer categoryId;

    /** 关键词搜索 */
    private String keyword;

    /** 地点 */
    private String location;

    /** 开始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;

    /** 状态: 1-已发布, null-不限制 */
    private Integer status;

    /** 排序: create_time / view_count */
    private String sortBy = "create_time";

    /** 页码 */
    private Integer page = 1;

    /** 每页大小 */
    private Integer size = 10;
}
