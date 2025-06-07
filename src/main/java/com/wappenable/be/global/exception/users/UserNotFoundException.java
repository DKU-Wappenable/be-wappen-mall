package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;
// 로그인 시 회원가입 되어있지 않은 경우
public class UserNotFoundException extends CustomException {
    
    public static final String MESSAGE = "사용자를 찾을 수 없습니다.";

    public UserNotFoundException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}