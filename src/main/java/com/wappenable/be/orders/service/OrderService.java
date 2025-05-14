package com.wappenable.be.orders.service;

import com.wappenable.be.product.domain.Product;
import com.wappenable.be.orders.domain.Order;
import com.wappenable.be.orders.domain.OrderItem;
import com.wappenable.be.orders.dto.OrderItemRequest;
import com.wappenable.be.orders.dto.OrderRequest;
import com.wappenable.be.orders.repository.OrderRepository;
import com.wappenable.be.product.repository.ProductRepository;
import com.wappenable.be.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    @Transactional
    public void processOrder(OrderRequest request){
        String status = request.getPaymentMethod().equalsIgnoreCase("BANK") ? "WAITING_FOR_DEPOSIT" : "PAID";
        Order order = Order.builder() // 주문 객체 초기화
                .buyerId(request.getBuyerId())
                .status(status)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryRequest(request.getDeliveryRequest())
                .orderedAt(LocalDateTime.now())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // 상품별 주문 항목 처리    
        for (OrderItemRequest itemRequest: request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

            int requestedQty = itemRequest.getQuantity();
            if (product.getStock() < requestedQty) {
                throw new IllegalStateException("재고가 부족합니다 : " + product.getName());
            }

            // 재고 차감
            product.setStock(product.getStock() - requestedQty);

            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(subtotal);

            // 주문 항목 생성 및 주문에 추가
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantity(requestedQty)
                    .unitPrice(unitPrice)
                    .build();

            order.addItem(orderItem);
        }

        // 총액 설정 및 결제 시도
        order.setTotalPrice(total);
        paymentService.processPayment(request.getPaymentMethod(), total);

        // 주문 저장
        orderRepository.save(order);
        
        log.info("[이메일 전송] {}번 사용자에게 주문 확인 이메일 전송됨", request.getBuyerId());
    }

    // BANK로 주문시 WAITING_FOR_DEPOSIT 상태에서 PAID 상태로 바꿈 
    @Transactional
    public void confirmBankDeposit(Long orderId){
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        if (!order.getStatus().equals("WAITING_FOR_DEPOSIT")) {
            throw new IllegalStateException("입금 대기 상태가 아닙니다.");
        }
        order.setStatus("PAID");
        orderRepository.save(order);
    }

    // 주문 상태를 CANCEL로 변경
    @Transactional
    public void cancleOrder(Long orderId){
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        order.setStatus("CANCELED");
        orderRepository.save(order);
    }

    // 주문 데이터를 실제로 삭제
    @Transactional
    public void deleteOrder(Long orderId){
        if(!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("주문을 찾을 수 없습니다.");
        }
        orderRepository.deleteById(orderId);
    }

    
}
