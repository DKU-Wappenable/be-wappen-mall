package com.wappenable.be.users.dto.response;

import com.wappenable.be.users.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangedMessageDto {
    private Long userId;
    private Role newRole;
    private String message;
}
