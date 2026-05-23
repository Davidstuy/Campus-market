package com.campusmarket.file;

import com.campusmarket.common.response.ApiResult;
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
 * 文件上传接口
 *
 * 流程：
 * 1. 前端选择图片 → el-upload 发送 multipart/form-data 请求
 * 2. 后端接收 MultipartFile，校验类型和大小
 * 3. 生成 UUID 文件名，保存到 ./uploads 目录
 * 4. 返回可访问的 URL：/api/v1/files/uuid.jpg
 *
 * multipart 和 JSON 的区别：
 * - JSON (application/json)：传文本数据，如登录注册
 * - multipart/form-data：传文件 + 文本混合数据
 *
 * @RequestParam vs @RequestPart：
 * - @RequestParam：用于普通表单字段
 * - @RequestPart：用于文件字段，和 @RequestParam 类似但语义更明确
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/files")
public class FileController {

    @Value("${campus-market.upload.path}")
    private String uploadPath;

    @Value("${campus-market.upload.allowed-types}")
    private String allowedTypes;

    /**
     * 上传文件
     *
     * MultipartFile 是 Spring 对上传文件的抽象封装：
     * - getOriginalFilename() → 原始文件名
     * - getContentType() → MIME 类型
     * - getSize() → 字节数
     * - getBytes() → 文件内容
     * - transferTo(file) → 保存到磁盘
     */
    @PostMapping("/upload")
    public ApiResult<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResult.error(400, "请选择文件");
        }

        // 1. 校验文件类型（通过扩展名白名单）
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (extension == null || !isAllowedExtension(extension)) {
            return ApiResult.error(400, "不支持的文件类型，允许：" + allowedTypes);
        }

        // 2. 生成唯一文件名（UUID + 原始扩展名）
        //    UUID = Universally Unique Identifier，全局唯一，避免文件名冲突
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        // 3. 确保上传目录存在
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 4. 保存文件
            Path destPath = uploadDir.resolve(newFilename);
            file.transferTo(destPath.toFile());

            // 5. 返回可访问的 URL（前端通过此 URL 查看图片）
            String url = "/v1/files/" + newFilename;
            log.info("文件上传成功: {} → {}", originalFilename, url);

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            return ApiResult.success(data);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResult.error(500, "文件上传失败，请重试");
        }
    }

    /**
     * 提取文件扩展名
     * "photo.jpg" → "jpg", "没有扩展名" → null
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查扩展名是否在允许列表中
     */
    private boolean isAllowedExtension(String ext) {
        return Arrays.asList(allowedTypes.split(",")).contains(ext);
    }
}
