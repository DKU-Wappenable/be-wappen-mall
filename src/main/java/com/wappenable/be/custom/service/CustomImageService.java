package com.wappenable.be.custom.service;

import com.wappenable.be.custom.domain.CustomizedImage;
import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.dto.CustomizedImageResponse;
import com.wappenable.be.custom.repository.CustomizedImageRepository;
import com.wappenable.be.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final ProductRepository productRepository;

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
                        .originalProductId(img.getOriginalProductId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 커스터마이징 이미지 저장 (Base64 → 파일 저장 + DB 저장)
     */
    public CustomizedImageResponse saveCustomizedImage(CustomizedImageRequest request, Long userId) {
        // optional: 상품 유효성 확인
        if (request.getOriginalProductId() != null) {
            productRepository.findById(request.getOriginalProductId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + request.getOriginalProductId()));
        }

        // Base64 → 실제 이미지 파일 저장
        String imagePath = saveBase64ImageToFile(request.getCustomizedImageUrl());

        // DB 저장
        CustomizedImage saved = customizedImageRepository.save(
                CustomizedImage.builder()
                        .customizedImageUrl(imagePath) // ex) /uploads/custom/uuid.png
                        .title(request.getTitle())
                        .originalProductId(request.getOriginalProductId())
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return CustomizedImageResponse.builder()
                .id(saved.getId())
                .customizedImageUrl(saved.getCustomizedImageUrl())
                .title(saved.getTitle())
                .originalProductId(saved.getOriginalProductId())
                .userId(saved.getUserId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Base64 이미지를 uploads/custom 폴더에 저장하고, 해당 파일 경로 반환
     */
    private String saveBase64ImageToFile(String base64Data) {
        try {
            if (base64Data == null || !base64Data.contains("base64,")) {
                throw new IllegalArgumentException("잘못된 Base64 데이터입니다.");
            }

            String[] parts = base64Data.split(",");
            String metadata = parts[0]; // data:image/png;base64
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

            return "/uploads/custom/" + fileName; // 웹에서 접근 가능한 상대 경로
        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 실패", e);
        }
    }
}
