package com.rabbiter.fm.dao;

import com.rabbiter.fm.model.UserBehavior;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserBehaviorDao {
    List<UserBehavior> findByUserId(Integer userId);

    List<UserBehavior> findByIdleId(Integer idleId);

    int insert(UserBehavior userBehavior);

    List<UserBehavior> findAll();
}