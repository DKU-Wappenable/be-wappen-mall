package com.wappenable.be.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserRequestDto {
    
    @NotBlank(message = "아이디 입력은 필수입니다.")
    private String email; // 아이디

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;
}
