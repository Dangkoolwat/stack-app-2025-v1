package com.daangcool.stack.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 파일 업로드 전역 기본 설정 VO (Value Object)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDefaults implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 기본 최대 파일 크기 (Bytes) - 정책 미지정 시 사용 */
    private long defaultMaxFileSizeBytes;

    /** 기본 최대 요청 크기 (Bytes) - Multipart 전체 크기 */
    private long defaultMaxRequestSizeBytes;

    /** 정책에 매칭되지 않는 파일 처리 (true: 차단, false: 기본값 적용 허용) */
    private boolean blockUnmatched;

    /** UI 노출용 안내 문구 */
    private String welcomeMessage;
}
