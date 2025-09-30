package com.daangcool.stack.domain.enumeration;

public enum SocialType {

    LOCAL("local"),
    FACEBOOK("facebook"),
    GOOGLE("google"),
    KAKAO("kakao"),
    INSTAGRAM("instagram"),
    MICROSOFT("microsoft"),
    NAVER("naver");

    private final String ROLE_PREFIX = "ROLE_";

    private final String name;

    SocialType(String name) {
        this.name = name;
    }

    public String getRoleType() { return ROLE_PREFIX + name.toUpperCase(); }

    public String getValue() { return name; }

    public boolean isEquals(String authority) {
        return this.name.equals(authority);
    }
}
