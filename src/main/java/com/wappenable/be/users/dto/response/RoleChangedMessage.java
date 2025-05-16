package com.wappenable.be.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoleChangedMessage {
    private Long userId;
    private String newRole;
    private String message;
}
