package com.rabbiter.fm.model;

import lombok.Data;

@Data
public class UserBehavior {
    private Integer id;
    private Integer userId;
    private Integer idleId;
    private Integer behaviorType; // 1: 浏览, 2: 收藏, 3: 购买
    private String createTime;
}