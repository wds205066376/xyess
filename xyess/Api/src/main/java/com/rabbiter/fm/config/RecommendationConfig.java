package com.rabbiter.fm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "recommendation")
@Data
public class RecommendationConfig {

    // 时间衰减因子（天）
    private double timeDecayFactor = 0.1;

    // 类别权重
    private double categoryWeight = 0.3;

    // 行为权重
    private double viewWeight = 1.0;
    private double favoriteWeight = 2.0;
    private double purchaseWeight = 3.0;

    // 缓存过期时间（分钟）
    private int cacheExpirationMinutes = 30;

    // 热门商品更新间隔（分钟）
    private int popularItemsUpdateInterval = 60;

    // 推荐结果数量限制
    private int defaultLimit = 10;
    private int maxLimit = 50;
}