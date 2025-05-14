package com.wappenable.be.product.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {
    
    private final AmazonS3 amazonS3;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<String> uploadFiles(MultipartFile[] files, String dirName) {
        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            result.add(upload(file,dirName));

        }
        return result;
    }

    public String upload(MultipartFile file, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }

}
