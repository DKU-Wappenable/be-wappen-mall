package com.wappenable.be.users.repository;

import com.wappenable.be.users.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wappenable.be.users.entity.AuthProvider;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
