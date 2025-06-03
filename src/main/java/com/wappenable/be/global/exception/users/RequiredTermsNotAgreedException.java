package com.wappenable.be.global.exception.users;

import com.wappenable.be.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class RequiredTermsNotAgreedException extends CustomException {

    public static final String MESSAGE = "필수 약관에 모두 동의해야 합니다.";

    public RequiredTermsNotAgreedException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}