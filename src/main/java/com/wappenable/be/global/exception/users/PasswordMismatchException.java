package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;

public class PasswordMismatchException extends CustomException {
    
    public static final String MESSAGE = "비밀번호와 비밀번호 확인이 일치하지 않습니다";
    
    public PasswordMismatchException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}