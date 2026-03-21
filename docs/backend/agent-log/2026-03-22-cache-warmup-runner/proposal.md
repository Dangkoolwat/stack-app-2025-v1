# 해결 방안 제안 (Proposal)

## 제안 방향
- Spring Boot의 `ApplicationRunner` 인터페이스를 구현하는 `CacheWarmupRunner` 컴포넌트를 생성하여 애플리케이션 초기 구동 직후 캐싱 동작을 백그라운드에서 강제 실행(`Warm-up`)시킵니다.
- 자주 사용하는 데이터(예: 공통 코드 전체 `findallGroups()`, 특정 필수 그룹의 디테일 `findAllDetailsByGroup("COMMON")`)를 사전 쿼리하여 Redis 캐시에 미리 로드해둡니다.

## 선택 이유 및 기대 효과
- **Cold Start 방어**: 서버 재실행 직후 프론트엔드 첫 접속 시 딜레이가 원천 차단됩니다.
- **DB 부하 감소**: 최초 대규모 트래픽 인입 시 무분별한 DB Read (Cache Stampede) 현상을 예방할 수 있습니다.
- **간단한 구현**: Spring의 `ApplicationRunner`는 애플리케이션 라이프사이클에 자연스레 개입하여 복잡한 트리거나 비동기 설계 없이 즉각 안전한 캐시 초기화가 가능합니다.
