package com.wappenable.be.users.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderUserId();
    String getEmail();
    String getNickname();
    Map<String, Object> getAttributes();
}
