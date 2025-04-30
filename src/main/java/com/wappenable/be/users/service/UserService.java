package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wappenable.be.users.dto.SignupRequest;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.exception.CustomException;
import com.wappenable.be.users.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("중복된 이메일입니다", HttpStatus.CONFLICT);
        }

        /*  
        Role : user, designer, shoop_owner, admin 이외 작성하면 Role.valueOf()에서
        IllegalArgumentException 터짐
        이건 나중에 ExceptionHandler에서 잡으면 된다.
        */
        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }
}
