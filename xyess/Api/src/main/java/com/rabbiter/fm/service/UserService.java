package com.rabbiter.fm.service;

import com.rabbiter.fm.model.UserModel;
import com.rabbiter.fm.vo.PageVo;

public interface UserService {

    /**
     * 获取某个用户的公开信息
     * @param id
     * @return
     */
    UserModel getUser(Long id);

    /**
     * 登录接口
     * @param accountNumber
     * @param userPassword
     * @return
     */
    UserModel userLogin(String accountNumber, String userPassword);

    /**
     * 注册接口
     * @param userModel
     * @return
     */
    boolean userSignIn(UserModel userModel);

    /**
     * 更新用户信息
     * @param userModel
     * @return
     */
    boolean updateUserInfo(UserModel userModel);

    /**
     * 修改密码
     * @param newPassword
     * @param oldPassword
     * @param id
     * @return
     */
    boolean updatePassword(String newPassword, String oldPassword,Long id);

    PageVo<UserModel> getUserByStatus(int status, int page , int nums);
}
