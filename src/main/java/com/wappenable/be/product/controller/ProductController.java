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

// Swagger 애노테이션 추가
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Product API", description = "상품 관련 API - 상품 등록, 조회, 수정, 삭제 기능을 제공합니다")
public class ProductController {

    private final ProductService productService;

    // ============================== 개발용 ==============================
    @PostMapping // 상품 등록 api
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @Operation(summary = "상품 등록", description = "새로운 상품을 등록합니다. 상점 소유자 또는 관리자만 접근 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상품 등록 성공",
                content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음 - 상점 소유자 또는 관리자만 접근 가능")
    })
    public ResponseEntity<?> createProduct( 
            @Parameter(description = "상품명", required = true) @RequestParam String name,
            @Parameter(description = "상품 가격", required = true) @RequestParam int price,
            @Parameter(description = "재고 수량", required = true) @RequestParam int stock,
            @Parameter(description = "상품 이미지 파일들", required = true) @RequestParam("images") MultipartFile[] images,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
    
        Long sellerId = userDetails.getId() ; // 로그인 된 사용자 Id
        Product saved = productService.createProduct(name, price, stock, images, sellerId);
        return ResponseEntity.ok(saved);
    }

    // 상품 수정 api
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @Operation(summary = "상품 수정", description = "기존 상품 정보를 수정합니다. 상점 소유자 또는 관리자만 접근 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상품 수정 성공",
                content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    public ResponseEntity<?> updateProduct (
        @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
        @Parameter(description = "상품명", required = true) @RequestParam String name,
        @Parameter(description = "상품 가격", required = true) @RequestParam int price,
        @Parameter(description = "재고 수량", required = true) @RequestParam int stock,
        @Parameter(description = "상품 이미지 파일들 (선택사항)") @RequestParam(value = "images", required = false) MultipartFile[] images,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long sellerId = userDetails.getId();
        Product updated = productService.updateProduct(id,name,price,stock,images,sellerId);
        return ResponseEntity.ok(updated);
    }
    

    // 상품 삭제 api
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다. 상점 소유자 또는 관리자만 접근 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
    public ResponseEntity<?> deleteProduct(
        @Parameter(description = "상품 ID", required = true) @PathVariable Long id,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long sellerId = userDetails.getId();
        productService.deleteProduct(id,sellerId);
        return ResponseEntity.ok("상품이 삭제 되었습니다.");
    } 

    // 상품 조회 api
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "상품 목록을 페이지네이션과 검색, 정렬 기능과 함께 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공",
                content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Product>> getProduct(
        @Parameter(description = "검색 키워드 (상품명 기준)") @RequestParam(required = false) String keyword,
        @Parameter(description = "정렬 기준", example = "createdAt") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
        @Parameter(description = "정렬 방향", example = "desc") @RequestParam(required = false, defaultValue = "desc") String direction,
        @Parameter(description = "페이지네이션 정보") Pageable pageable
     ) {
        return ResponseEntity.ok(productService.getProduct(keyword,sortBy, direction,pageable));
     }

     // 상품 상세 조회 
     @GetMapping("/{id}")
     @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보를 조회합니다.")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공",
                 content = @Content(schema = @Schema(implementation = Product.class))),
         @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
     })
     public ResponseEntity<Product> getProductDetail(
         @Parameter(description = "상품 ID", required = true) @PathVariable Long id
     ){
        return ResponseEntity.ok(productService.getProductDetail(id));
     }

     // 상품 대량 등록
     @PostMapping("/bulk")
     @PreAuthorize("hasAnyRole('SHOP_OWNER', 'ADMIN')") 
     @Operation(summary = "상품 대량 등록", description = "CSV 파일과 이미지 ZIP 파일을 사용하여 상품을 대량으로 등록합니다.")
     @SecurityRequirement(name = "Bearer Authentication")
     @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "대량 등록 성공"),
         @ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
         @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
         @ApiResponse(responseCode = "403", description = "권한 없음")
     })
     public ResponseEntity<?> bulkUpload(
        @Parameter(description = "상품 정보가 포함된 CSV 파일", required = true) @RequestParam("csvFile") MultipartFile csvFile, 
        @Parameter(description = "상품 이미지들이 포함된 ZIP 파일 (선택사항)") @RequestParam(value = "zipFile", required = false) MultipartFile zipFile,
        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
     ) {
        Long sellerId = userDetails.getId();
        List<Map<String,Object>> result = productService.bulkUpload(csvFile,zipFile,sellerId);
        return ResponseEntity.ok(Map.of("results",result));
     }
    
    }