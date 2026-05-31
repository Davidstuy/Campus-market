package com.campusmarket.user.controller;

import com.campusmarket.common.response.ApiResult;
import com.campusmarket.user.dto.UpdateProfileRequest;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 用户个人资料接口
 *
 * 认证：三个接口都需要登录，JWT Filter 已验证 token 并把 User 放入 request attribute。
 * Controller 通过 @RequestAttribute("currentUser") 直接获取当前用户。
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${campus-market.upload.path}")
    private String uploadPath;

    /**
     * 获取当前用户资料
     */
    @GetMapping("/me")
    public ApiResult<User> getProfile(@RequestAttribute("currentUser") User currentUser) {
        return ApiResult.success(currentUser);
    }

    /**
     * 公开接口：根据 ID 获取卖家简要信息（头像、昵称）
     */
    @GetMapping("/public/{id}")
    public ApiResult<Map<String, String>> getPublicInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResult.error(404, "用户不存在");
        }
        Map<String, String> info = new HashMap<>();
        info.put("id", String.valueOf(user.getId()));
        info.put("nickname", user.getNickname());
        info.put("avatarUrl", user.getAvatarUrl());
        return ApiResult.success(info);
    }

    /**
     * 更新个人资料（昵称、手机号、微信、QQ）
     *
     * MyBatis-Plus 的 updateById 会根据主键 id 更新，只更新非 null 字段不够精确，
     * 这里用 UserService.updateById 直接更新整个实体，前端传什么就更新什么。
     */
    @PutMapping("/me")
    public ApiResult<User> updateProfile(@RequestAttribute("currentUser") User currentUser,
                                         @RequestBody UpdateProfileRequest request) {
        currentUser.setNickname(request.getNickname());
        currentUser.setAvatarUrl(request.getAvatarUrl());
        currentUser.setPhone(request.getPhone());
        currentUser.setWechat(request.getWechat());
        currentUser.setQq(request.getQq());

        userService.updateById(currentUser);

        log.info("用户 {} 更新了个人资料", currentUser.getId());
        return ApiResult.success(currentUser);
    }

    /**
     * 上传头像
     *
     * 流程：接收文件 → 校验 → 生成 UUID 文件名 → 保存到磁盘 → 更新 user.avatar_url → 返回新 URL
     *
     * consumes = multipart/form-data：声明这个接口接收文件上传格式
     */
    @PostMapping("/me/avatar")
    public ApiResult<Map<String, String>> uploadAvatar(
            @RequestAttribute("currentUser") User currentUser,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ApiResult.error(400, "请选择文件");
        }

        // 校验扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (extension == null) {
            return ApiResult.error(400, "无法识别文件类型");
        }

        Set<String> allowed = Set.of("jpg", "jpeg", "png", "gif", "webp");
        if (!allowed.contains(extension)) {
            return ApiResult.error(400, "不支持的文件类型，允许：" + String.join(",", allowed));
        }

        // 保存文件
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path destPath = uploadDir.resolve(newFilename);
            file.transferTo(destPath.toFile());

            // 更新用户头像 URL
            String url = "/v1/files/" + newFilename;
            currentUser.setAvatarUrl(url);
            userService.updateById(currentUser);

            log.info("用户 {} 更新了头像: {}", currentUser.getId(), url);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return ApiResult.success(data);

        } catch (IOException e) {
            log.error("头像上传失败", e);
            return ApiResult.error(500, "头像上传失败，请重试");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
