package com.wappenable.be.orders.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;

@Entity
@Table(name= "orders")
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long buyerId;
    private BigDecimal totalPrice;
    // @Enumerated(EnumType.STRING)
    private String status;
    private LocalDateTime orderedAt;
    private String deliveryAddress;
    private String deliveryRequest;

    @OneToMany(mappedBy ="order", cascade= CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item){
        item.setOrder(this);
        this.items.add(item);   
    }

}
