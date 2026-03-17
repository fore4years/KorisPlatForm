package com.generator.rental.controller;

import com.generator.rental.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@PreAuthorize("isAuthenticated()")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "pdf");
    // 2MB
    private static final long MAX_SIZE_AVATAR = 2 * 1024 * 1024;
    // 5MB
    private static final long MAX_SIZE_DEFAULT = 5 * 1024 * 1024;

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param type 文件类型 (common, avatar)
     * @return 包含文件URL等信息的 Map
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {

        try {
            // 1. Validation
            if (file.isEmpty()) {
                throw new RuntimeException("文件为空");
            }

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = StringUtils.getFilenameExtension(originalFilename).toLowerCase();

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new RuntimeException("不支持的文件类型: " + extension);
            }

            long size = file.getSize();
            if ("avatar".equals(type) && size > MAX_SIZE_AVATAR) {
                throw new RuntimeException("头像大小不能超过2MB");
            } else if (size > MAX_SIZE_DEFAULT) {
                throw new RuntimeException("文件大小不能超过5MB");
            }

            // 2. Prepare Directory
            Path rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path targetDir = rootLocation.resolve(type);
            Files.createDirectories(targetDir);

            // 3. Save File
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path targetPath = targetDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Generate URL
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/files/")
                    .path(type + "/")
                    .path(newFilename)
                    .toUriString();

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("filename", newFilename);
            response.put("originalFilename", originalFilename);

            return Result.success(response);

        } catch (IOException ex) {
            throw new RuntimeException("文件上传失败: " + ex.getMessage());
        }
    }
}
