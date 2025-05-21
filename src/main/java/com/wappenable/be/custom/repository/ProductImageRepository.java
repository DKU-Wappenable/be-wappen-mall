package com.wappenable.be.custom.repository;

import com.wappenable.be.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>{
    List<ProductImage> findAll();

}