package com.wappenable.be.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // JSON 역직렬화를 위해 필요
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}
