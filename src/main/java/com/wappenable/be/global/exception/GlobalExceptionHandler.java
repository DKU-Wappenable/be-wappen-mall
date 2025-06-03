package com.wappenable.be.global.exception; // 패키지 따로 빼줘야 해.

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // ✅ 유효성 검증 오류 처리 (RequestBody @Valid 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> error = new HashMap<>();
        
        // 첫 번째 필드 오류의 메시지를 사용 (보안상 구체적인 필드명은 노출하지 않음)
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
        String errorMessage = fieldError.getDefaultMessage();
        
        // 로그인/회원가입 구분 없이 일반적인 메시지로 변환
        if (errorMessage.contains("아이디")) {
            error.put("error", "입력 형식을 확인해주세요.");
        } else if (errorMessage.contains("비밀번호")) {
            error.put("error", "입력 형식을 확인해주세요.");
        } else {
            error.put("error", "입력 형식을 확인해주세요.");
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ✅ 폼 데이터 유효성 검증 오류 처리
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "입력 형식을 확인해주세요.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ✅ 일반적인 예외 처리 (마지막 fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
