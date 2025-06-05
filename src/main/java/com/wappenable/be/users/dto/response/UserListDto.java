package com.wappenable.be.users.dto.response;

import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Long id;
    private String email;
    private String recoveryEmail;
    private String nickname;
    private Role role;

    public static UserListDto from(User user) {
        return new UserListDto(
            user.getId(),
            user.getEmail(),
            user.getRecoveryEmail(),
            user.getNickname(),
            user.getRole()
        );
    }
}
