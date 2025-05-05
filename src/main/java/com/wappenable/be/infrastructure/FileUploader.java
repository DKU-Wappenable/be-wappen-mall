package com.wappenable.be.infrastructure;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploader {
    String upload(MultipartFile file);
}
