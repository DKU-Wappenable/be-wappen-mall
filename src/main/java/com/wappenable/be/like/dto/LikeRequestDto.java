package com.wappenable.be.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequestDto {
    private Long userId;
    private Long productId;
}
