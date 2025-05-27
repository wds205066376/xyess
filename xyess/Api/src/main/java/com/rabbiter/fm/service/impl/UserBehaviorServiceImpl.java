package com.rabbiter.fm.service.impl;

import com.rabbiter.fm.dao.UserBehaviorDao;
import com.rabbiter.fm.model.UserBehavior;
import com.rabbiter.fm.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorDao userBehaviorDao;

    @Override
    public int addUserBehavior(UserBehavior userBehavior) {
        // 验证必要参数
        if (userBehavior.getUserId() == null || userBehavior.getIdleId() == null
                || userBehavior.getBehaviorType() == null) {
            return 0;
        }

        // 插入行为记录
        return userBehaviorDao.insert(userBehavior);
    }
}