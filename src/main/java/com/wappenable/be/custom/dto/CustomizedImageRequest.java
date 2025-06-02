package com.wappenable.be.custom.dto;

import lombok.Data;
import lombok.*;

@Data
public class CustomizedImageRequest {
    private Long originalProductId;
    private String title;
    private String customizedImageUrl;

}