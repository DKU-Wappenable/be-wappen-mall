package com.wappenable.be.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String recoveryEmail;
}
