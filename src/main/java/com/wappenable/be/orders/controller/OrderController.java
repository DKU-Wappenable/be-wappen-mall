package com.wappenable.be.orders.controller;

import com.wappenable.be.orders.dto.OrderRequest;
import com.wappenable.be.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.wappenable.be.orders.domain.Order;
import java.util.List;
import com.wappenable.be.orders.dto.OrderResponse;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    // 주문
    @PreAuthorize("hasRole('USER')") 
    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long buyerId = userDetails.getId();
        orderService.processOrder(request,userDetails.getId());
        return ResponseEntity.ok("주문이 완료되었습니다.");
    }

    // 전체 주문 목록 조회 (관리자, 가게 사장)
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOP_OWNER')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // 본인 주문 목록 조회 (사용자 전용)
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        return ResponseEntity.ok(orderService.getOrderDtosByUserId(userId));
    }
   
    //  주문 상세 조회 (본인 또는 관리자, 사장 허용)
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long orderId,@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderDetailWithAccessCheck(orderId, userDetails));
    }

    
    // 무통장 입금시 결제 상태를 대기중에서 PAID로 변경
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @PatchMapping("/{orderId}/confirm-deposit")
    public ResponseEntity<String> confirmDeposit(@PathVariable("orderId") Long orderId){
        orderService.confirmBankDeposit(orderId);
        return ResponseEntity.ok("입금 확인 완료, 주문 상태를 PAID로 변경하였습니다.");
    }

    // 주문 취소시 주문 상태를 CANCELED 로 변경
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getId();
    orderService.cancelOrder(orderId, userId); // userId로 본인 여부 검증 포함
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