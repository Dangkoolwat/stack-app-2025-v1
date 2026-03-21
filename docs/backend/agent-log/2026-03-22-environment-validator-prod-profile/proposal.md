# 해결 방안 제안 (Proposal)

## 제안 방향
- `ApplicationRunner` 인터페이스를 구현한 필수 환경변수 검사 컴포넌트(`EnvironmentValidator`)를 새롭게 작성하여 Spring Boot Application Context 레벨의 기동 시점에 런타임 예외를 발생(Fail-Fast)시킴.
- `@Profile("prod")` 애노테이션을 부착하여, 로컬 브라우징 등 자율성이 보장되어야 할 개발(`dev`) 등 타 프로파일에서는 간섭하지 않도록 제어.

## 선택 이유 및 기대 효과
- **강렬한 Fail-Fast(조기 실패)**: 시작 즉시 `Assert.hasText()` 등에 막혀 기동이 중단되므로 파이프라인(CI/CD)이나 K8s 컨테이너 배포 시 환경 변수 오류를 `CrashLoopBackOff` 상태로 즉결 처분하여 릴리즈 장애 사전 예방 효과.
- **코드 직관성**: 반복문을 순회해 Profile이 `prod` 인지 확인할 필요 없이 Spring 컨텍스트 애노테이션 하나로 깔끔하게 모드 종속성 분리.
