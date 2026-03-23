package com.daangcool.stack.config.hibernate;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.daangcool.stack.service.softdelete.IncludeDeleted;
import com.daangcool.stack.service.softdelete.SoftDeleteHibernateFilter;
import com.daangcool.stack.service.softdelete.SoftDeleteScope;

/**
 * 모든 Repository 호출에 대해 소프트 삭제 필터를 기본으로 활성화합니다.
 *
 * - 기본: is_deleted = 0 조건 자동 적용
 * - 예외: {@link IncludeDeleted} 스코프에서는 필터 미적용 (삭제 데이터 포함)
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SoftDeleteHibernateFilterAspect {

    private final EntityManager entityManager;

    public SoftDeleteHibernateFilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Around("execution(* com.daangcool.stack.repository..*(..))")
    public Object applySoftDeleteFilter(ProceedingJoinPoint pjp) throws Throwable {
        if (SoftDeleteScope.isIncludeDeleted()) {
            return pjp.proceed();
        }

        Session session = entityManager.unwrap(Session.class);
        Filter existing = session.getEnabledFilter(SoftDeleteHibernateFilter.SOFT_DELETE_FILTER_NAME);
        boolean enabledHere = (existing == null);
        if (enabledHere) {
            session.enableFilter(SoftDeleteHibernateFilter.SOFT_DELETE_FILTER_NAME);
        }

        try {
            return pjp.proceed();
        } finally {
            if (enabledHere) {
                session.disableFilter(SoftDeleteHibernateFilter.SOFT_DELETE_FILTER_NAME);
            }
        }
    }
}
