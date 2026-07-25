package com.campus.lostfound.common;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 */
@Getter
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long total;       // 总记录数
    private final long page;        // 当前页码
    private final long size;        // 每页大小
    private final long pages;       // 总页数
    private final List<T> records;  // 数据列表

    private PageResult(long total, long page, long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = total == 0 ? 0 : (total + size - 1) / size;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
