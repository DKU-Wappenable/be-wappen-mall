package com.wappenable.be.users.domain;


import java.time.LocalDateTime;

import com.wappenable.be.global.security.oauth2.domain.AuthProvider;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // 예: google, kakao, naver

    private String providerUserId; // 소셜 고유 ID ex. "103847239847238472394"
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
