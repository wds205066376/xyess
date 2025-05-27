package com.rabbiter.fm.service.impl;

import com.rabbiter.fm.dao.IdleItemDao;
import com.rabbiter.fm.dao.UserBehaviorDao;
import com.rabbiter.fm.model.IdleItemModel;
import com.rabbiter.fm.model.UserBehavior;
import com.rabbiter.fm.config.RecommendationConfig;
import com.rabbiter.fm.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * 推荐系统服务实现类
 * 实现了基于用户行为的协同过滤推荐算法
 * 主要功能：
 * 1. 计算用户相似度
 * 2. 基于时间衰减的行为权重计算
 * 3. 商品类别相似度计算
 * 4. 个性化商品推荐
 * 5. 热门商品推荐
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    @Autowired
    private UserBehaviorDao userBehaviorDao;

    @Autowired
    private IdleItemDao idleItemDao;

    @Autowired
    private RecommendationConfig config;

    /**
     * 计算用户相似度
     * 核心算法：基于用户行为的时间衰减协同过滤
     * 计算步骤：
     * 1. 找出两个用户的共同交互商品
     * 2. 对每个共同商品计算时间衰减权重
     * 3. 考虑用户行为类型权重（浏览、收藏、购买）
     * 4. 考虑商品类别相似度
     * 5. 综合计算最终相似度得分
     */
    private double calculateUserSimilarity(List<UserBehavior> user1Behaviors, List<UserBehavior> user2Behaviors) {
        // 将用户行为转换为Map，便于快速查找
        Map<Integer, UserBehavior> user1BehaviorMap = user1Behaviors.stream()
                .collect(Collectors.toMap(UserBehavior::getIdleId, behavior -> behavior));
        Map<Integer, UserBehavior> user2BehaviorMap = user2Behaviors.stream()
                .collect(Collectors.toMap(UserBehavior::getIdleId, behavior -> behavior));

        // 计算两个用户共同交互的商品集合
        Set<Integer> commonItems = new HashSet<>(user1BehaviorMap.keySet());
        commonItems.retainAll(user2BehaviorMap.keySet());

        if (commonItems.isEmpty()) {
            return 0.0;
        }

        double similarity = 0.0;
        LocalDateTime now = LocalDateTime.now();

        // 遍历共同商品，计算相似度
        for (Integer itemId : commonItems) {
            UserBehavior behavior1 = user1BehaviorMap.get(itemId);
            UserBehavior behavior2 = user2BehaviorMap.get(itemId);

            // 计算时间衰减因子，越近的行为权重越大
            double timeDecay1 = calculateTimeDecay(behavior1.getCreateTime(), now);
            double timeDecay2 = calculateTimeDecay(behavior2.getCreateTime(), now);

            // 计算行为权重，不同行为类型有不同的权重
            double weight1 = getBehaviorWeight(behavior1.getBehaviorType()) * timeDecay1;
            double weight2 = getBehaviorWeight(behavior2.getBehaviorType()) * timeDecay2;

            // 计算类别相似度，同类商品权重更高
            double categoryWeight = calculateCategoryWeight(behavior1.getIdleId(), behavior2.getIdleId());

            // 累加相似度得分
            similarity += weight1 * weight2 * categoryWeight;
        }

        return similarity;
    }

    /**
     * 计算时间衰减因子
     * 使用指数衰减函数：e^(-αt)，其中：
     * α 为时间衰减因子（可配置）
     * t 为时间间隔（天数）
     * 作用：使近期行为具有更高的权重
     */
    private double calculateTimeDecay(String createTimeStr, LocalDateTime now) {
        try {
            if (createTimeStr == null || createTimeStr.isEmpty()) {
                return 1.0; // 默认权重
            }

            // 解析时间字符串
            LocalDateTime createTime;
            try {
                createTime = LocalDateTime.parse(createTimeStr);
            } catch (Exception e) {
                // 尝试其他时间格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                createTime = LocalDateTime.parse(createTimeStr, formatter);
            }

            // 计算时间差（天数）
            long daysDiff = ChronoUnit.DAYS.between(createTime, now);
            if (daysDiff < 0)
                daysDiff = 0;

            // 计算时间衰减
            return Math.exp(-config.getTimeDecayFactor() * daysDiff);
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * 计算商品类别权重
     * 如果两个商品属于同一类别，增加权重
     * 作用：提高同类商品的推荐优先级
     */
    private double calculateCategoryWeight(Integer itemId1, Integer itemId2) {
        try {
            if (itemId1 == null || itemId2 == null) {
                return 1.0;
            }

            IdleItemModel item1 = idleItemDao.selectById(itemId1);
            IdleItemModel item2 = idleItemDao.selectById(itemId2);

            if (item1 == null || item2 == null) {
                return 1.0;
            }

            // 同类商品增加权重
            if (item1.getIdleLabel() != null && item2.getIdleLabel() != null &&
                    item1.getIdleLabel().equals(item2.getIdleLabel())) {
                return 1.0 + config.getCategoryWeight();
            }
            return 1.0;
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * 获取行为权重
     * 不同行为类型具有不同的权重：
     * - 浏览：基础权重
     * - 收藏：较高权重
     * - 购买：最高权重
     */
    private double getBehaviorWeight(int behaviorType) {
        switch (behaviorType) {
            case 1: // 浏览
                return config.getViewWeight();
            case 2: // 收藏
                return config.getFavoriteWeight();
            case 3: // 购买
                return config.getPurchaseWeight();
            default:
                return 0.0;
        }
    }

    /**
     * 获取用户推荐商品
     * 推荐流程：
     * 1. 验证用户ID和限制参数
     * 2. 获取用户行为历史
     * 3. 计算用户相似度
     * 4. 获取相似用户喜欢的商品
     * 5. 计算商品得分并排序
     * 6. 返回推荐结果
     * 
     * 降级策略：
     * - 无用户ID：返回热门商品
     * - 无行为历史：返回热门商品
     * - 无相似用户：返回热门商品
     * - 无推荐商品：返回热门商品
     */
    @Override
    public List<IdleItemModel> getRecommendedItems(Integer userId, int limit, Integer currentItemId) {
        try {
            // 验证并限制推荐数量
            limit = Math.min(Math.max(1, limit), config.getMaxLimit());

            // 无用户ID时返回热门商品
            if (userId == null || userId == 0) {
                return getPopularItems(limit, currentItemId);
            }

            // 获取用户行为历史
            List<UserBehavior> userBehaviors = userBehaviorDao.findByUserId(userId);
            if (userBehaviors == null || userBehaviors.isEmpty()) {
                return getPopularItems(limit, currentItemId);
            }

            // 获取所有用户行为数据
            List<UserBehavior> allBehaviors = userBehaviorDao.findAll();
            if (allBehaviors == null || allBehaviors.isEmpty()) {
                return getPopularItems(limit, currentItemId);
            }

            // 按用户ID分组行为数据
            Map<Integer, List<UserBehavior>> userBehaviorsMap = allBehaviors.stream()
                    .collect(Collectors.groupingBy(UserBehavior::getUserId));

            // 计算用户相似度
            Map<Integer, Double> userSimilarities = new HashMap<>();
            for (Map.Entry<Integer, List<UserBehavior>> entry : userBehaviorsMap.entrySet()) {
                if (!entry.getKey().equals(userId)) {
                    try {
                        double similarity = calculateUserSimilarity(userBehaviors, entry.getValue());
                        if (similarity > 0) {
                            userSimilarities.put(entry.getKey(), similarity);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }

            // 无相似用户时返回热门商品
            if (userSimilarities.isEmpty()) {
                return getPopularItems(limit, currentItemId);
            }

            // 获取用户已交互的商品集合
            Set<Integer> userInteractedItems = userBehaviors.stream()
                    .map(UserBehavior::getIdleId)
                    .collect(Collectors.toSet());

            // 排除当前浏览的商品
            if (currentItemId != null) {
                userInteractedItems.add(currentItemId);
            }

            // 计算商品推荐得分
            Map<Integer, Double> itemScores = new HashMap<>();
            for (Map.Entry<Integer, Double> entry : userSimilarities.entrySet()) {
                Integer similarUserId = entry.getKey();
                Double similarity = entry.getValue();

                List<UserBehavior> similarUserBehaviors = userBehaviorsMap.get(similarUserId);
                if (similarUserBehaviors != null) {
                    for (UserBehavior behavior : similarUserBehaviors) {
                        if (behavior.getIdleId() != null && !userInteractedItems.contains(behavior.getIdleId())) {
                            try {
                                double timeDecay = calculateTimeDecay(behavior.getCreateTime(), LocalDateTime.now());
                                double score = similarity * getBehaviorWeight(behavior.getBehaviorType()) * timeDecay;
                                itemScores.merge(behavior.getIdleId(), score, Double::sum);
                            } catch (Exception e) {
                                continue;
                            }
                        }
                    }
                }
            }

            // 无推荐商品时返回热门商品
            if (itemScores.isEmpty()) {
                return getPopularItems(limit, currentItemId);
            }

            // 根据得分排序并获取推荐商品
            List<IdleItemModel> recommendations = itemScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        try {
                            return idleItemDao.selectById(entry.getKey());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 推荐结果为空时返回热门商品
            if (recommendations.isEmpty()) {
                return getPopularItems(limit, currentItemId);
            }

            return recommendations;
        } catch (Exception e) {
            return getPopularItems(limit, currentItemId);
        }
    }

    // 兼容旧版本接口
    @Override
    public List<IdleItemModel> getRecommendedItems(Integer userId, int limit) {
        return getRecommendedItems(userId, limit, null);
    }

    /**
     * 获取热门商品
     * 实现策略：
     * 1. 优先获取随机商品
     * 2. 随机商品不足时获取最新商品
     * 3. 排除当前正在浏览的商品
     * 4. 限制返回数量
     */
    private List<IdleItemModel> getPopularItems(int limit, Integer currentItemId) {
        logger.info("获取随机商品, limit={}, 排除商品ID={}", limit, currentItemId);

        try {
            // 获取随机商品
            List<IdleItemModel> randomItems = idleItemDao.selectRandomItems(limit * 2);

            if (randomItems == null || randomItems.isEmpty()) {
                logger.warn("未能获取到随机商品，尝试获取最新商品");
                randomItems = idleItemDao.selectLatestItems(limit * 2);
            }

            if (randomItems == null || randomItems.isEmpty()) {
                logger.error("无法获取任何商品");
                return new ArrayList<>();
            }

            // 过滤当前商品并限制数量
            List<IdleItemModel> result;
            if (currentItemId != null) {
                result = randomItems.stream()
                        .filter(item -> !Integer.valueOf(currentItemId).equals(item.getId().intValue()))
                        .limit(limit)
                        .collect(Collectors.toList());
            } else {
                result = randomItems.stream().limit(limit).collect(Collectors.toList());
            }

            logger.info("返回随机商品，数量: {}", result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取随机商品出错", e);
            return new ArrayList<>();
        }
    }

    // 兼容旧方法
    private List<IdleItemModel> getPopularItems(int limit) {
        return getPopularItems(limit, null);
    }
}