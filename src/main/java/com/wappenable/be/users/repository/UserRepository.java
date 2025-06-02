package com.wappenable.be.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wappenable.be.users.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email); // 실제 로그인 아이디
    Optional<User> findByRecoveryEmail(String recoveryEmail); // 아이디/비밀번호 찾기에 사용할 인증용 이메일
    Optional<User> findByEmailAndRecoveryEmail(String email, String recoveryEmail);


}
