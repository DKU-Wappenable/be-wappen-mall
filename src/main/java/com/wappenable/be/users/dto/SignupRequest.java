package com.wappenable.be.users.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String nickname;
    private String password;
    private String role; // enum이지만 처음엔 문자열로 받아도 됨
}

