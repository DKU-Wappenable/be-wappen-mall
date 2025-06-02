package com.wappenable.be.custom.controller;

import com.wappenable.be.custom.dto.ProductImageResponse;
import com.wappenable.be.custom.dto.CustomizedImageRequest;
import com.wappenable.be.custom.service.CustomImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.custom.dto.CustomizedImageResponse;

import java.util.List;

// 상품 커스터마이징을 위한 이미지 API 컨트롤러
@RestController
@RequestMapping("/api/custom-images")
@RequiredArgsConstructor

public class CustomImageController {
    private final CustomImageService customImageService;

    // 모든 상품 이미지 리스트를 반환
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<List<CustomizedImageResponse>> getAllCustomImages() {
        List<CustomizedImageResponse> responseList = customImageService.getAllCustomizedImages();
        return ResponseEntity.ok(responseList);
    }
    /*
    커스터마이징 완료된 이미지를 Base64 형태로 받아서 로컬 저장 후 DTO 반환
     → 프론트에서 “커스터마이징 화면”에서 저장 버튼을 눌렀을 때 호출
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<CustomizedImageResponse> uploadCustomizedImage(
            @RequestBody CustomizedImageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        CustomizedImageResponse resp = customImageService.saveCustomizedImage(request, userId);
        return ResponseEntity.ok(resp);
    }
}