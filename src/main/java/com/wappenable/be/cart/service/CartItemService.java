package com.wappenable.be.cart.service;

import com.wappenable.be.cart.dto.CartItemRequestDto;
import com.wappenable.be.cart.dto.CartItemResponseDto;
import com.wappenable.be.cart.entity.CartItem;
import com.wappenable.be.cart.repository.CartItemRepository;
import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.repository.ProductRepository;
import com.wappenable.be.users.domain.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.wappenable.be.cart.dto.CartSummaryResponseDto;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void addToCart(CartItemRequestDto dto, User user) {
        try {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));
    
            CartItem item = CartItem.builder()
                    .productId(dto.getProductId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(dto.getQuantity())
                    .customizationImageUrl(dto.getCustomizationImageUrl())
                    .user(user)
                    .build();
    
            cartItemRepository.save(item);
            System.out.println("[DEBUG] 장바구니 저장 성공");
    
        } catch (Exception e) {
            System.out.println("[ERROR] addToCart 실패: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public List<CartItemResponseDto> getCartItems(User user) {
        return cartItemRepository.findAllByUser(user).stream()
                .map(item -> CartItemResponseDto.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .customizationImageUrl(item.getCustomizationImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeItem(Long id, User user) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public void updateCartItemQuantity(User user, Long cartItemId, int newQuantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }

        cartItem.setQuantity(newQuantity);
    
    
    }
    
    @Transactional
    public CartSummaryResponseDto getCartWithTotal(User user) {
        List<CartItem> items = cartItemRepository.findAllByUser(user);
        List<CartItemResponseDto> dtoList = items.stream()
                .map(item -> CartItemResponseDto.builder()
                        .id(item.getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .customizationImageUrl(item.getCustomizationImageUrl())
                        
                        .build())
                .collect(Collectors.toList());
    
        int total = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();
    
        return new CartSummaryResponseDto(total, dtoList);
    }
    

}
