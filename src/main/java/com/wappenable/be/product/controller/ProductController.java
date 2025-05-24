package com.wappenable.be.product.controller;

import com.wappenable.be.product.domain.Product;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // ============================== 개발용 ==============================
    @PostMapping // 상품 등록 api
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    public ResponseEntity<?> createProduct( 
            @RequestParam String name,
            @RequestParam int price,
            @RequestParam int stock,
            @RequestParam("images") MultipartFile[] images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
    
        Long sellerId = userDetails.getId() ; // 로그인 된 사용자 Id
        Product saved = productService.createProduct(name, price, stock, images, sellerId);
        return ResponseEntity.ok(saved);
    }

    // 상품 수정 api
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    public ResponseEntity<?> updateProduct (
        @PathVariable Long id,
        @RequestParam String name,
        @RequestParam int price,
        @RequestParam int stock,
        @RequestParam(value = "images", required = false) MultipartFile[] images,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long sellerId = userDetails.getId();
        Product updated = productService.updateProduct(id,name,price,stock,images,sellerId);
        return ResponseEntity.ok(updated);
    }
    

    // 상품 삭제 api
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    public ResponseEntity<?> deleteProduct(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long sellerId = userDetails.getId();
        productService.deleteProduct(id,sellerId);
        return ResponseEntity.ok("상품이 삭제 되었습니다.");
    } 

    // 상품 조회 api
    @GetMapping
    public ResponseEntity<Page<Product>> getProduct(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false, defaultValue = "desc") String direction,
        Pageable pageable
     ) {
        return ResponseEntity.ok(productService.getProduct(keyword,sortBy, direction,pageable));
     }

     // 상품 상세 조회 
     @GetMapping("/{id}")
     public ResponseEntity<Product> getProductDetail(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductDetail(id));
     }

     // 상품 대량 등록
     @PostMapping("/bulk")
     @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
     public ResponseEntity<?> bulkUpload(
        @RequestParam("csvFile") MultipartFile csvFile, 
        @RequestParam(value = "zipFile", required = false) MultipartFile zipFile,
        @AuthenticationPrincipal CustomUserDetails userDetails
     ) {
        Long sellerId = userDetails.getId();
        List<Map<String,Object>> result = productService.bulkUpload(csvFile,zipFile,sellerId);
        return ResponseEntity.ok(Map.of("results",result));
     }
    
    }