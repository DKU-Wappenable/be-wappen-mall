package com.wappenable.be.users.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDto {
    private String nickname;
    private String recoveryEmail;
    // 필요한 필드 추가
}