package com.wappenable.be.cart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.wappenable.be.users.domain.User;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 장바구니에 담은 상품 ID
    private Long productId;

    // DB에서 조회한 상품명
    private String productName;

    // DB에서 조회한 가격
    private int price;

    // 사용자가 지정한 수량
    private int quantity;

    // 커스터마이징된 이미지 URL ("/uploads/custom/uuid1234.png" 등)
    private String customizationImageUrl;

    // 장바구니 항목 생성 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 장바구니 소유 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
