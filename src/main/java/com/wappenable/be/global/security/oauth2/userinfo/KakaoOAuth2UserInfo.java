package com.wappenable.be.global.security.oauth2.userinfo;

import java.util.Map;


public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderUserId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        return "kakao_" + getProviderUserId() + "@noemail.local";
    }

    @Override
    public String getNickname() {
        Map<String, Object> props = (Map<String, Object>) attributes.get("properties");
        return props != null ? (String) props.get("nickname") : "카카오유저";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    
    // kakao developers에 프로필 사진 선택 사항으로 두었음.
    // 근데 이거 어떻게 저장하게? User 엔티티는 ERD 보고 작성한건데 이미지는 작성 안되어있었음.
    // public String getProfileImageUrl() {
    //     Map<String, Object> props = (Map<String, Object>) attributes.get("properties");
    //     return props != null ? (String) props.get("profile_image") : null;
    // }

}
