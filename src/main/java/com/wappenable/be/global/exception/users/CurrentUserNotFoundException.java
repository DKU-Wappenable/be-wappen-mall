package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;
// 회원 탈퇴용
public class CurrentUserNotFoundException extends CustomException {
    
    public static final String MESSAGE = "사용자를 찾을 수 없습니다.";

    public CurrentUserNotFoundException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}