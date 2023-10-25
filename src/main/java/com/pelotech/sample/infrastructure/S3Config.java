package com.pelotech.sample.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    private static final String AWS_REGION = "us-west-2";

    @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(AWS_REGION)
                .build();
    }

}
