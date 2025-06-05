package com.wappenable.be.product.dto;

import com.wappenable.be.product.domain.Product;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private int price;
    private int stock;
    private String category;
    private String description;

    private List<String> imageUrls;

    public static ProductResponseDto from(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .description(product.getDescription())
                .imageUrls(
                        product.getProductImages().stream()
                                .map(img -> img.getImages())
                                .collect(Collectors.toList())
                )
                .build();
    }
}
