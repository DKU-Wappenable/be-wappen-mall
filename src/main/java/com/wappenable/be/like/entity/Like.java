package com.wappenable.be.product.domain;

import jakarta.persistence.*;
import lombok.*;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.product.domain.Product;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;
}
