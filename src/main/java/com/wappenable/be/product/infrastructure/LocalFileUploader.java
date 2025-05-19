package com.wappenable.be.product.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileUploader implements FileUploader {

    private final String uploadDir = "uploads";
    // 단일 상품 등록
    @Override
    public String upload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path savePath = Paths.get(uploadDir, fileName);

            Files.createDirectories(savePath.getParent());

            Files.copy(file.getInputStream(), savePath);
            log.info("파일 저장완료: {}", savePath.toString());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    // 대용량 파일 업로드 용
    @Override
    public String upload(String fileName, byte[] fileData) {
        try {
            String uniqueName = UUID.randomUUID() + "_" + fileName;
            Path savePath = Paths.get(uploadDir, uniqueName);
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, fileData);
            return savePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패: " + fileName, e);
        }
    }
}