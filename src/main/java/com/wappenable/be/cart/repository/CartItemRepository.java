package com.wappenable.be.cart.repository;

import com.wappenable.be.cart.entity.CartItem;
import com.wappenable.be.users.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {

    List<CartItem> findAllByUser(User user);

}
