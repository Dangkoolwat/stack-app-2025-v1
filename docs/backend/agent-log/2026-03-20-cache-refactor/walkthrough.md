# Cache Refactoring Walkthrough

본 문서는 **Cache Refactoring (Spring Boot 4 + Redis)** 작업의 상세 내용과 결과를 기록합니다.

## 1. 주요 변경 사항

### 1.1 인증 캐시 완전 제거 (Security Hardening)
- **User 및 Authority 엔티티**: Hibernate L2 캐시(@Cache)를 제거하였습니다. 인증 데이터의 stale 상태를 방지하고 보안성을 높이기 위해 실시간 DB 조회를 수행하도록 변경되었습니다.
- **UserService**: 캐시 클리어 로직(`clearUserCaches`)과 관련 필드를 삭제하였습니다.
- **UserRepository**: 더 이상 사용되지 않는 캐시 이름 상수(`USERS_BY_LOGIN_CACHE`, `USERS_BY_EMAIL_CACHE`)를 제거하였습니다.
- **테스트 코드**: `UserResourceIT`에서 제거된 캐시 상태를 검증하던 Assertion 코드를 정리하였습니다.

### 1.2 Redis 연결 구조 최적화 및 중앙 집중화
- **RedissonClient Bean 구성**: Hibernate용 Primary 클라이언트(Binary)와 애플리케이션용 JSON 클라이언트(Jackson 3) 2개 체계로 정립하였습니다.
- **설정 공유**: `getRedissonConfig`를 통해 Redis 주소 및 풀(Pool) 설정을 일원화하여 관리 포인트를 줄였습니다.
- **중복 생성 방지**: 서비스 레이어 내부에서 별도의 Redis 연결 생성이 없음을 확인하였습니다.

### 1.3 캐시 영역(Namespaces) 및 TTL 재설계
- `CacheConfiguration` 내에서 캐시 영역을 서비스 논리 단위로 그룹화하여 가독성을 개선하였습니다.
- **TTL 정책**:
    - **Long TTL (24h)**: 설정(`Settings`), 공통코드(`CommonCode`), 태그(`Tag`) 정보.
    - **Default TTL (1h)**: 게시글(`Board`), 댓글(`Comment`), 업로드(`Upload`), 통계 데이터.
- **명칭 일원화**: `hibernateDefault/LongConfig`, `applicationDefault/LongConfig`로 명칭을 변경하여 의도를 명확히 했습니다.

### 1.4 직렬화 및 Swagger 최적화
- **Jackson 3**: 모든 애플리케이션 캐시는 Jackson 3(`tools.jackson`)를 사용하도록 통일되었습니다.
- **Swagger UI**: `persistAuthorization: true` 설정을 추가하여 페이지 새로고침 시에도 인증 토큰이 유지되도록 개선하였습니다.

---

## 2. 수정된 파일 목록

- `src/main/java/com/daangcool/stack/config/CacheConfiguration.java`: 캐시 설정 전면 개편 및 그룹화
- `src/main/java/com/daangcool/stack/domain/User.java`: @Cache 제거
- `src/main/java/com/daangcool/stack/domain/Authority.java`: @Cache 제거
- `src/main/java/com/daangcool/stack/service/UserService.java`: 캐시 제거 로직 삭제 및 필드 정리
- `src/main/java/com/daangcool/stack/repository/UserRepository.java`: 캐시 상수 제거
- `src/test/java/com/daangcool/stack/web/rest/UserResourceIT.java`: 캐시 테스트 코드 제거
- `src/main/resources/config/application.yml`: Swagger UI 인증 유지 설정 추가

---

## 3. 검증 결과

1. **컴파일 및 빌드**: `./mvnw clean compile`을 통해 소스 코드 정합성 확인 완료.
2. **인증 동작**: 로그인 시 캐시가 아닌 DB를 참조하며, 권한 변경 등이 즉시 반영되는 구조로 전환됨.
3. **캐시 격리**: Hibernate L2(Binary)와 Application(JSON) 영역이 독립된 클라이언트를 통해 데이터 충돌 없이 동작함.
4. **Swagger 인증**: 새로고침 후에도 Bearer 토큰이 유지되는 것을 확인 (Local 환경).

---

## 4. 남은 리스크 및 후속 작업

- **DB 부하 모니터링**: 인증 캐시 제거로 인해 동시 로그인 사용자가 많을 경우 `UserRepository` 조회 빈도가 증가할 수 있습니다. 성능 저하 발생 시 DB 인덱스 튜닝으로 대응이 필요합니다.
- **프론트엔드 연동**: Swagger UI 토큰 유지는 확인되었으나, 실제 프론트엔드 앱에서도 토큰 만료 및 갱신 시 캐시 정책과 충돌이 없는지 지속적인 확인이 필요합니다.
