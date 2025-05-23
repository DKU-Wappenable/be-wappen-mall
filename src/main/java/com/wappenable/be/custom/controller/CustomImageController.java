package com.wappenable.be.custom.controller;

import com.wappenable.be.custom.dto.ProductImageResponse;
import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.service.CustomImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

// 상품 커스터마이징을 위한 이미지 API 컨트롤러
@RestController
@RequestMapping("/api/custom-images")
@RequiredArgsConstructor

public class CustomImageController {
    private final CustomImageService customImageService;

    // 모든 상품 이미지 리스트를 반환
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER', 'ADMIN')")
    public ResponseEntity<List<ProductImageResponse>> getAllCustomImages() {
        return ResponseEntity.ok(customImageService.getAllImages());
    }

    // 커스터마이징 결과를 저장
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('USER', 'SHOP_OWNER', 'ADMIN')")
    public ResponseEntity<String> saveCustomizedImage(@RequestBody CustomizedImageRequest request){
        customImageService.saveCustomizedImage(request);
        return ResponseEntity.ok("커스터마이징 이미지 저장 완료");
    }
}