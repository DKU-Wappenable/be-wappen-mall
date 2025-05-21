package com.wappenable.be.custom.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name ="customized_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomizedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customizedImageUrl;
    private String title; // 사용자가 붙인 이름
    private Long userId; // 사용자 Id
}
