package com.wappenable.be.custom.repository;


import com.wappenable.be.custom.domain.CustomizedImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomizedImageRepository extends JpaRepository<CustomizedImage, Long> {
}