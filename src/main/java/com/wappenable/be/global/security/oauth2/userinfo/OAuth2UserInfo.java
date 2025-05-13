package com.wappenable.be.global.security.oauth2.userinfo;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderUserId();
    String getEmail();
    String getNickname();
    Map<String, Object> getAttributes();
}
