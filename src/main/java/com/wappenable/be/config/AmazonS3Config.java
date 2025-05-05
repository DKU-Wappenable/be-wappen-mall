package com.wappenable.be.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion("ap-northeast-2")
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("YOUR_ACCESS_KEY", "YOUR_SECRET_KEY")
                ))
                .build();
    }
}
