package com.rabbiter.fm.controller;

import com.alibaba.fastjson.JSON;
import com.rabbiter.fm.model.IdleItemModel;
import com.rabbiter.fm.service.RecommendationService;
import com.rabbiter.fm.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendation")
public class RecommendationController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/items")
    public Result<List<IdleItemModel>> getRecommendedItems(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Integer currentItemId) {
        logger.info("接收到推荐请求: userId={}, limit={}, currentItemId={}", userId, limit, currentItemId);
        try {
            // 添加随机时间戳参数，确保每次请求都能获取新的结果
            long timestamp = System.currentTimeMillis();

            List<IdleItemModel> recommendedItems;
            if (currentItemId != null) {
                // 使用新的排除当前商品的方法
                recommendedItems = recommendationService.getRecommendedItems(userId, limit, currentItemId);
            } else {
                // 保持兼容性
                recommendedItems = recommendationService.getRecommendedItems(userId, limit);
            }

            logger.info("推荐成功, 返回商品数量: {}, 时间戳: {}", recommendedItems.size(), timestamp);

            // 记录每个推荐商品的信息，特别是图片字段
            if (recommendedItems.size() > 0) {
                for (IdleItemModel item : recommendedItems) {
                    logger.info("推荐商品: id={}, name={}, pictureList={}",
                            item.getId(), item.getIdleName(), item.getPictureList());
                }
            }

            return Result.success(recommendedItems);
        } catch (Exception e) {
            logger.error("获取推荐商品失败", e);
            return Result.error("获取推荐商品失败：" + e.getMessage());
        }
    }
}
