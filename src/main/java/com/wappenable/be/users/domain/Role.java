package com.wappenable.be.users.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    USER, SHOP_OWNER, ADMIN;

    /*
     * "Spring Security에서 사용자 권한을 표현할 때, 'ROLE_USER' 같은 형식이 필요하다면 필요"
     * Spring Security에서는 GrantedAuthority로 권한을 다루기 때문에, 문자열로 변환하는 작업이 자주 필요함
     * 이 메서드는 자주 쓰이는 헬퍼 유틸이라 enum 안에 넣으면 깔끔함
     */
    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }

    @JsonCreator
    public static Role from(String value) {
        return Role.valueOf(value.toUpperCase()); // "admin" → "ADMIN"
    }
}
