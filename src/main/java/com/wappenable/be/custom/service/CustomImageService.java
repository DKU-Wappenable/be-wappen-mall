package com.wappenable.be.custom.service;

import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.dto.ProductImageResponse;
import com.wappenable.be.custom.domain.CustomizedImage;
import com.wappenable.be.custom.repository.CustomizedImageRepository;
import com.wappenable.be.custom.repository.ProductImageRepository;
import com.wappenable.be.product.domain.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


// 이미지 데이터를 가공해서 컨트롤러에 제공하는 서비스 클래스
@Service
@RequiredArgsConstructor
public class CustomImageService {

    private final ProductImageRepository productImageRepository;
    private final CustomizedImageRepository customizedImageRepository;

    // 모든 상품 이미지를 DTO 형태로 반환
    public List<ProductImageResponse> getAllImages() {
        return productImageRepository.findAll().stream()
            .map(image -> ProductImageResponse.builder()
                    .productId(image.getProduct().getId())
                    .imageUrl(image.getImages())
                    .build())
            .collect(Collectors.toList());        
    }

    public void saveCustomizedImage(CustomizedImageRequest request) {
        CustomizedImage customizedImage = CustomizedImage.builder()
            .customizedImageUrl(request.getCustomizedImageUrl())
            .title(request.getTitle())
            .userId(request.getUserId())
            .build();
        customizedImageRepository.save(customizedImage);
    }
 }
