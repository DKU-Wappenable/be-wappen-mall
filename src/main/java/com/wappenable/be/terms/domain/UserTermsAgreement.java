package com.wappenable.be.terms.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.wappenable.be.users.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_terms_agreement")
public class UserTermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private boolean termsChecked;
    private boolean privacyChecked;
    private boolean financialChecked;
    private boolean marketingChecked;

    private boolean checked; // 필수 약관 모두 동의 여부

    private boolean first;   // 최초 동의 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime agreedAt;
} 


// @Entity @Getter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @EntityListeners(AuditingEntityListener.class)
// @Table(name = "user_terms_agreement")
// public class UserTermsAgreement {

//     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne(fetch = FetchType.LAZY)
//     private User user;

//     @ManyToOne(fetch = FetchType.LAZY)
//     private Terms terms;

//     @CreatedDate
//     @Column(updatable = false)
//     private LocalDateTime agreedAt;
// }