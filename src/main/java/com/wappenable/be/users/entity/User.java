package com.wappenable.be.users.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
/*
 * 복수형 테이블이 당연시 : 작성 안하면 test시 h2에서 user 테이블 생성할 때 충돌 일어남. 표준을 users로 보기 때문
 */
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "password_hash", nullable = true) // 소셜 계정을 위해 null이어도 허가
    private String passwordHash;

    @Enumerated(EnumType.STRING) // DB 저장 시 String으로 저장
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // user 하나에 여러개의 socialAccounts 존재 가능, user 삭제 시 모든 연동 계정 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) 
    private List<SocialAccount> socialAccounts = new ArrayList<>();
}