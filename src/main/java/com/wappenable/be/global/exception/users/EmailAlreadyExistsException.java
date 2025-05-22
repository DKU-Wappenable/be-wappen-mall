package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;

public class EmailAlreadyExistsException extends CustomException {
    
    public static final String MESSAGE = "이미 존재하는 아이디입니다.";
    
    public EmailAlreadyExistsException() {
        super(MESSAGE, HttpStatus.CONFLICT);
    }
}