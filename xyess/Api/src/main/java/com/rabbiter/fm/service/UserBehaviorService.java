package com.rabbiter.fm.service;

import com.rabbiter.fm.model.UserBehavior;

public interface UserBehaviorService {
    /**
     * 添加用户行为记录
     * 
     * @param userBehavior 用户行为对象
     * @return 影响的行数
     */
    int addUserBehavior(UserBehavior userBehavior);
}