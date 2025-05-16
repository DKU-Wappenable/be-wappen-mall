package com.wappenable.be.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Long id;
    private String email;
    private String nickname;
    private String role;
}
