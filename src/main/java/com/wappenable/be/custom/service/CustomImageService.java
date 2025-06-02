
package com.wappenable.be.custom.service;

import com.wappenable.be.custom.domain.CustomizedImage;
import com.wappenable.be.custom.dto.CustomizedImageResponse;
import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.repository.CustomizedImageRepository;
import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomImageService {

    private final CustomizedImageRepository customizedImageRepository;
    private final ProductRepository productRepository;   // 원본 상품 검증(선택)
    private final ObjectMapper objectMapper;             // 필요하다면 JSON 처리

    /**
     * (1) DB에 저장된 모든 CustomizedImage를 조회해서 DTO 리스트로 반환
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
     * (2) 사용자가 보낸 요청(request)에 따라 커스터마이징 이미지를 저장
     */
    public CustomizedImageResponse saveCustomizedImage(CustomizedImageRequest request, Long userId) {
        // (선택) 원본 상품 검증: request.getOriginalProductId()가 실제 존재하는 product인지 확인
        if (request.getOriginalProductId() != null) {
            productRepository.findById(request.getOriginalProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. (originalProductId=" + request.getOriginalProductId() + ")"));
        }

        CustomizedImage entity = CustomizedImage.builder()
                .customizedImageUrl(request.getCustomizedImageUrl())   // 로컬 저장된 경로 또는 URL
                .title(request.getTitle())
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .originalProductId(request.getOriginalProductId())
                .build();

        CustomizedImage saved = customizedImageRepository.save(entity);

        // 저장 후 DTO로 변환해서 리턴
        return CustomizedImageResponse.builder()
                .id(saved.getId())
                .customizedImageUrl(saved.getCustomizedImageUrl())
                .title(saved.getTitle())
                .userId(saved.getUserId())
                .createdAt(saved.getCreatedAt())
                .originalProductId(saved.getOriginalProductId())
                .build();
    }
}
