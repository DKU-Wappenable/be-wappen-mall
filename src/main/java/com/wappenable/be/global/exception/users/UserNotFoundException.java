package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;
// 로그인 시 회원가입 되어있지 않은 경우
public class UserNotFoundException extends CustomException {
    
    public static final String MESSAGE = "아이디가 존재하지 않습니다.";

    public UserNotFoundException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}