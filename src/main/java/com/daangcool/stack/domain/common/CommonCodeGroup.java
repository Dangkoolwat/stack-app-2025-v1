package com.daangcool.stack.domain.common;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * 공통 그룹 코드 (Master Category)
 * 예: BOARD_TYPE, PROD_CATEGORY, ORDER_STATUS
 */
@Entity
@Table(name = "stack_common_group")
@Getter
@Setter
public class CommonCodeGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(max = 50)
    @Id // 그룹 코드(예: BOARD_TYPE)를 Primary Key로 사용
    @Column(name = "group_code", length = 50, nullable = false)
    private String groupCode;

    @NotNull
    @Size(max = 100)
    @Column(name = "group_name", length = 100, nullable = false)
    private String groupName;

    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * 논리적 삭제 여부 (Soft Delete)
     * DB 컬럼명: IS_DELETED (Oracle에서는 0:미삭제, 1:삭제)
     */
    @Column(name = "IS_DELETED", nullable = false)
    private boolean deleted = false;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    // CommonDetail 과의 연관 관계 - 1:N
    // fetch = FetchType.LAZY를 사용하여 즉시 로딩을 방지 (성능 최적화)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<CommonCodeDetail> details = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommonCodeGroup that)) return false;
        return Objects.equals(getGroupCode(), that.getGroupCode());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getGroupCode());
    }
}
