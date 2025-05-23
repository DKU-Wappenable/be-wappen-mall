package com.wappenable.be.global.exception.admin;

import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

// ADMIN 권한이 아닌 계정이 ADMIN 권한을 사용하려는 경우
 
public class AdminPermissionRequiredException extends CustomException {
    public static final String MESSAGE = "해당 요청은 관리자 권한이 필요합니다.";

    public AdminPermissionRequiredException() {
        super(MESSAGE, HttpStatus.FORBIDDEN);
    }
}