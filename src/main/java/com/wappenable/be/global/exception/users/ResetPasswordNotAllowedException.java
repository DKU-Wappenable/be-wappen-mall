package com.wappenable.be.global.exception.users;

import com.wappenable.be.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ResetPasswordNotAllowedException extends CustomException {
    public ResetPasswordNotAllowedException() {
        super("권한이 없습니다. 비밀번호를 재설정은 본인 계정만 가능합니다.", HttpStatus.FORBIDDEN);
    }
}