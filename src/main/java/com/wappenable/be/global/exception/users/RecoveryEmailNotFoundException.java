package com.wappenable.be.global.exception.users;

import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

// 복구용 이메일로 사용자를 찾을 수 없을 때
public class RecoveryEmailNotFoundException extends CustomException {

    public static final String MESSAGE = "해당 복구 이메일로 등록된 사용자가 없습니다.";

    public RecoveryEmailNotFoundException() {
        super(MESSAGE, HttpStatus.BAD_REQUEST); // 400 Bad Request
    }
}
