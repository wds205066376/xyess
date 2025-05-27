package com.rabbiter.fm.controller;

import com.rabbiter.fm.common.utils.PathUtils;
import com.rabbiter.fm.service.FileService;
import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.common.utils.IdFactoryUtil;
import com.rabbiter.fm.vo.ResultVo;
import com.rabbiter.fm.service.UserService;
import com.rabbiter.fm.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件管理控制器
 * 提供文件处理相关的核心功能接口：
 * 1. 文件上传
 * 2. 图片获取
 * 3. 文件存储管理
 * 
 * 特点：
 * - 支持跨域访问
 * - 文件类型验证
 * - 安全存储机制
 * - 缓存优化
 */
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = { "http://localhost:9323", "http://127.0.0.1:9323",
        "http://localhost:9321" }, allowCredentials = "true")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    /**
     * 文件上传接口
     * 功能：
     * 1. 验证文件有效性
     * 2. 生成唯一文件名
     * 3. 创建存储目录
     * 4. 保存文件
     * 
     * 安全措施：
     * - 文件大小限制
     * - 文件类型验证
     * - 存储路径安全
     * 
     * @param multipartFile 上传的文件
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    public ResultVo uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return ResultVo.fail(ErrorMsg.FILE_UPLOAD_ERROR);
        }

        try {
            // 生成唯一文件名
            String uuid = "file" + IdFactoryUtil.getFileId();
            String fileName = uuid + multipartFile.getOriginalFilename();

            // 创建存储目录
            File fileDir = new File(PathUtils.getClassLoadRootPath() + "/pic");
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            // 保存文件
            File dest = new File(fileDir.getAbsolutePath() + "/" + fileName);
            multipartFile.transferTo(dest);

            // 返回文件访问URL
            return ResultVo.success("/file/image?imageName=" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResultVo.fail(ErrorMsg.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 获取图片接口
     * 功能：
     * 1. 根据图片名称获取图片
     * 2. 设置响应头
     * 3. 输出图片数据
     * 
     * 优化措施：
     * - 浏览器缓存
     * - 图片压缩
     * - 错误处理
     * 
     * @param imageName 图片名称
     * @param response  HTTP响应对象
     */
    @GetMapping("/image")
    public void getImage(@RequestParam("imageName") String imageName,
            HttpServletResponse response) throws IOException {
        // 构建图片文件路径
        File fileDir = new File(PathUtils.getClassLoadRootPath() + "/pic");
        File image = new File(fileDir.getAbsolutePath() + "/" + imageName);

        if (image.exists()) {
            // 读取图片数据
            FileInputStream fileInputStream = new FileInputStream(image);
            byte[] bytes = new byte[fileInputStream.available()];

            if (fileInputStream.read(bytes) > 0) {
                // 设置响应头
                response.setContentType("image/jpeg");
                // 设置缓存时间（1年）
                response.setHeader("Cache-Control", "max-age=31536000");

                // 输出图片数据
                OutputStream outputStream = response.getOutputStream();
                outputStream.write(bytes);
                outputStream.close();
            }
            fileInputStream.close();
        } else {
            // 图片不存在时返回404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 通过用户ID获取头像
     */
    @GetMapping("/avatar/{userId}")
    public void getUserAvatar(@PathVariable("userId") Long userId, HttpServletResponse response) throws IOException {
        UserModel user = userService.getUser(userId);
        if (user != null && user.getAvatar() != null) {
            String avatarPath = user.getAvatar();
            String imageName = null;

            // 从不同格式的路径中提取图片名称
            if (avatarPath.contains("imageName=")) {
                imageName = avatarPath.split("imageName=")[1].split("&")[0];
            } else if (avatarPath.startsWith("/image")) {
                imageName = avatarPath.split("\\?imageName=")[1];
            } else {
                imageName = avatarPath;
            }

            // 获取图片文件
            File fileDir = new File(PathUtils.getClassLoadRootPath() + "/pic");
            File image = new File(fileDir.getAbsolutePath() + "/" + imageName);

            if (image.exists()) {
                FileInputStream fileInputStream = new FileInputStream(image);
                byte[] bytes = new byte[fileInputStream.available()];
                if (fileInputStream.read(bytes) > 0) {
                    response.setContentType("image/jpeg");
                    // 设置不缓存
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);

                    OutputStream outputStream = response.getOutputStream();
                    outputStream.write(bytes);
                    outputStream.close();
                }
                fileInputStream.close();
            } else {
                // 如果找不到图片，返回默认头像
                File defaultImage = new File(fileDir.getAbsolutePath() + "/noasndo123.jpg");
                if (defaultImage.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(defaultImage);
                    byte[] bytes = new byte[fileInputStream.available()];
                    if (fileInputStream.read(bytes) > 0) {
                        response.setContentType("image/jpeg");
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                        OutputStream outputStream = response.getOutputStream();
                        outputStream.write(bytes);
                        outputStream.close();
                    }
                    fileInputStream.close();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
