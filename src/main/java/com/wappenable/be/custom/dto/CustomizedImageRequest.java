package com.wappenable.be.custom.dto;

import lombok.Data;
import lombok.*;

@Data
public class CustomizedImageRequest {
    private String customizedImageUrl;
    private String title;
    private Long userId;

}
