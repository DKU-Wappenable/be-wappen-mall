package com.wappenable.be.orders.dto;
import lombok.Data;
import java.util.List;
@Data
public class OrderItemRequest {
        private Long productId;
        private Integer quantity;
   
}
