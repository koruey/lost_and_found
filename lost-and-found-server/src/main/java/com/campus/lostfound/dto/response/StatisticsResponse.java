package com.campus.lostfound.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatisticsResponse {

    /** 失物总数 */
    private Long lostCount;

    /** 招领总数 */
    private Long foundCount;

    /** 已解决数量 */
    private Long resolvedCount;

    /** 匹配成功数量 */
    private Long matchCount;

    /** 用户总数 */
    private Long userCount;

    /** 分类统计 */
    private List<Map<String, Object>> categoryStats;

    /** 每日发布趋势 */
    private List<Map<String, Object>> dailyTrend;
}
