package com.wappenable.be.custom.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductImageResponse { // 상품 이미지 정보를 응답으로 전달할 DTO
    private Long productId;
    private String imageUrl;
}
