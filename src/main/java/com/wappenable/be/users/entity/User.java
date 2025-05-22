package com.wappenable.be.users.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    // TODO : 프론트 로그인 아이디, 계정 찾기 용 이메일, 비번, 비번 재확인
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 로그인용 id
    @Column(unique = true)
    private String email;

    // 아이디/비밀번호 찾기를 위한 외부 이메일 주소
    @Column(name = "recovery_email", nullable = false)
    private String recoveryEmail;

    @Column(nullable = true)
    private String nickname; // [ ] : 사용자 정의 닉네임, 소셜 로그인은 null? 프론트에 입력란 없기 때문에 일단 null 허용

    @Column(name = "password_hash", nullable = true) // 소셜 계정을 위해 null이어도 허가
    private String passwordHash;

    @Enumerated(EnumType.STRING) // DB 저장 시 String으로 저장
    @Column(nullable = false)
    private Role role;

    // Audit
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;


    // user 하나에 여러개의 socialAccounts 존재 가능, user 삭제 시 모든 연동 계정 삭제
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) 
    private List<SocialAccount> socialAccounts = new ArrayList<>();

}