package com.wappenable.be.users.entity;

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

    private String provider; // 예: google, kakao, naver

    private String providerId; // 소셜 고유 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
