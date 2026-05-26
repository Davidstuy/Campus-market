package com.campusmarket.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OssConfig {

    @Value("${campus-market.upload.oss.endpoint}")
    private String endpoint;

    @Value("${campus-market.upload.oss.access-key-id}")
    private String accessKeyId;

    @Value("${campus-market.upload.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${campus-market.upload.oss.bucket-name}")
    private String bucketName;

    @Value("${campus-market.upload.oss.domain}")
    private String domain;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
