package com.wappenable.be.custom.service;

import com.wappenable.be.custom.domain.CustomizedImage;
import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.dto.CustomizedImageResponse;
import com.wappenable.be.custom.repository.CustomizedImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomImageService {

    private final CustomizedImageRepository customizedImageRepository;

    /**
     * 모든 커스터마이징 이미지 조회
     */
    public List<CustomizedImageResponse> getAllCustomizedImages() {
        return customizedImageRepository.findAll().stream()
                .map(img -> CustomizedImageResponse.builder()
                        .id(img.getId())
                        .customizedImageUrl(img.getCustomizedImageUrl())
                        .title(img.getTitle())
                        .userId(img.getUserId())
                        .createdAt(img.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 커스터마이징 이미지 저장 (Base64 → 파일 저장 + DB 저장)
     */
    public CustomizedImageResponse saveCustomizedImage(CustomizedImageRequest request, Long userId) {
        String imagePath = saveBase64ImageToFile(request.getCustomizedImageUrl());

        CustomizedImage saved = customizedImageRepository.save(
                CustomizedImage.builder()
                        .customizedImageUrl(imagePath)
                        .title(request.getTitle())
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return CustomizedImageResponse.builder()
                .id(saved.getId())
                .customizedImageUrl(saved.getCustomizedImageUrl())
                .title(saved.getTitle())
                .userId(saved.getUserId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Base64 이미지를 파일로 저장
     */
    private String saveBase64ImageToFile(String base64Data) {
        try {
            if (base64Data == null || !base64Data.contains("base64,")) {
                throw new IllegalArgumentException("잘못된 Base64 데이터입니다.");
            }

            String[] parts = base64Data.split(",");
            String metadata = parts[0];
            String base64Image = parts[1];
            String extension = metadata.contains("png") ? ".png" : ".jpg";

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            String fileName = UUID.randomUUID() + extension;
            Path uploadPath = Paths.get("uploads/custom");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            return "/uploads/custom/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 실패", e);
        }
    }

    public void deleteImage(Long id, Long userId) {
        CustomizedImage image = customizedImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."));

        if (!image.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        customizedImageRepository.delete(image);
    }
}
