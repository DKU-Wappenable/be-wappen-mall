package com.wappenable.be.orders.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private Long buyerId;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime orderedAt;
    private String deliveryAddress;
    private String deliveryRequest;
    private List<OrderItemResponse> items;

}
