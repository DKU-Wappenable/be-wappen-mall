package com.wappenable.be.service;

import com.wappenable.be.domain.Product;
import com.wappenable.be.repository.ProductRepository;
import com.wappenable.be.infrastructure.LocalFileUploader;
import com.wappenable.be.infrastructure.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final LocalFileUploader fileUploader; // 개발용
    private final S3Uploader s3Uploader; // 배포용 (현재 미사용)

    // ============================== 개발용 ==============================

    // 상품 등록
    public Product createProduct(String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty()) {
                String imageUrl = fileUploader.upload(file); 
                imageUrls.add(imageUrl);
            }
        }

        Product product = Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .images(imageUrls)
                .sellerId(sellerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return productRepository.save(product);
    }

    // 상품 수정
    public Product updateProduct(Long id, String name, int price, int stock, MultipartFile[] images) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUpdatedAt(LocalDateTime.now());

        if (images != null) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String imageUrl = fileUploader.upload(file); 
                    imageUrls.add(imageUrl);
                }
            }
            product.setImages(imageUrls);
        }

        return productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        productRepository.delete(product);
    }

    // ============================== 배포용 (현재 주석 처리 상태) ==============================

    /*
    // 상품 등록
    public Product createProduct(String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        List<String> imageUrls = s3Uploader.uploadFiles(images, "products"); 
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .images(imageUrls)
                .sellerId(sellerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return productRepository.save(product);
    }

    // 상품 수정
    public Product updateProduct(Long id, String name, int price, int stock, MultipartFile[] images, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setUpdatedAt(LocalDateTime.now());

        if (images != null) {
            List<String> imageUrls = s3Uploader.uploadFiles(images, "products");
            product.setImages(imageUrls);
        }

        return productRepository.save(product);
    }

    // 상품 삭제
    public void deleteProduct(Long id, Long sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        if (!product.getSellerId().equals(sellerId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }

        productRepository.delete(product);
    }
    */

}
