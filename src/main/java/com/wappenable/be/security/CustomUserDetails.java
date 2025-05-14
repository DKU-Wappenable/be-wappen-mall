package com.wappenable.be.security;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Arrays;
import java.util.Collection;

public class CustomUserDetails extends User{

    private Long id;
    private String role;
    
    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id, String role){
        super(username, password, authorities);
        this.id =id;
        this.role = role;
    }

    public boolean hasAnyRole(String... allowedRoles) {
        return Arrays.stream(allowedRoles).anyMatch(r -> r.equals(this.role));
    }

    public Long getId() {
        return id;
    }

}
