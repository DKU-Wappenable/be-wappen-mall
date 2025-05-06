package com.wappenable.be.repository;

import com.wappenable.be.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom { // 상품 조회
    Page<Product> searchByConditions(String keyword, String sortBy, String direction, Pageable pageable);
}