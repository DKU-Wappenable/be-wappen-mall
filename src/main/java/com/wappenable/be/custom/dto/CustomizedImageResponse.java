package com.wappenable.be.custom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomizedImageResponse {
    private Long id;
    private String customizedImageUrl;
    private String title;
    private Long userId;
    private LocalDateTime createdAt;
}
