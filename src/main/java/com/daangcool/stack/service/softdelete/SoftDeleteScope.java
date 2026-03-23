package com.daangcool.stack.service.softdelete;

/**
 * 요청/작업 단위로 "삭제 데이터 포함 조회" 스코프를 제어합니다.
 *
 * 기본값: includeDeleted=false (삭제 데이터 제외)
 */
public final class SoftDeleteScope {

    private static final ThreadLocal<Integer> INCLUDE_DELETED_DEPTH = ThreadLocal.withInitial(() -> 0);

    private SoftDeleteScope() {}

    public interface ScopeToken extends AutoCloseable {
        @Override
        void close();
    }

    public static boolean isIncludeDeleted() {
        return INCLUDE_DELETED_DEPTH.get() > 0;
    }

    public static ScopeToken openIncludeDeleted() {
        INCLUDE_DELETED_DEPTH.set(INCLUDE_DELETED_DEPTH.get() + 1);
        return () -> {
            int depth = INCLUDE_DELETED_DEPTH.get() - 1;
            if (depth <= 0) {
                INCLUDE_DELETED_DEPTH.remove();
            } else {
                INCLUDE_DELETED_DEPTH.set(depth);
            }
        };
    }
}

