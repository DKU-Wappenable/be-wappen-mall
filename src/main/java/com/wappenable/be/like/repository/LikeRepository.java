package com.wappenable.be.product.repository;

import com.wappenable.be.product.domain.Like;
import com.wappenable.be.product.domain.Product;
import com.wappenable.be.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndProduct(User user, Product product);
    List<Like> findByUser(User user);
    int countByProduct(Product product);
    void deleteByUserAndProduct(User user, Product product);
}
