package com.daangcool.stack.service.softdelete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 소프트 삭제 필터를 무시하고(=삭제 데이터 포함) 조회가 필요할 때 사용합니다.
 *
 * 사용 예:
 * - 관리자 복구/하드 삭제 기능
 * - 휴지통(삭제 목록) 조회
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface IncludeDeleted {}

