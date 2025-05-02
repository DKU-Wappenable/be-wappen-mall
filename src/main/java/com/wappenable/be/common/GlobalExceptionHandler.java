package com.wappenable.be.common; // ⭐ 패키지 따로 빼줘야 해.

import com.wappenable.be.users.exception.CustomException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 프로젝트 전체에서 발생하는 예외들을 전역적으로 처리 해주는 클래스
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(error);
        // return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    
}
