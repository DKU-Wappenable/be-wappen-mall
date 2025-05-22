package com.wappenable.be.global.exception.admin;

import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

// ADMIN이 유효하지 않은 권한을 부여하려는 경우
public class RoleNotValidException extends CustomException {
    public static final String MESSAGE = "유효하지 않은 권한(Role) 값입니다.";

    public RoleNotValidException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}