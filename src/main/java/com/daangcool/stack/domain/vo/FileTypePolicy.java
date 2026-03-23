package com.daangcool.stack.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 파일 타입별 업로드 정책 VO (Value Object)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileTypePolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 정책 식별 키 (예: jpeg-image, pdf-doc) */
    private String key;

    /** UI 표시명 (예: JPEG 이미지) */
    private String label;

    /** 활성화 여부 */
    private Boolean enabled;

    /** 허용 확장자 리스트 (예: ["jpg", "jpeg"]) */
    private List<String> allowedExtensions;

    /** 허용 MIME 타입 리스트 (예: ["image/jpeg"]) */
    private List<String> allowedMimeTypes;

    /** 최대 허용 파일 크기 (Bytes) */
    private Long maxFileSizeBytes;

    /** UI 노출 순서 */
    private Integer displayOrder;

    /** 정책 설명 */
    private String description;

    /** 향후 확장을 위한 메타데이터 (예: 이미지 압축률, 썸네일 생성 여부 등) */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
