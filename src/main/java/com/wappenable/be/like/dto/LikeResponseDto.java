package com.wappenable.be.product.dto;

import com.wappenable.be.product.domain.Like;
import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.domain.ProductImage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LikeResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private List<String> imageUrls;
    private int price;         // ✅ 필드 추가
    private String category;   // ✅ 필드 추가
    // 기본 생성자
    public LikeResponseDto() {
    }

    // Like 객체로부터 생성
    public LikeResponseDto(Like like) {
        this.id = like.getId();
        this.productId = like.getProduct().getId();
        this.productName = like.getProduct().getName();
        this.imageUrls = like.getProduct().getProductImages()
                             .stream()
                             .map(ProductImage::getImages) // getUrl → getImages 수정
                             .toList();
        this.price = like.getProduct().getPrice(); // ✅ 추가
        this.category = like.getProduct().getCategory();
    }

    // Product 기반 생성 (id 없음, from() 메서드용)
    public static LikeResponseDto from(Product product, int likeCount) {
        LikeResponseDto dto = new LikeResponseDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setImageUrls(
            product.getProductImages().stream()
                    .map(ProductImage::getImages)
                    .toList()
        );
        dto.setPrice(product.getPrice());              // ✅ 추가
        dto.setCategory(product.getCategory());        // ✅ 추가
        return dto;
    }
}
