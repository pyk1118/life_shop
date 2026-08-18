package com.lifeshop.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.lifeshop.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS文件上传服务类
 * 提供图片上传到OSS云端的功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final OssProperties ossProperties;

    /**
     * 允许的图片文件类型
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    };

    /**
     * 允许的图片文件扩展名
     */
    private static final String[] ALLOWED_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".webp"
    };

    /**
     * 最大文件大小：2MB
     */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    /**
     * 上传图片到OSS
     * 
     * @param file 上传的文件
     * @param directory OSS存储目录（如：avatars, logos, goods）
     * @return OSS访问URL（HTTPS）
     * @throws IOException 上传失败时抛出异常
     */
    public String uploadImage(MultipartFile file, String directory) throws IOException {
        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 2. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }

        // 3. 文件类型校验
        String contentType = file.getContentType();
        if (!isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("只支持上传JPG/PNG/WEBP格式的图片");
        }

        // 4. 文件扩展名校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("只支持JPG/PNG/WEBP格式的图片");
        }

        // 5. 生成唯一文件名（UUID + 原扩展名）
        String filename = UUID.randomUUID().toString() + extension;
        String objectKey = directory + "/" + filename;

        // 6. 创建OSS客户端
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
            );

            // 7. 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(file.getSize());

            // 8. 上传文件到OSS
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putRequest = new PutObjectRequest(
                ossProperties.getBucketName(),
                objectKey,
                inputStream,
                metadata
            );
            ossClient.putObject(putRequest);

            // 9. 构建HTTPS访问URL
            String url = buildHttpsUrl(objectKey);
            log.info("文件上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new IOException("文件上传失败: " + e.getMessage());
        } finally {
            // 10. 关闭OSS客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 判断是否为允许的图片类型
     */
    private boolean isAllowedImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为允许的文件扩展名
     */
    private boolean isAllowedExtension(String extension) {
        if (extension == null) {
            return false;
        }
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 构建HTTPS访问URL
     * 使用bucket的域名格式：https://{bucket-name}.{endpoint}/{object-key}
     */
    private String buildHttpsUrl(String objectKey) {
        String endpoint = ossProperties.getEndpoint();
        // 移除协议前缀（http://或https://）
        String domain = endpoint.replaceFirst("^https?://", "");
        // 构建HTTPS URL
        return "https://" + ossProperties.getBucketName() + "." + domain + "/" + objectKey;
    }
}
