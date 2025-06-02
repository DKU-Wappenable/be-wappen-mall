package com.wappenable.be.cart.dto;

import lombok.Data;

@Data
public class CartItemUpdateRequestDto {
    private int quantity;    // 수정할 새 수량
}
