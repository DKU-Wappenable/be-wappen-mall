package com.wappenable.be.global.security.oauth2.service;

import com.wappenable.be.global.security.oauth2.domain.AuthProvider;
import com.wappenable.be.global.security.oauth2.userinfo.OAuth2UserInfo;
import com.wappenable.be.global.security.oauth2.userinfo.OAuth2UserInfoFactory;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.SocialAccount;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.SocialAccountRepository;
import com.wappenable.be.users.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * 새로운 유저를 save() 했는데도 SuccessHandler에서 못 찾는다면
 * 트랜잭션이 커밋되기 전에 SuccessHandler가 실행되어 조회가 안될 수도 있다.
 */
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User;
        try {
            oAuth2User = super.loadUser(request);
        } catch (OAuth2AuthenticationException e) {
            log.error("OAuth2 인증 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }

        String provider = request.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        String providerUserId = userInfo.getProviderUserId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        
        // 카카오에서 이메일 받아오지 않기 때문에 임시 이메일 생성, 구글, 네이버는 이메일 받아옴
        // 나중에 이메일 받아오지 않는 provider 늘어나면 계속하여 코드 변경이 필요해진다.
        if (email == null || email.isBlank()) {
            email = "kakao_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
            + "_" + UUID.randomUUID().toString().substring(0, 8)
            + "@placeholder.com";
        }
        final String finalEmail = email; // 람다에서 쓸 수 있게 final 변수 선언

        log.info("[OAuth2] provider={}, providerUserId={}, email={}, nickname={}", provider, providerUserId, email, nickname);

        // 사용자 조회 or 생성 , 기본 Role = USER
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(finalEmail)
                    .recoveryEmail(finalEmail)
                    .nickname(nickname)
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(newUser);
        });

        // 소셜 계정 연동
        Optional<SocialAccount> existing = socialAccountRepository.findByProviderAndProviderUserId(AuthProvider.valueOf(provider.toUpperCase()), providerUserId);
        if (existing.isEmpty()) {
            SocialAccount socialAccount = SocialAccount.builder()
                    .provider(AuthProvider.valueOf(provider.toUpperCase()))
                    .providerUserId(providerUserId)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            socialAccountRepository.save(socialAccount);
        }

        return new DefaultOAuth2User(
                Collections.singleton(user.getRole().toGrantedAuthority()),
                userInfo.getAttributes(),
                "email"
                // userNameAttributeName
        );

        // TODO: 소셜 로그인 연결 해제, 재가입 흐름
    }
} 
