package com.campus.lostfound.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.lostfound.common.Constant;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.MatchRecord;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.mapper.MatchRecordMapper;
import com.campus.lostfound.service.MatchingService;
import com.campus.lostfound.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 智能匹配服务实现
 * 基于多维特征计算失物与招领的匹配度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final ItemMapper itemMapper;
    private final MatchRecordMapper matchRecordMapper;
    private final NotificationService notificationService;

    // 匹配权重
    private static final double WEIGHT_TEXT = 0.25;       // 文本语义
    private static final double WEIGHT_IMAGE_DESC = 0.25;  // 图片描述
    private static final double WEIGHT_OCR = 0.15;         // OCR文字
    private static final double WEIGHT_CATEGORY = 0.15;    // 分类
    private static final double WEIGHT_LOCATION = 0.10;    // 地点
    private static final double WEIGHT_TIME = 0.10;        // 时间

    @Override
    @Async("aiExecutor")
    public void matchItem(Long itemId) {
        triggerMatch(itemId);
    }

    @Override
    public void triggerMatch(Long itemId) {
        Item sourceItem = itemMapper.selectById(itemId);
        if (sourceItem == null) {
            log.warn("匹配失败: 物品{}不存在", itemId);
            return;
        }

        log.info("开始智能匹配: itemId={}, type={}", itemId, sourceItem.getType());

        // 查询对端类型的已发布物品
        int oppositeType = sourceItem.getType() == Constant.ITEM_TYPE_LOST
                ? Constant.ITEM_TYPE_FOUND : Constant.ITEM_TYPE_LOST;

        List<Item> candidates = itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getType, oppositeType)
                        .eq(Item::getStatus, Constant.ITEM_STATUS_PUBLISHED)
                        .ne(Item::getId, itemId));

        log.info("候选物品数量: {}", candidates.size());

        // 对每个候选物品计算匹配度
        List<MatchRecord> matches = new ArrayList<>();
        for (Item candidate : candidates) {
            MatchRecord record = computeMatch(sourceItem, candidate);
            if (record.getTotalScore().doubleValue() >= Constant.MATCH_THRESHOLD) {
                matches.add(record);
            }
        }

        // 按分数降序排列
        matches.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));

        // 删除旧匹配记录并保存新记录
        matchRecordMapper.delete(new LambdaQueryWrapper<MatchRecord>()
                .eq(MatchRecord::getLostItemId,
                        sourceItem.getType() == Constant.ITEM_TYPE_LOST ? itemId : null)
                .or()
                .eq(MatchRecord::getFoundItemId,
                        sourceItem.getType() == Constant.ITEM_TYPE_FOUND ? itemId : null));

        int saved = 0;
        for (MatchRecord match : matches) {
            matchRecordMapper.insert(match);
            saved++;
        }

        log.info("匹配完成: itemId={}, 匹配到{}条记录(阈值={})", itemId, saved, Constant.MATCH_THRESHOLD);

        // 通知双方（匹配度最高的前3条）
        int notifyCount = Math.min(3, matches.size());
        for (int i = 0; i < notifyCount; i++) {
            MatchRecord match = matches.get(i);
            notifyMatch(match);
        }
    }

    /**
     * 计算两个物品的匹配度
     */
    private MatchRecord computeMatch(Item source, Item candidate) {
        // 确定哪个是失物，哪个是招领
        Item lostItem = source.getType() == Constant.ITEM_TYPE_LOST ? source : candidate;
        Item foundItem = source.getType() == Constant.ITEM_TYPE_FOUND ? source : candidate;

        // 计算各维度分数
        double textScore = computeTextSimilarity(
                source.getTitle() + " " + source.getDescription(),
                candidate.getTitle() + " " + candidate.getDescription());

        double imageDescScore = computeTextSimilarity(
                source.getAiDescription() != null ? source.getAiDescription() : "",
                candidate.getAiDescription() != null ? candidate.getAiDescription() : "");

        double ocrScore = computeTextSimilarity(
                source.getAiOcrText() != null ? source.getAiOcrText() : "",
                candidate.getAiOcrText() != null ? candidate.getAiOcrText() : "");

        double categoryScore = source.getCategoryId().equals(candidate.getCategoryId()) ? 100.0 : 0.0;

        double locationScore = computeLocationScore(source.getLocation(), candidate.getLocation());

        double timeScore = computeTimeScore(source.getItemDate(), candidate.getItemDate());

        // 加权求和
        double total = textScore * WEIGHT_TEXT
                + imageDescScore * WEIGHT_IMAGE_DESC
                + ocrScore * WEIGHT_OCR
                + categoryScore * WEIGHT_CATEGORY
                + locationScore * WEIGHT_LOCATION
                + timeScore * WEIGHT_TIME;

        // 构建匹配记录
        MatchRecord record = new MatchRecord();
        record.setLostItemId(lostItem.getId());
        record.setFoundItemId(foundItem.getId());
        record.setTotalScore(bd(total));
        record.setTextScore(bd(textScore));
        record.setImageScore(bd(imageDescScore));
        record.setOcrScore(bd(ocrScore));
        record.setCategoryScore(bd(categoryScore));
        record.setLocationScore(bd(locationScore));
        record.setTimeScore(bd(timeScore));
        record.setReason(buildReason(source, candidate, textScore, categoryScore, locationScore, timeScore));
        record.setStatus(Constant.MATCH_STATUS_PENDING);

        return record;
    }

    /**
     * 文本相似度计算（基于关键词交集）
     */
    private double computeTextSimilarity(String text1, String text2) {
        if (StrUtil.isBlank(text1) || StrUtil.isBlank(text2)) {
            return 0.0;
        }

        // 分词（简单按字符切分2-gram）
        Set<String> tokens1 = tokenize(text1);
        Set<String> tokens2 = tokenize(text2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        // Jaccard相似度
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        return (double) intersection.size() / union.size() * 100.0;
    }

    /**
     * 简单分词（2-gram + 单字关键词）
     */
    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        // 去除标点
        String cleaned = text.replaceAll("[，。！？、；：\"'（）\\[\\]【】\\s]+", "");

        // 提取2-gram
        for (int i = 0; i < cleaned.length() - 1; i++) {
            tokens.add(cleaned.substring(i, i + 2));
        }

        // 提取关键词（长度>=2的词）
        for (String word : cleaned.split("的|了|在|是|有|和|就|不|人|都|一|个|上|也|很|到|说|要|去|你|会|着|没有|看")) {
            if (word.length() >= 1) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    /**
     * 地点相似度
     */
    private double computeLocationScore(String loc1, String loc2) {
        if (StrUtil.isBlank(loc1) || StrUtil.isBlank(loc2)) {
            return 0.0;
        }
        if (loc1.equals(loc2)) {
            return 100.0;
        }
        // 检查是否包含相同关键词（如"图书馆"、"食堂"等）
        Set<String> tokens1 = tokenize(loc1);
        Set<String> tokens2 = tokenize(loc2);
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        return intersection.isEmpty() ? 10.0 : 50.0;
    }

    /**
     * 时间相似度
     */
    private double computeTimeScore(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return 50.0; // 无信息时给中等分
        }
        long daysDiff = Math.abs(ChronoUnit.DAYS.between(date1, date2));
        if (daysDiff <= 3) {
            return 100.0;
        } else if (daysDiff <= 7) {
            return 70.0;
        } else if (daysDiff <= 30) {
            return 40.0;
        } else {
            return 10.0;
        }
    }

    /**
     * 生成匹配理由
     */
    private String buildReason(Item source, Item candidate, double textScore,
                                double categoryScore, double locationScore, double timeScore) {
        StringBuilder sb = new StringBuilder();
        if (categoryScore >= 100) sb.append("分类一致; ");
        if (textScore >= 50) sb.append("描述相似度高; ");
        if (locationScore >= 100) sb.append("地点一致; ");
        if (locationScore >= 50) sb.append("地点相近; ");
        if (timeScore >= 70) sb.append("时间接近; ");
        if (sb.isEmpty()) sb.append("综合特征匹配");
        return sb.toString();
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(Math.min(value, 100.0))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 发送匹配通知
     */
    private void notifyMatch(MatchRecord match) {
        Item lostItem = itemMapper.selectById(match.getLostItemId());
        Item foundItem = itemMapper.selectById(match.getFoundItemId());

        if (lostItem != null && foundItem != null) {
            String title = "物品匹配通知";
            String reason = match.getReason();

            // 通知失主
            notificationService.createNotification(
                    lostItem.getUserId(),
                    Constant.NOTIFY_TYPE_MATCH,
                    title,
                    "你的失物「" + lostItem.getTitle() + "」可能与招领「"
                            + foundItem.getTitle() + "」匹配！" + reason,
                    foundItem.getId());

            // 通知拾取者
            notificationService.createNotification(
                    foundItem.getUserId(),
                    Constant.NOTIFY_TYPE_MATCH,
                    title,
                    "你发布的招领「" + foundItem.getTitle() + "」可能与失物「"
                            + lostItem.getTitle() + "」匹配！" + reason,
                    lostItem.getId());
        }
    }
}
