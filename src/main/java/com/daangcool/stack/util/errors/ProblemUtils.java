package com.daangcool.stack.util.errors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Locale;

/**
 * RFC 7807 기반 ProblemDetail 생성 유틸리티.
 * 다국어(i18n) 메시지 및 시스템 시간대 반영.
 */
public final class ProblemUtils {

    private static MessageSource messageSource;

    private ProblemUtils() {}

    /** 외부 설정(TimeMessageSourceConfiguration)에서 MessageSource 주입 */
    public static void setMessageSource(MessageSource source) {
        ProblemUtils.messageSource = source;
    }

    /** ProblemDetail 표준 생성 */
    public static ProblemDetail build(HttpStatus status,
                                      String type,
                                      String title,
                                      String detail,
                                      HttpServletRequest req) {

        Locale locale = LocaleContextHolder.getLocale();
        ZoneId zoneId = ZoneId.systemDefault(); // 시스템 시간대 자동 반영
        String path = req != null ? req.getRequestURI() : "unknown";

        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(localize(title, locale));
        problem.setDetail(localize(detail, locale));
        problem.setInstance(URI.create(path));

        problem.setProperty("status", status.value());
        problem.setProperty("path", path);
        problem.setProperty("timestamp", OffsetDateTime.now(zoneId).toString());
        problem.setProperty("locale", locale.toLanguageTag());

        return problem;
    }

    /** i18n 메시지 변환 */
    private static String localize(String key, Locale locale) {
        if (messageSource == null || key == null) return key;
        try {
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key; // 키가 없으면 원문 반환
        }
    }
}
