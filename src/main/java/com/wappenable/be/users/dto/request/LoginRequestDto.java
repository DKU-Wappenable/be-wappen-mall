package com.wappenable.be.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // JSON 역직렬화를 위해 필요
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "아이디를 입력해주세요")
    @Pattern(
        regexp = "^[a-z0-9]{4,20}$",
        message = "아이디는 영문 소문자와 숫자만 포함한 4~20자여야 합니다."
    )
    /*
     * 회원가입에서 이미 @Pattern을 작성했는데 로그인에서 작성해야할 필요가 있을까?
     * 1. 보안 공격 차단 : 컨트롤러 레벨에서 걸러내는 것 자체가 방어선이 된다. 예: SQL Injection 유사 시도 등
     * 2. 불필요한 DB 호출 방지
     */
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
