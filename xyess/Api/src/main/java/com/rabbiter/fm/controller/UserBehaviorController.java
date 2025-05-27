package com.rabbiter.fm.controller;

import com.rabbiter.fm.common.Result;
import com.rabbiter.fm.model.UserBehavior;
import com.rabbiter.fm.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/user-behavior")
public class UserBehaviorController {

    @Autowired
    private UserBehaviorService userBehaviorService;

    @PostMapping("/add")
    public Result<Integer> addUserBehavior(@RequestBody UserBehavior userBehavior) {
        try {
            // 设置创建时间
            userBehavior.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 插入行为记录
            int result = userBehaviorService.addUserBehavior(userBehavior);

            if (result > 0) {
                return Result.success(result);
            } else {
                return Result.error("添加用户行为记录失败");
            }
        } catch (Exception e) {
            return Result.error("添加用户行为记录失败: " + e.getMessage());
        }
    }
}