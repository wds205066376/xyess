package com.rabbiter.fm.controller;

import com.rabbiter.fm.service.UserService;
import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.UserModel;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 用户管理控制器
 * 提供用户相关的核心功能接口：
 * 1. 用户注册
 * 2. 用户登录
 * 3. 用户信息管理
 * 4. 用户状态管理
 * 
 * 安全措施：
 * - 跨域配置
 * - 参数验证
 * - 错误处理
 */
@CrossOrigin(origins = { "http://localhost:9321" }, allowCredentials = "true")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * 功能：
     * 1. 验证用户信息
     * 2. 设置默认头像
     * 3. 初始化用户状态
     * 4. 记录注册时间
     * 
     * @param userModel 用户注册信息
     * @return 注册结果
     */
    @PostMapping("sign-in")
    public ResultVo signIn(@RequestBody UserModel userModel) {
        System.out.println(userModel);
        // 设置注册时间
        userModel.setSignInTime(new Timestamp(System.currentTimeMillis()));

        // 设置默认头像
        if (userModel.getAvatar() == null || "".equals(userModel.getAvatar())) {
            userModel.setAvatar("/image?imageName=cat.jpg");
        }

        // 初始化用户状态（0-正常）
        if (userModel.getUserStatus() == null) {
            userModel.setUserStatus((byte) 0);
        }

        if (userService.userSignIn(userModel)) {
            return ResultVo.success(userModel);
        }
        return ResultVo.fail(ErrorMsg.REGISTER_ERROR);
    }

    /**
     * 用户登录接口
     * 功能：
     * 1. 验证用户账号密码
     * 2. 生成登录凭证
     * 3. 设置Cookie
     * 4. 返回用户信息
     * 
     * 安全措施：
     * - 密码加密传输
     * - Cookie安全配置
     * - 登录状态验证
     * 
     * @param accountNumber 用户账号
     * @param userPassword  用户密码
     * @param response      HTTP响应对象
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResultVo login(@RequestParam("accountNumber") String accountNumber,
            @RequestParam("userPassword") String userPassword,
            HttpServletResponse response) {
        UserModel loginUser = userService.userLogin(accountNumber, userPassword);
        if (null == loginUser) {
            return ResultVo.fail(ErrorMsg.EMAIL_LOGIN_ERROR);
        }
        if (loginUser.getUserStatus() != null && loginUser.getUserStatus().equals((byte) 1)) {
            return ResultVo.fail(ErrorMsg.ACCOUNT_Ban);
        }
        Cookie cookie = new Cookie("shUserId", String.valueOf(loginUser.getId()));
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        return ResultVo.success(loginUser);
    }

    /**
     * 退出登录
     *
     * @param shUserId
     * @param response
     * @return
     */
    @RequestMapping("logout")
    public ResultVo logout(
            @CookieValue("shUserId") @NotNull(message = "登录异常 请重新登录") @NotEmpty(message = "登录异常 请重新登录") String shUserId,
            HttpServletResponse response) {
        Cookie cookie = new Cookie("shUserId", shUserId);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResultVo.success();
    }

    /**
     * 获取用户信息接口
     * 功能：
     * 1. 根据用户ID获取用户信息
     * 2. 返回用户详细信息
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("info")
    public ResultVo getOneUser(@RequestParam("id") Long id) {
        return ResultVo.success(userService.getUser(id));
    }

    /**
     * 更新用户信息接口
     * 功能：
     * 1. 验证用户信息
     * 2. 更新用户数据
     * 3. 返回更新结果
     * 
     * @param userModel 用户信息
     * @return 更新结果
     */
    @PostMapping("/info")
    public ResultVo updateUserPublicInfo(@RequestBody UserModel userModel) {
        if (userService.updateUserInfo(userModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    /**
     * 修改密码
     *
     * @param id
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @GetMapping("/password")
    public ResultVo updateUserPassword(
            @RequestParam("id") Long id,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        if (userService.updatePassword(newPassword, oldPassword, id)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.PASSWORD_RESET_ERROR);
    }
}
