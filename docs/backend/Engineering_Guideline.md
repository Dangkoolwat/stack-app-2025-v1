# Backend Engineering Guideline

## 1. 구조적 작업 계획 (Plan-First Approach)
모든 백엔드 기능 개발 및 DB 변경 작업은 다음 4단계를 엄격히 준수합니다. 에이전트는 단계별 문서화 및 승인 없이 다음 단계로 진행할 수 없습니다.

### Step 1: 논의 및 분석 (Initial Discussion)
- 요구사항과 현재 시스템(Java 코드, Liquibase 스케줄)의 한계점을 분석합니다.
- 작업 브랜치를 생성하기 전, 변경 범위를 명확히 논의합니다.

### Step 2: 설계 제안 및 토론 (Proposal & Debate)
- 제안서 작성: `docs/proposals/` 내에 작업 계획서를 작성합니다. (API 명세, DB 변경안 포함)
- 토론: 제안된 설계의 트레이드오프(성능, 보안, 유지보수성)를 검토합니다.
- 확정: 설계안이 승인되면 이를 최종 가이드라인으로 삼아 개발을 시작합니다.

### Step 3: 구현 및 자체 검증 (Implementation & Test)
- 설계안에 따라 구현을 진행합니다.
- 오라클 주의: 쿼리 작성 시 인덱스 활용 여부를 검토하고 N+1 문제가 발생하지 않도록 `Join Fetch` 등을 적절히 사용합니다.
- 테스트: 단위 테스트 및 JHipster 보안 가드가 포함된 통합 테스트를 수행합니다.

### Step 4: 완료 보고 및 문서화 (Completion & Finalize)
- 작업 완료 후 최종 API 명세와 변경된 DB 구조를 기록한 '완료 보고서'를 작성합니다.
- 기술 부채나 향후 개선이 필요한 사항을 명시하여 지식을 전파합니다.

## 2. 에이전트 전용 금지 가이드 (Explicit Bans for AI)
- BAN 1: Liquibase를 통하지 않은 직접적인 DB 스키마 수정 제안.
- BAN 2: Resource(Controller) 계층에서 Entity 클래스를 직접 반환하거나 파라미터로 받는 행위.
- BAN 3: 오라클 예약어(USER, ORDER, GROUP 등)를 물리 테이블/컬럼명으로 직접 사용하는 행위.
- BAN 4: 한 트랜잭션 내에서 과도하게 긴 작업 수행 (DB 커넥션 점유 방지).

## 3. 일반 DB 사용 시 공통 주의사항
- NULL 처리: 오라클은 빈 문자열(`''`)을 `NULL`로 처리하므로, 타 DB와 호환성을 고려한 방어적 코딩을 수행합니다.
- Paging: 대량 데이터 조회 시 반드시 `Offset-Fetch` 기반의 페이징 처리를 적용합니다.
- Timezone: 서버, 어플리케이션, 데이터베이스 간의 시간대 설정을 항상 일관되게 유지합니다. (UTC 권장이나 Asia/Seoul 기준)

## 4. 리뷰 체크리스트
- [ ] 설계 제안(Proposal)이 먼저 작성되고 승인되었는가?
- [ ] Liquibase 변경 로그가 오라클 문법 및 식별자 제약을 준수하는가?
- [ ] 모든 API 응답이 DTO를 통해 정규화된 에러 형식을 따르는가?
- [ ] 작업 완료 후 최종 문서 업데이트가 완료되었는가?

## 5. JSON / Cache 변경 작업 규칙

### 5.1 Jackson 변경 작업

Jackson 관련 변경 작업은 반드시 아래 절차를 따른다.

#### Step 1: dependency 분석
- mvn dependency:tree 수행
- Jackson 2 존재 여부 확인

#### Step 2: 혼용 제거
- com.fasterxml.jackson 전부 제거
- tools.jackson으로 통일

#### Step 3: 코드 정리
- import 전수 교체
- ObjectMapper 직접 생성 제거

#### Step 4: 검증
- Swagger 정상 동작 확인
- /v3/api-docs 확인
- JSON 직렬화 오류 확인

---

### 5.2 Cache 변경 작업

Cache 구조 변경 시 반드시 다음을 따른다.

#### Step 1: 캐시 대상 분류
- 인증 데이터인지 여부 확인
- 조회 데이터인지 여부 확인

#### Step 2: 금지 대상 확인
다음은 캐시 적용 금지:

- 로그인 관련
- UserDetails
- 권한 정보

#### Step 3: TTL 설계
- 데이터 성격별 TTL 정의

#### Step 4: 영향 분석
- Hibernate L2와 중복 여부
- JSON/Binary 충돌 여부

---

### 5.3 Redis 변경 작업

#### 필수 확인
- Redis 연결 수 증가 여부
- RedissonClient 중복 생성 여부

#### 금지
- 서비스별 RedisClient 생성
- 기능별 Redis 연결 확장

---

### 5.4 리뷰 체크리스트 (추가)

- [ ] Jackson 혼용이 없는가?
- [ ] ObjectMapper가 단일 체계인가?
- [ ] 인증 데이터 캐시가 없는가?
- [ ] Redis 연결이 중앙 집중형인가?
- [ ] 캐시 TTL이 적절한가?
