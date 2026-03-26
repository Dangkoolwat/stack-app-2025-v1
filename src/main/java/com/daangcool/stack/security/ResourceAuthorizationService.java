package com.daangcool.stack.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * ResourceAuthorizationService
 * -----------------------------------------------------------
 * 객체 수준의 권한(Object-Level Authorization)을 검증하는 서비스입니다.
 * 리소스의 소유자이거나 관리자 권한을 가진 경우에만 작업을 허용합니다.
 * -----------------------------------------------------------
 */
@Service
public class ResourceAuthorizationService {

    private final Logger log = LoggerFactory.getLogger(ResourceAuthorizationService.class);

    /**
     * 현재 사용자가 해당 리소스의 소유자이거나 관리자인지 확인합니다.
     *
     * @param resourceOwnerId 리소스를 소유한 사용자의 ID
     * @return 권한이 있으면 true, 없으면 false
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return true;
        }

        return SecurityUtils.getCurrentUserId()
            .map(currentUserId -> currentUserId.equals(resourceOwnerId))
            .orElse(false);
    }

    /**
     * 현재 사용자가 해당 리소스의 소유자(Login 기준)이거나 관리자인지 확인합니다.
     *
     * @param resourceOwnerLogin 리소스를 소유한 사용자의 Login
     * @return 권한이 있으면 true, 없으면 false
     */
    public boolean isOwnerOrAdminByLogin(String resourceOwnerLogin) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return true;
        }

        return SecurityUtils.getCurrentUserLogin()
            .map(currentLogin -> currentLogin.equals(resourceOwnerLogin))
            .orElse(false);
    }

    /**
     * 현재 사용자가 해당 리소스의 소유자(Login 기준)이거나 관리자인지 검증합니다.
     * 권한이 없는 경우 AccessDeniedException을 발생시킵니다.
     *
     * @param resourceOwnerLogin 리소스를 소유한 사용자의 Login
     * @param entityName 엔티티 이름 (에러 메시지용)
     * @param errorKey 에러 키 (에러 메시지용)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateOwnerOrAdminByLogin(String resourceOwnerLogin, String entityName, String errorKey) {
        if (!isOwnerOrAdminByLogin(resourceOwnerLogin)) {
            log.warn("Authorization failed for user. OwnerLogin: {}, CurrentUserLogin: {}", 
                resourceOwnerLogin, SecurityUtils.getCurrentUserLogin().orElse(null));
            throw new AccessDeniedException("해당 작업에 대한 권한이 없습니다.");
        }
    }

    /**
     * 현재 사용자가 해당 리소스의 소유자이거나 관리자인지 검증합니다.
     * 권한이 없는 경우 AccessDeniedException을 발생시킵니다.
     *
     * @param resourceOwnerId 리소스를 소유한 사용자의 ID
     * @param entityName 엔티티 이름 (에러 메시지용)
     * @param errorKey 에러 키 (에러 메시지용)
     * @throws AccessDeniedException 권한이 없는 경우
     */
    public void validateOwnerOrAdmin(Long resourceOwnerId, String entityName, String errorKey) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            log.warn("Authorization failed for user. OwnerId: {}, CurrentUserId: {}", 
                resourceOwnerId, SecurityUtils.getCurrentUserId().orElse(null));
            throw new AccessDeniedException("해당 작업에 대한 권한이 없습니다.");
        }
    }
}
