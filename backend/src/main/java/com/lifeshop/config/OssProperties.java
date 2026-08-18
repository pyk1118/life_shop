package com.lifeshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性类
 * 从application.yml中读取aliyun.oss配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    
    /**
     * OSS访问端点
     */
    private String endpoint;
    
    /**
     * OSS访问密钥ID
     */
    private String accessKeyId;
    
    /**
     * OSS访问密钥Secret
     */
    private String accessKeySecret;
    
    /**
     * OSS存储桶名称
     */
    private String bucketName;
}
