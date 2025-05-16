package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.dto.request.LoginRequest;
import com.wappenable.be.users.dto.request.SignupRequest;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
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
                // .createdAt(LocalDateTime.now()) 생략 가능 : @CreatedAt
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("이메일이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("사용자 없음",HttpStatus.BAD_REQUEST));

        try {
            Role newRole = Role.valueOf(roleName.toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new CustomException("잘못된 역할 값입니다", HttpStatus.BAD_REQUEST);
        }
    }

    // 전체 사용자 조회
    @Transactional(readOnly = true)
    public List<UserListDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserListDto(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
        }


}
