package com.daangcool.stack.config.hibernate;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import com.daangcool.stack.service.softdelete.IncludeDeleted;
import com.daangcool.stack.service.softdelete.SoftDeleteScope;

/**
 * {@link IncludeDeleted}가 선언된 실행 구간에서는 소프트 삭제 필터를 비활성화할 수 있게 스코프를 열어줍니다.
 */
@Aspect
@Component
public class IncludeDeletedAspect {

    @Around("@within(com.daangcool.stack.service.softdelete.IncludeDeleted) || @annotation(com.daangcool.stack.service.softdelete.IncludeDeleted)")
    public Object includeDeletedScope(ProceedingJoinPoint pjp) throws Throwable {
        try (SoftDeleteScope.ScopeToken ignored = SoftDeleteScope.openIncludeDeleted()) {
            return pjp.proceed();
        }
    }
}
