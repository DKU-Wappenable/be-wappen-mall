package com.wappenable.be.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IamportTokenRequest {
    @JsonProperty("imp_key")
    private String impKey;

    @JsonProperty("imp_secret")
    private String impSecret;
}
