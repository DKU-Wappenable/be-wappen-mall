package com.wappenable.be.terms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wappenable.be.terms.domain.UserTermsAgreement;
import com.wappenable.be.users.domain.User;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
    Optional<UserTermsAgreement> findTopByUserOrderByAgreedAtDesc(User user);
    List<UserTermsAgreement> findByUser(User user);
    boolean existsByUserAndFirstIsTrueAndCheckedIsTrue(User user);
}
