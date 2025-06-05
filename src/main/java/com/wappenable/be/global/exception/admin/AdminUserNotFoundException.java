package com.wappenable.be.global.exception.admin;

import org.springframework.http.HttpStatus;

import com.wappenable.be.global.exception.CustomException;
// ADMIN에서 회원 조회를 하려는데 없는 회원인 경우
public class AdminUserNotFoundException extends CustomException {
    public static final String MESSAGE = "사용자를 찾을 수 없습니다.";

    public AdminUserNotFoundException() {
        super(MESSAGE, HttpStatus.NOT_FOUND);
    }
}
