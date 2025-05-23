package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

/**
 * 아이디와 복구 이메일이 일치하는 사용자를 찾을 수 없는 경우
 */
public class UserRecoveryMismatchException extends CustomException {
    public static final String MESSAGE = "아이디와 복구용 이메일이 일치하는 사용자를 찾을 수 없습니다.";

    public UserRecoveryMismatchException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
