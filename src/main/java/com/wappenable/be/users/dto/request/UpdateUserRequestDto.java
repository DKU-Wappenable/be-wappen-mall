package com.wappenable.be.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDto {
    @NotBlank(message = "닉네임 입력은 필수입니다.")
    @Pattern(
        regexp = "^(?!.*\\s)[A-Za-z0-9가-힣]{2,10}$",
        message = "닉네임은 영어, 숫자 또는 한글로 이루어진 2~10자여야 합니다."
    )
    private String nickname;
}