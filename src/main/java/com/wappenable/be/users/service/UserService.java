package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wappenable.be.users.dto.LoginRequest;
import com.wappenable.be.users.dto.TokenResponse;
import com.wappenable.be.users.dto.SignupRequest;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.exception.CustomException;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.util.JwtUtil;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("이메일이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new TokenResponse(accessToken, refreshToken);
    }
}
