package com.wappenable.be.global.exception.users;
import org.springframework.http.HttpStatus;
import com.wappenable.be.global.exception.CustomException;

public class UnauthorizedAccessException extends CustomException {

    public static final String MESSAGE = "권한이 없습니다.";

    public UnauthorizedAccessException() {
        super(MESSAGE, HttpStatus.FORBIDDEN);
    }
}