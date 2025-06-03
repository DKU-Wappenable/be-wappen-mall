package com.wappenable.be.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse {
    private String message;
    private String socialLogoutUrl; // 소셜 사용자일 경우에만 전달
}