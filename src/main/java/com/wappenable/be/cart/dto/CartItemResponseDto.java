package com.wappenable.be.cart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponseDto {

    private Long id;
    private String productName;
    private int price;
    private int quantity;
    private String customizationImageUrl;
    
   
}
