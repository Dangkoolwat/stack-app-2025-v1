# 셀프 체크 (Self-Check)

- [x] Architecture compliance: 운영과 개발 환경 프로파일의 의존성이 컴포넌트 빈(`@Profile("prod")`) 자체 레벨에서 격리 분리되므로 아키텍처 원칙 준수.
- [x] No hidden breaking changes: `dev` 와 `test` 등에서 이 검증 로직으로 인해 파이프라인 CI/CD가 멈추는 에러 사례를 완전히 예방함. 오직 `prod` 만 영향받음.
- [x] Security impact reviewed: `JWT_SECRET`을 의무화하고 길이(64자 이상)까지 점검하도록 구축하여 JWT가 취약한 키로 브루트포스 뚫리는 것을 사전에 방지.
- [x] Config / dependency impact checked: 기존 `@Value` 주입 기법을 그대로 따르며 OS 변수나 Spring Yaml 변수, `.env` 모두와 충돌 없이 바인딩됨.
