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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/files")
public class FileController {

    private final OssFileService ossFileService;

    @Value("${campus-market.upload.allowed-types}")
    private String allowedTypes;

    @Value("${campus-market.upload.path}")
    private String uploadPath;

    /**
     * 文件上传到 OSS
     */
    @PostMapping("/upload")
    public ApiResult<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResult.error(400, "请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (extension == null || !isAllowedExtension(extension)) {
            return ApiResult.error(400, "不支持的文件类型，允许：" + allowedTypes);
        }

        try {
            String fileUrl = ossFileService.uploadFile(file);
            Map<String, String> data = new HashMap<>();
            data.put("url", fileUrl);
            return ApiResult.success(data);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResult.error(500, "文件上传失败，请重试");
        }
    }

    /**
     * 本地文件访问（兼容迁移前上传的历史图片）
     * GET /v1/files/uuid.jpg → 返回 ./uploads/uuid.jpg
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadPath).resolve(filename);
            if (Files.exists(filePath)) {
                return serveResource(filePath);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 本地缩略图访问（兼容历史缩略图）
     * GET /v1/files/thumb/uuid.jpg → 返回 ./uploads/uuid_thumb.jpg
     */
    @GetMapping("/thumb/{filename:.+}")
    public ResponseEntity<Resource> serveThumb(@PathVariable String filename) {
        try {
            Path thumbPath = Paths.get(uploadPath).resolve(getThumbFilename(filename));
            if (Files.exists(thumbPath)) {
                return serveResource(thumbPath);
            }
            // 缩略图不存在时回退到原图
            Path originalPath = Paths.get(uploadPath).resolve(filename);
            if (Files.exists(originalPath)) {
                return serveResource(originalPath);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getThumbFilename(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? filename + "_thumb.jpg" : filename.substring(0, dot) + "_thumb.jpg";
    }

    private ResponseEntity<Resource> serveResource(Path path) throws IOException {
        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .body(resource);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return null;
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedExtension(String ext) {
        return Arrays.asList(allowedTypes.split(",")).contains(ext);
    }
}
