package com.daangcool.stack.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";
    public static final String TEXT_ONLY = "[가-힣a-zA-Z0-9\\s]+";
    public static final String TEXT_WITH_SYMBOL = "^[가-힣a-zA-Z0-9\\s!@#\\$%\\^\\&*\\?':)\\(+=,._-]+$";

    // : 숫자, 특문 각 1회 이상, 영문은 2개 이상 사용하여 8자리 이상 입력
    public static final String PASSWORD_PATTERN ="(?=.*\\d{1,50})(?=.*[~`!@#$%\\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{8,50}$";


    public static final String DIGIT = "[0-9]+";
    public static final String CHAR_PATTERN = "[a-zA-Z\\s]+";
    public static final String ID_PATTERN = "[a-zA-Z0-9]+";
    public static final String CODE_PATTERN = "[0-9]{6}";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String SYSTEM = "system";
    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String DEFAULT_LANGUAGE = "ko";


    public static final Integer MAX_CAPTCHA_TRIES = 0;
    public static final Integer MAX_ATTEMPT = 5;

    public static final String LATITUDE_PATTERN="^[+-]?((90\\.?0*$)|(([0-8]?[0-9])\\.?[0-9]*$))";

    public static final String LONGITUDE_PATTERN="^[+-]?((180\\.?0*$)|(((1[0-7][0-9])|([0-9]{0,2}))\\.?[0-9]*$))";

    // 33 - 43
    public static final String KOR_LATITUDE_PATTERN="(^(3)[3-9]|^(4)[0-3])\\.[0-9]*$";

    // 124 - 132
    public static final String KOR_LONGITUDE_PATTERN="(^(12)[4-9]|^(13)[0-2])\\.?[0-9]*$";

    public static final String NUMBER_ONLY = "^[0-9]*$";

    // 숫자사이 . - 입력가능
    public static final String KOR_PHONE_NUMBER_PATTERN="^01(?:0|1|[6-9])[.-]?(\\\\d{3}|\\\\d{4})[.-]?(\\\\d{4})$";
    public static final String KOR_GENERAL_NUMBER_PATTERN="^(0(2|3[1-3]|4[1-4]|5[1-5]|6[1-4]))[.-]?(\\d{3,4})[.-]?(\\d{4})$";

    public static final String KOR_ENG_ONLY = "[가-힣a-zA-Z\\s]+";

    public static final String KOR_NUMBER_PATTERN="^(01(?:0|1|[6-9]){1}|02|0[3-9]{1}[0-9]{1})[.-]?[0-9]{3,4}[.-]?[0-9]{4}$";

    public static final String YOUTUBE_PATTERN_ID= "^(?:(?:\\w*.?://)?\\w*.?\\w*-?.?\\w*/(?:embed|e|v|watch|.*/)?\\??(?:feature=\\w*\\.?\\w*)?&?(?:v=)?/?)([\\w\\d_-]+).*";

    public static final String IP_ADDRESS="(?!(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.).*)(?!255\\.255\\.255\\.255)(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)){3}";

    public static final String IMAGE_ONLY="\\\\.(?:jpg|gif|png|jpeg|JPG|GIF|PNG|JPEG)";

    private Constants() {
    }
}
