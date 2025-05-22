package com.wappenable.be.product.controller;

import com.wappenable.be.product.domain.Product;
import com.wappenable.be.security.CustomUserDetails;
import com.wappenable.be.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;  
// import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // ============================== 개발용 ==============================
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @PostMapping // 상품 등록 api
    public ResponseEntity<?> createProduct( 
            @RequestParam String name,
            @RequestParam int price,
            @RequestParam int stock,
            @RequestParam("images") MultipartFile[] images,
            @RequestParam(required = false) Long sellerId
    ) {
        if (sellerId == null) {
            sellerId = 1L; // 로그인 기능 전 임시 sellerId
        }

        Product saved = productService.createProduct(name, price, stock, images, sellerId);
        return ResponseEntity.ok(saved);
    }

    // 상품 수정 api
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct (
        @PathVariable Long id,
        @RequestParam String name,
        @RequestParam int price,
        @RequestParam int stock,
        @RequestParam(value = "images", required = false) MultipartFile[] images,
        @RequestParam(required = false) Long sellerId
    ) { 
        if (sellerId == null) {
            sellerId =1L;
        }
        Product updated = productService.updateProduct(id,name,price,stock,images);
        return ResponseEntity.ok(updated);
    }

    // 상품 삭제 api
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
        @PathVariable Long id,
        @RequestParam(required = false) Long sellerId
        ) {
            if(sellerId == null) {
                sellerId = 1L;
            }
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
     @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
     @PostMapping("/bulk")
     public ResponseEntity<?> bulkUpload(
        @RequestParam("csvFile") MultipartFile csvFile, 
        @RequestParam(value = "zipFile", required = false) MultipartFile zipFile,
        @RequestParam(required = false) Long sellerId
     ) {
        if (sellerId == null) sellerId =1L;

        List<Map<String,Object>> result = productService.bulkUpload(csvFile,zipFile,sellerId);
        return ResponseEntity.ok(Map.of("results",result));
     }


    // ============================== 배포용 (주석처리 상태) ==============================
    /*
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam String name,
            @RequestParam int price,
            @RequestParam int stock,
            @RequestParam("images") MultipartFile[] images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (!userDetails.hasAnyRole("ROLE_DESIGNER", "ROLE_SHOP_OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한 없음");
        }

        Long sellerId = userDetails.getId();

        Product saved = productService.createProduct(name, price, stock, images, sellerId);
        return ResponseEntity.ok(saved);
    }
    
       @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
        @PathVariable Long id,
        @RequestParam String name,
        @RequestParam int price,
        @RequestParam int stock,
        @RequestParam(value = "images", required = false) MultipartFile[] images,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Product updated = productService.updateProduct(id, name, price, stock, images, userDetails.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
        @PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        productService.deleteProduct(id, userDetails.getId());
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }
         @GetMapping("/{id}")
     public ResponseEntity<ProductResponse> getProductDetail(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductDetail(id));
     }

       // 상품 대량 등록
     @PostMapping("/bulk")
     public ResponseEntity<?> bulkUpload(
        @RequestParam("csvFile") MultipartFile csvFile, 
        @RequestParam(value = "zipFile", required = false) MultipartFile zipFile,
        @RequestParam(required = false) Long sellerId
     ) {
        if (sellerId == null) sellerId =1L;

        List<Map<String,Object>> result = productService.bulkUpload(csvFile,zipFile,sellerId);
        return ResponseEntity.ok(Map.of("results",result));
     }
    */

} 