package com.wappenable.be.terms.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wappenable.be.global.exception.users.RequiredTermsNotAgreedException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
// import com.wappenable.be.terms.domain.Terms;
import com.wappenable.be.terms.domain.UserTermsAgreement;
import com.wappenable.be.terms.dto.request.AgreeTermsRequestDto;
// import com.wappenable.be.terms.repository.TermsRepository;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgreeTermsService {

    private final UserRepository userRepository;
    // private final TermsRepository termsRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;


    @Transactional
    public void saveAgreement(AgreeTermsRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);

        boolean requiredAgreed = request.isTerms() && request.isPrivacy() && request.isFinancial();

        UserTermsAgreement agreement = UserTermsAgreement.builder()
            .user(user)
            .termsChecked(request.isTerms())
            .privacyChecked(request.isPrivacy())
            .financialChecked(request.isFinancial())
            .marketingChecked(request.isMarketing())
            .checked(requiredAgreed)
            .first(true) // 최초 동의로 간주
            .build();

        userTermsAgreementRepository.save(agreement);
    }


    // @Transactional
    // public void agree(AgreeTermsRequestDto request) {
    //     String email = SecurityContextHolder.getContext().getAuthentication().getName();

    //     User user = userRepository.findByEmail(email)
    //             .orElseThrow(UserNotFoundException::new);

    //      // 1. 동의한 ID만 추출
    //     List<Long> agreedIds = request.getAgreements().stream()
    //     .filter(AgreeTermsRequestDto.AgreeItem::isAgreed)
    //     .map(AgreeTermsRequestDto.AgreeItem::getTermsId)
    //     .toList();

    //     // 2. 동의한 Terms 조회
    //     List<Terms> termsList = termsRepository.findAllById(agreedIds);

    //     // 3. 필수 약관 모두 동의했는지 확인
    //     long agreedRequiredCount = termsList.stream()
    //         .filter(Terms::isRequired)
    //         .count();
    //     long totalRequiredCount = termsRepository.countByRequired(true);
    //     if (agreedRequiredCount != totalRequiredCount) {
    //         throw new RequiredTermsNotAgreedException();
    //     }

    //     // 4. DB 저장
    //     List<UserTermsAgreement> agreements = termsList.stream()
    //         .map(terms -> UserTermsAgreement.builder()
    //             .user(user)
    //             .terms(terms)
    //             .build())
    //         .toList();

    //     userTermsAgreementRepository.saveAll(agreements);
    // }
}