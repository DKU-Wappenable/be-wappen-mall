package com.wappenable.be.product.service;

import com.wappenable.be.product.domain.Like;
import com.wappenable.be.product.domain.Product;
import com.wappenable.be.product.repository.LikeRepository;
import com.wappenable.be.product.repository.ProductRepository;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.product.dto.LikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void like(Long userId, Long productId) {
        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        if (likeRepository.findByUserAndProduct(user, product).isEmpty()) {
            likeRepository.save(Like.builder()
                    .user(user)
                    .product(product)
                    .build());
        }
    }

    public void unlike(Long userId, Long productId) {
        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        likeRepository.deleteByUserAndProduct(user, product);
    }

    public List<LikeResponseDto> getUserLikedProducts(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        List<Like> likes = likeRepository.findByUser(user);

        return likes.stream()
        .map(like -> new LikeResponseDto(like)) 
        .toList();
    }

    public int getLikeCount(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        return likeRepository.countByProduct(product);
    }
}
