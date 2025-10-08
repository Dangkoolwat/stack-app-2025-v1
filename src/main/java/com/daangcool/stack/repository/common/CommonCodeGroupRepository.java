package com.daangcool.stack.repository.common;

import com.daangcool.stack.domain.common.CommonCodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommonCodeGroupRepository extends JpaRepository<CommonCodeGroup, String> {
    // 비삭제 그룹 코드를 groupCode로 조회
    Optional<CommonCodeGroup> findOneByGroupCodeAndDeletedIsFalse(String groupCode);

    // 비삭제 그룹 코드를 displayOrder 순으로 모두 조회
    List<CommonCodeGroup> findAllByDeletedIsFalseOrderByDisplayOrderAsc();

    // GroupCode의 존재 여부를 확인 (삭제된 것도 포함하여 중복 체크)
    boolean existsByGroupCode(String groupCode);
}
