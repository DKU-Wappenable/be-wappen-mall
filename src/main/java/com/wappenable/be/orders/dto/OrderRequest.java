
package com.wappenable.be.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
    private String paymentMethod; 
    private String deliveryAddress;
    private String deliveryRequest;
}