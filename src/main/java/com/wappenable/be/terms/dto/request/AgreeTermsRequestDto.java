package com.wappenable.be.terms.dto.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AgreeTermsRequestDto {
    // private List<AgreeItem> agreements;

    // @Getter
    // @NoArgsConstructor
    // @AllArgsConstructor
    // public static class AgreeItem {
    //     private Long termsId;
    //     private boolean agreed; // → 향후 일부 동의/비동의 구조에도 대응 가능
    // }

    private boolean terms;
    private boolean privacy;
    private boolean financial;
    private boolean marketing;
}