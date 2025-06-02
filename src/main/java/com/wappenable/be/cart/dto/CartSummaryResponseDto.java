package com.wappenable.be.cart.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
public class CartSummaryResponseDto {
    private int totalAmount;
    private List<CartItemResponseDto> items;
}
