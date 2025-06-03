package com.wappenable.be.global.security.auth;

import com.wappenable.be.global.exception.users.LoginUserNotFoundException;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(LoginUserNotFoundException::new);
        return new CustomUserDetails(user);
    }
}
