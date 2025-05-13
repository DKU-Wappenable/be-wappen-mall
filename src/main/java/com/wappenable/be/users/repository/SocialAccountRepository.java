package com.wappenable.be.users.repository;

import com.wappenable.be.global.security.oauth2.domain.AuthProvider;
import com.wappenable.be.users.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
