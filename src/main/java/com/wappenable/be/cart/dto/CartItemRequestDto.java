package com.wappenable.be.cart.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CartItemRequestDto {

    private Long productId;
    private int quantity;
    private String customizationImageUrl;
}
