package com.wappenable.be.product.repository;

import com.wappenable.be.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom { 

} 