package com.wappenable.be.orders.dto;

import com.wappenable.be.orders.domain.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private int quantity;
    private BigDecimal unitPrice;

}
