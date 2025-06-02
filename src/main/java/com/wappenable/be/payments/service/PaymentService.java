package com.wappenable.be.payments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RestTemplate restTemplate;

    @Value("${iamport.key}")
    private String impkey;

    @Value("${iamport.secret}")
    private String impsecret;
    
    private static final String TOKEN_URL =  "https://api.iamport.kr/users/getToken";

    public void processPayment(String method, BigDecimal amount) {
        switch (method.toUpperCase()) {
            case "BANK" -> log.info("[무통장 입금] 결제 완료: {}", amount);
            case "CARD", "KAKAOPAY" -> requestIamportToken(amount);
            default -> throw new IllegalArgumentException("결제수단이 올바르지 않습니다.");
        }
    }

    private void requestIamportToken(BigDecimal amount) {
      
        // 1. JSON 바디 생성
        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("imp_key", impkey);
        tokenRequest.put("imp_secret", impsecret);
        try {

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(tokenRequest);

            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // 3. HttpEntity 생성
            HttpEntity<String> entity = new HttpEntity<>(json, headers);

            // 4. POST 요청
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(TOKEN_URL, entity, Map.class);

            // 5. 응답 처리
            if (tokenResponse.getStatusCode() == HttpStatus.OK && tokenResponse.getBody() != null) {
                Map<String, Object> response = (Map<String, Object>) tokenResponse.getBody().get("response");
                if (response != null) {
                    String accessToken = (String) response.get("access_token");
                    log.info("[아임포트] Access Token 발급 성공: {}", accessToken);
                } else {
                    throw new RuntimeException("응답에 access_token 없음");
                }
            }
        } catch (HttpClientErrorException e) {
            log.error("[아임포트 오류] {}", e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("[일반 오류] {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
