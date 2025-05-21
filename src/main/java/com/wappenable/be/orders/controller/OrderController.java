package com.wappenable.be.orders.controller;

import com.wappenable.be.orders.dto.OrderRequest;
import com.wappenable.be.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wappenable.be.orders.domain.Order;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    // 주문
    @PreAuthorize("hasRole('USER')") 
    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
        orderService.processOrder(request);
        return ResponseEntity.ok("주문이 완료되었습니다.");
    }

    // USER ID 기반 주문 목록 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
    return ResponseEntity.ok(orderService.getOrderDtosByUserId(userId));
    }

    // 주문ID 기반 주문 조회 -- 주문 상세정보 페이지
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
    Order order = orderService.getOrderById(orderId);
    return ResponseEntity.ok(orderService.convertToDto(order));
    }
   
    
    // 무통장 입금시 결제 상태를 대기중에서 PAID로 변경
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @PatchMapping("/{orderId}/confirm-deposit")
    public ResponseEntity<String> confirmDeposit(@PathVariable("orderId") Long orderId){
        orderService.confirmBankDeposit(orderId);
        return ResponseEntity.ok("입금 확인 완료, 주문 상태를 PAID로 변경하였습니다.");
    }

    // 주문 취소시 주문 상태를 CANCLEED 로 변경
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancleOrder(@PathVariable Long orderId){
        orderService.cancleOrder(orderId);
        return ResponseEntity.ok("주문이 취소되었습니다.");
    }  

    // 주문 데이터를 실제로 삭제
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId){
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("주문이 삭제되었습니다.");
    
    }
}


