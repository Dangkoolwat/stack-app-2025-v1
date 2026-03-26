package com.daangcool.stack.common.constant;

/**
 * 프로젝트 전체에서 사용하는 캐시 이름 상수 클래스입니다.
 * -----------------------------------------------------------
 * 분산된 서비스의 캐시 이름을 통합 관리하여 명칭 불일치를 방지하고
 * CacheConfiguration과의 정합성을 유지합니다.
 * -----------------------------------------------------------
 */
public final class CacheNames {

    // --- Global Settings ---
    public static final String SETTINGS = "settings";

    // --- Common Code ---
    public static final String COMMON_GROUPS = "common_groups";
    public static final String COMMON_GROUP_LIST = "common_group_list";
    public static final String COMMON_DETAILS = "common_details";
    public static final String COMMON_DETAILS_BY_GROUP = "common_details_by_group";

    // --- Board ---
    public static final String BOARD_BY_ID = "board_by_id";
    public static final String BOARD_PAGE = "board_page";
    public static final String BOARD_SEARCH = "board_search";
    public static final String BOARD_NOTICES = "board_notices";
    public static final String BOARD_COUNT_TOTAL = "board_count_total";
    public static final String BOARD_COUNT_BY_USER = "board_count_by_user";

    // --- Comment ---
    public static final String COMMENT_BY_ID = "comment_by_id";
    public static final String COMMENT_BY_BOARD = "comment_by_board";
    public static final String COMMENT_SEARCH = "comment_search";
    public static final String COMMENT_COUNT_BY_BOARD = "comment_count_by_board";
    public static final String COMMENT_COUNT_BY_USER = "comment_count_by_user";
    public static final String COMMENT_STATS = "comment_stats";

    // --- Tag ---
    public static final String TAG_BY_ID = "tag_by_id";
    public static final String TAG_ALL = "tag_all";
    public static final String TAG_PREFIX = "tag_prefix";
    public static final String TAG_POPULAR = "tag_popular";

    // --- Upload ---
    public static final String UPLOAD_BY_ID = "upload_by_id";
    public static final String UPLOAD_BY_BOARD = "upload_by_board";
    public static final String UPLOAD_STATS = "upload_stats";
    public static final String UPLOAD_ALL = "upload_all";

    private CacheNames() {
        // 인스턴스화 방지
    }
}
