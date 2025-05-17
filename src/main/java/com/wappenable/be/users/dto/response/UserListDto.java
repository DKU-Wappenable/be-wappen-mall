package com.wappenable.be.users.dto.response;

import com.wappenable.be.users.entity.Role;

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
    private Role role;
}
