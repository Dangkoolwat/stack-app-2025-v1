package com.daangcool.stack.repository.common;

import com.daangcool.stack.domain.common.CommonCodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, Long> {

    // 그룹 코드를 기준으로 비삭제 상세 코드를 sortOrder 순으로 조회
    List<CommonCodeDetail> findAllByGroupGroupCodeAndDeletedIsFalseOrderBySortOrderAsc(String groupCode);

    // 그룹 코드와 상세 코드(Code)로 단일 비삭제 상세 정보를 조회
    Optional<CommonCodeDetail> findOneByGroupGroupCodeAndCodeAndDeletedIsFalse(String groupCode, String code);

    // 그룹 내 코드 중복 체크 (삭제된 것도 포함)
    boolean existsByGroupGroupCodeAndCode(String groupCode, String code);

    // 그룹 내 코드 존재 여부 확인 (삭제 여부 상관없이 단일 조회)
    Optional<CommonCodeDetail> findOneByGroupGroupCodeAndCode(String groupCode, String code);
}
