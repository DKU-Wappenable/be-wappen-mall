package com.wappenable.be.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// API 서버 상태 확인용 컨트롤러
@RestController
public class ApiController {

    // 루트 경로("/") 요청 시 "API Server" 문자열을 반환
    @GetMapping("/")
    public String home() {
        return "updated API Server plz really?";
    }

    @GetMapping("/test")
    public String test(){
        return "API TEST No~~~ PLZ";
    }
    
} 