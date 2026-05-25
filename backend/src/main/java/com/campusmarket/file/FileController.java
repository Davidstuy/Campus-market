package com.campusmarket.file;

import com.campusmarket.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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

            // 4.1 生成缩略图（宽高不超过 400px，JPEG 格式）
            generateThumbnail(destPath, extension);

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

    /**
     * 缩略图访问接口
     * GET /v1/files/thumb/uuid.jpg → 返回 uuid_thumb.jpg
     */
    @GetMapping("/thumb/{filename:.+}")
    public ResponseEntity<Resource> getThumb(@PathVariable String filename) {
        try {
            Path thumbPath = Paths.get(uploadPath).resolve(getThumbFilename(filename));
            if (Files.exists(thumbPath)) {
                return serveFile(thumbPath);
            }
            // 缩略图不存在时回退到原图
            Path originalPath = Paths.get(uploadPath).resolve(filename);
            if (Files.exists(originalPath)) {
                return serveFile(originalPath);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 生成缩略图（最大 400px，JPEG 格式）
     * WebP 和 GIF 跳过（Java 原生不支持 WebP；GIF 动图保留原样）
     */
    private void generateThumbnail(Path originalPath, String extension) {
        if ("webp".equals(extension) || "gif".equals(extension)) return;

        try {
            BufferedImage original = ImageIO.read(originalPath.toFile());
            if (original == null) return;

            int maxSize = 400;
            int w = original.getWidth();
            int h = original.getHeight();
            if (w <= maxSize && h <= maxSize) return; // 已经很小，不需要缩略图

            double ratio = Math.min((double) maxSize / w, (double) maxSize / h);
            int newW = (int) (w * ratio);
            int newH = (int) (h * ratio);

            BufferedImage thumb = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumb.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, newW, newH);
            g.drawImage(original, 0, 0, newW, newH, null);
            g.dispose();

            Path thumbPath = originalPath.resolveSibling(getThumbFilename(originalPath.getFileName().toString()));
            ImageIO.write(thumb, "jpg", thumbPath.toFile());
        } catch (IOException e) {
            log.warn("缩略图生成失败: {}", originalPath, e);
        }
    }

    private String getThumbFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? filename + "_thumb.jpg" : filename.substring(0, dot) + "_thumb.jpg";
    }

    private ResponseEntity<Resource> serveFile(Path path) throws IOException {
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .body(resource);
    }
}
