package com.wappenable.be.global.security.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.wappenable.be.users.domain.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(user.getRole().toGrantedAuthority());
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash(); // 주의: 소셜 로그인 사용자는 null일 수 있음
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // email을 식별자로 사용
    }

    public Long getId() {
        return user.getId();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true; // 필요 시 커스터마이징 가능
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}