// package com.wappenable.be.terms.domain;

// import java.time.LocalDateTime;

// import org.springframework.data.annotation.CreatedDate;
// import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// import com.wappenable.be.global.type.TermsType;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.EntityListeners;
// import jakarta.persistence.EnumType;
// import jakarta.persistence.Enumerated;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;

// // NOTE : 실제 약관이 저장되는 엔티티
// @Entity @Getter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @EntityListeners(AuditingEntityListener.class)
// @Table(name = "terms")
// public class Terms {

//     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Enumerated(EnumType.STRING)
//     private TermsType type;

//     private String version;

//     @Column(columnDefinition = "TEXT")
//     private String content;

//     private boolean required; // true : 필수 약관, false : 선택 약관

//     @CreatedDate
//     @Column(updatable = false)
//     private LocalDateTime createdAt;

//     @LastModifiedDate
//     private LocalDateTime updatedAt;
// }
