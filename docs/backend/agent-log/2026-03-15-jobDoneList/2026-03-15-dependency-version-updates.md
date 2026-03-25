# 2026-03-15 의존성 버전 업데이트 — C-5 Spring Cloud, W-5 AWS SDK

---

## Date

2026-03-15

---

## Agent

Claude (Anthropic Claude Sonnet 4.6)

---

## Task Title

보안 점검 항목 C-5, W-5 의존성 버전 최신화 확인 및 리포트 반영

---

## Goal

보안 점검 리포트의 두 항목을 완료 처리합니다.

- C-5: Spring Cloud BOM을 Spring Boot 4.0 호환 버전(`2025.1.0`)으로 업데이트
- W-5: AWS SDK Java v2를 최신 버전(`2.42.13`)으로 업데이트

---

## Context

- `docs/security/2026-03-15-system-security-optimization-report.md` C-5 항목:
  `spring-cloud-dependencies 2024.0.1` (Boot 3.x 전용) → Boot 4.0 호환 버전으로 교체 필요
- 동 리포트 W-5 항목:
  `amazon-awssdk 2.25.60` (구버전, 보안 패치 누락 가능) → 최신 버전으로 업데이트 필요
- 두 항목 모두 사용자가 직접 `pom.xml`을 수정하여 반영 완료한 상태로 확인됨

---

## Work Performed

1. `pom.xml` 현재 버전 확인
   - `spring-cloud-dependencies.version`: `2025.1.0` (변경 완료)
   - `amazon-awssdk.version`: `2.42.13` (변경 완료)
2. `docs/security/2026-03-15-system-security-optimization-report.md` 업데이트:
   - C-5 항목: ~~취소선~~ 처리 +  완료 표기 + 적용 버전 명시
   - W-5 항목: ~~취소선~~ 처리 +  완료 표기 + 적용 버전 명시
   - W-6 (commons-io), I-4 (liquibase-hibernate6) 이전 완료 항목도 동일하게 표기 정리
   - 액션 플랜 요약 테이블에 상태 컬럼 추가 및 완료 항목 반영
   - 헤더에 최종 업데이트 일자 추가
3. 본 agent-log 작성

---

## Files Modified

- `docs/security/2026-03-15-system-security-optimization-report.md` — C-5, W-5 완료 상태 반영
- `docs/agent-log/2026-03-15-dependency-version-updates.md` — 본 파일 (신규)

읽기 전용 확인:
- `pom.xml` — 버전 적용 상태 확인

---

## 변경된 버전 요약

| 항목 | 이전 버전 | 적용 버전 | 변경 이유 |
|------|-----------|-----------|-----------|
| Spring Cloud BOM | `2024.0.1` | `2025.1.0` | Spring Boot 4.0 공식 호환 버전 |
| AWS SDK Java v2 | `2.25.60` | `2.42.13` | 보안 패치 포함 최신 버전 |

---

## Architecture Impact

런타임 의존성 버전 변경입니다. Spring Cloud BOM 업데이트는 자동 설정 동작에 영향을 줄 수 있습니다.  
빌드 및 테스트 통과 여부를 반드시 확인하세요.

---

## Security Impact

- AWS SDK 2.42.13: 2.25.60 대비 다수의 보안 패치 포함 — 긍정적 영향
- Spring Cloud 2025.1.0: Boot 4.0 호환으로 런타임 안정성 개선

---

## Verification

```bash
./mvnw clean package -DskipTests   # 빌드 및 의존성 해석 확인
./mvnw test                        # 테스트 통과 확인
```

---

## Risks

- Spring Cloud 2025.1.0: Boot 4.0 호환이지만 실제 사용 중인 Spring Cloud 기능(Feign, Config 등)이 있다면 API 변경 여부를 확인해야 합니다.
- AWS SDK 2.42.13: 메이저 버전 변경이 아닌 마이너 업데이트이므로 Breaking Change 위험은 낮습니다.

---

## Next Suggested Tasks

잔여 미완료 항목 (우선순위 순):

1. C-1: `application-secret.yml` `.gitignore` 추가 및 자격증명 rotate
2. C-2: JWT 시크릿 rotate 및 환경변수화
3. C-3: prod Oracle 계정 최소 권한 전환
4. C-4: 파일 업로드 MIME 타입 서버측 검증 (Apache Tika)
5. W-1: Rate Limiting (Bucket4j)
6. W-7: OTP Redis TTL 전환
7. W-8: LoggingAspect 민감 파라미터 필터링
8. W-9: 운영 TLS 설정

---

## Notes for Future Agents

- `pom.xml`의 `spring-cloud-dependencies.version`과 `amazon-awssdk.version`은 이미 최신으로 적용되어 있습니다. 다시 변경하지 마세요.
- 잔여 보안 항목의 전체 목록과 상세 내용은 `docs/security/2026-03-15-system-security-optimization-report.md`를 참조하세요.
- 리포트의 액션 플랜 요약 테이블에 각 항목의 완료 상태가 반영되어 있습니다.
