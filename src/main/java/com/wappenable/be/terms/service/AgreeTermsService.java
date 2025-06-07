package com.wappenable.be.terms.service;

import com.wappenable.be.terms.domain.UserTermsAgreement;
import com.wappenable.be.terms.dto.request.AgreeTermsRequestDto;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import com.wappenable.be.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AgreeTermsService {

    private final UserTermsAgreementRepository userTermsAgreementRepository;

    public void saveAgreement(User user, AgreeTermsRequestDto request) {

        // 기존 체크된 약관 invalidate
        userTermsAgreementRepository.findTopByUserOrderByAgreedAtDesc(user)
            .ifPresent(prev -> {
                prev.setChecked(false);
                userTermsAgreementRepository.save(prev);
            });

        boolean requiredAgreed = request.isTerms() && request.isPrivacy() && request.isFinancial();

        UserTermsAgreement agreement = UserTermsAgreement.builder()
                .user(user)
                .termsChecked(request.isTerms())
                .privacyChecked(request.isPrivacy())
                .financialChecked(request.isFinancial())
                .marketingChecked(request.isMarketing())
                .checked(true)
                .first(requiredAgreed)
                .agreedAt(LocalDateTime.now())
                .build();

        userTermsAgreementRepository.save(agreement);
    }
}
