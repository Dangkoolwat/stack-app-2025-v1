---
agent: Antigravity
created_at: 2026-03-26 (목요일)
language: ko
---

문제 현상
1. CommonCodeService 에서 캐시 삭제 시 NullPointerException 발생.
2. GlobalSettingsService 에서 캐시 이름 불일치로 인해 캐시 기능이 정상 동작하지 않음.
3. CommonCodeDetail 삭제 후 동일한 코드로 재생성 시 중복 오류 발생 (소프트 삭제 제약).

재현 경로
1. 공통 코드 상세 저장 또는 삭제 시 서비스 로직에서 캐시 매니저를 호출할 때 발생.
2. 설정 변경 후 조회 시 DB에서 항상 새로 읽어오는 현상 확인.
3. 공통 코드 삭제 후 동일 코드로 추가 버튼 클릭 시 서버에서 400 Bad Request 에러 반환.

원인 분석
1. 서비스 계층에서는 camelCase 이름을 사용했으나 설정 파일에는 snake_case로 등록되어 null 반환 및 requireNonNull 검증 실패.
2. GlobalSettingsService 내부에 하드코딩된 캐시 이름 상수가 전체 표준과 불일치.
3. DB의 code 컬럼에 유니크 제약 조건이 있고, 소프트 삭제된 행이 여전히 존재하여 새 행 삽입을 차단함. 또한 게시판 테이블 등에서 해당 코드를 외래 키로 참조하고 있어 물리 삭제가 불가능함.

영향 범위

- 공통 코드 관리 기능 전체 (조회, 생성, 수정, 삭제)
- 전역 설정 관리 기능
- 게시판 관련 코드 참조 로직
