package com.wappenable.be.users.service;


import com.wappenable.be.global.exception.admin.AdminUserNotFoundException;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    private final UserRepository userRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(AdminUserNotFoundException::new);

        user.setRole(newRole);
    }

    // [ ] : 이건 전체 사용자 조회이지 50명 초기 목록 렌더링이 아님
    // @Transactional(readOnly = true)
    // public List<UserListDto> getAllUsers() {
    //     return userRepository.findAll().stream()
    //             .map(user -> new UserListDto(
    //                     user.getId(),
    //                     user.getEmail(),
    //                     user.getRecoveryEmail(),
    //                     user.getNickname(),
    //                     user.getRole()
    //             ))
    //             .collect(Collectors.toList());
    // }

    // NOTE : 50명 렌더링
    public Page<UserListDto> getUsersPage(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(user -> {
                boolean agreed = userTermsAgreementRepository.existsByUserAndFirstIsTrueAndCheckedIsTrue(user);
                return UserListDto.from(user, agreed);
            });
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(AdminUserNotFoundException::new);
        // 소셜 계정 정보 로깅 (삭제 전)
        if (!user.getSocialAccounts().isEmpty()) {
            log.info("관리자 삭제: 사용자 {} (이메일: {}) - {}개 소셜계정도 함께 삭제", 
                    userId, user.getEmail(), user.getSocialAccounts().size());
            
            // 어떤 소셜 계정들이 삭제되는지 로깅
            user.getSocialAccounts().forEach(socialAccount -> 
                log.info("  - {} 계정 삭제: {}", socialAccount.getProvider(), socialAccount.getProviderUserId())
            );
        } else {
            log.info("관리자 삭제: 일반 사용자 {} (이메일: {})", userId, user.getEmail());
        }
        
        // 약관 동의 기록이 있다면 로깅
        if (user.getTermsAgreements() != null && !user.getTermsAgreements().isEmpty()) {
            log.info("사용자 {}의 약관 동의 기록 {}개도 함께 삭제", userId, user.getTermsAgreements().size());
        }

        // User 삭제 시 cascade로 SocialAccount들도 자동 삭제
        userRepository.delete(user);
        
        log.info("사용자 삭제 완료: {}", userId);
    }
}
