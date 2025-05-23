package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;

public class InvalidPasswordException extends CustomException {
    
    public static final String MESSAGE = "비밀번호가 일치하지 않습니다.";
    
    public InvalidPasswordException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }
}