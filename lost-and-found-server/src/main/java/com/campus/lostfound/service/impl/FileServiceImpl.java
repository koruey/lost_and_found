package com.campus.lostfound.service.impl;

import cn.hutool.core.util.IdUtil;
import com.campus.lostfound.config.CosConfig;
import com.campus.lostfound.exception.BusinessException;
import com.campus.lostfound.exception.ErrorCode;
import com.campus.lostfound.service.FileService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

/**
 * 文件服务实现 - 腾讯云COS存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp");

    /** 最大文件大小 10MB */
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文件大小不能超过10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_SUPPORTED,
                    "不支持的文件类型，仅支持jpg/png/gif/webp/bmp");
        }

        try {
            // 2. 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String objectKey = IdUtil.fastSimpleUUID() + extension;

            // 3. 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);

            // 4. 上传到COS
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putRequest = new PutObjectRequest(
                        cosConfig.getBucketName(), objectKey, inputStream, metadata);
                cosClient.putObject(putRequest);
            }

            // 5. 返回访问URL（公有读格式）
            String url = String.format("https://%s.cos.%s.myqcloud.com/%s",
                    cosConfig.getBucketName(), cosConfig.getRegion(), objectKey);
            log.info("图片上传成功: {} -> {}", originalName, url);
            return url;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }
}
