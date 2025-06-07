package com.wappenable.be.product.controller;

import com.wappenable.be.product.domain.Like;
import com.wappenable.be.product.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.product.dto.LikeRequestDto;
import com.wappenable.be.product.dto.LikeResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public void like(@RequestBody LikeRequestDto dto) {
        likeService.like(dto.getUserId(), dto.getProductId());
    }
    @Transactional
    @DeleteMapping("/{productId}")
    public void unlike(@PathVariable Long productId, @RequestParam Long userId) {
        likeService.unlike(userId, productId);
    }

    
    @GetMapping
    public List<LikeResponseDto> getUserLikes(@RequestParam Long userId) {
        return likeService.getUserLikedProducts(userId); // ✅ 스트림도, map도 필요 없음!
    }




}
