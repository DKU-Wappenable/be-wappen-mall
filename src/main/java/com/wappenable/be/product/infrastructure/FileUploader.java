package com.wappenable.be.product.infrastructure;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {
    String upload(MultipartFile file);
    String upload(String fileName, byte[] fileData);

    void delete(String fileUrl); // 삭제 기능 추가
} 