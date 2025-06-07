package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

public class SamePasswordException extends CustomException {
    
    public static final String MESSAGE = "기존 비밀번호와 동일한 비밀번호로는 변경할 수 없습니다.";

    public SamePasswordException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
