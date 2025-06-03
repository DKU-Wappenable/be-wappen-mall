package com.wappenable.be.global.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            u -> {},
            () -> {
                User admin = User.builder()
                        .email(adminEmail)
                        .nickname("관리자")
                        .recoveryEmail("admin@email.com")
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
            }
        );
    }
}
