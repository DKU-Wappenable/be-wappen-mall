package com.wappenable.be.orders.repository;

import com.wappenable.be.orders.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OrderRepository extends JpaRepository<Order, Long> {

}