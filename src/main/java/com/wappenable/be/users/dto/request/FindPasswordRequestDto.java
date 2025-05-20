package com.wappenable.be.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequestDto {
    // [ ] : 필드 수정 필요
    @NotBlank
    private String loginId;

    @NotBlank
    private String phoneNumber;
}
