package com.wappenable.be.users.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor // 테스트나 내부에서 생성자 주입할 때 사용
public class RoleUpdateRequestDto {
    private String role;
}
