package com.rabbiter.fm.service;

import com.rabbiter.fm.model.IdleItemModel;
import java.util.List;

public interface RecommendationService {
    List<IdleItemModel> getRecommendedItems(Integer userId, int limit);

    /**
     * 获取用户推荐商品，排除当前正在浏览的商品
     * 
     * @param userId        用户ID
     * @param limit         限制数量
     * @param currentItemId 当前正在浏览的商品ID，将被排除在推荐结果外
     * @return 推荐商品列表
     */
    List<IdleItemModel> getRecommendedItems(Integer userId, int limit, Integer currentItemId);
}