package com.wappenable.be.cart.controller;

import com.wappenable.be.cart.dto.CartItemRequestDto;
import com.wappenable.be.cart.dto.CartItemResponseDto;
import com.wappenable.be.cart.dto.CartItemUpdateRequestDto;
import com.wappenable.be.cart.service.CartItemService;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.users.domain.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.wappenable.be.cart.dto.CartSummaryResponseDto;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<Void> addToCart(
            @RequestBody CartItemRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        cartItemService.addToCart(dto, user);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    @@PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<CartSummaryResponseDto> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        CartSummaryResponseDto summary = cartItemService.getCartWithTotal(user);
        return ResponseEntity.ok(summary);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        cartItemService.removeItem(id, user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{cartItemId}")
    @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
    public ResponseEntity<Void> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody CartItemUpdateRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        cartItemService.updateCartItemQuantity(user, cartItemId, dto.getQuantity());
        return ResponseEntity.ok().build();
    }
     // 결제 (Mock)
     @PostMapping("/checkout")
     @PreAuthorize("hasAnyRole('USER','SHOP_OWNER','ADMIN')")
     public ResponseEntity<String> checkout(@AuthenticationPrincipal CustomUserDetails userDetails) {
         User user = userDetails.getUser();
         int total = cartItemService.getCartWithTotal(user).getTotalAmount();
         return ResponseEntity.ok("총 " + total + "원 결제 완료 (Mock)");
     }
}
