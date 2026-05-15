---
name: liquibase-migration
description: >
  Liquibase DB migration patterns for JHipster-based Spring Boot projects.
  Use when creating/modifying changesets, managing rollbacks, or handling
  schema evolution for Oracle/PostgreSQL databases.
---

# Liquibase Migration Expert

DB 스키마 변경 시 안전한 마이그레이션을 위한 가이드. JHipster + Spring Boot + Liquibase 환경에 최적화.

## 1. Core Standards

- **Changelog Format**: XML (JHipster default). YAML/SQL도 허용하되 기존 프로젝트 관례를 따른다.
- **Changeset ID**: `YYYYMMDDHHMMSS-N` 형식 (JHipster convention).
- **Author**: 실제 개발자 이름 또는 에이전트 식별자 (e.g., `architect`, `agent-antigravity`).
- **Immutability**: 이미 적용된 changeset은 절대 수정하지 않는다. 새 changeset으로 보완.

## 2. Changeset Patterns

### 2-1. 테이블 생성
```xml
<!-- 주문 테이블 생성 (비즈니스 요구사항: 주문 관리 도메인) -->
<changeSet id="20260515120000-1" author="architect">
    <createTable tableName="orders">
        <column name="id" type="bigint">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="title" type="varchar(255)">
            <constraints nullable="false"/>
        </column>
        <column name="created_date" type="timestamp" defaultValueComputed="CURRENT_TIMESTAMP"/>
    </createTable>
</changeSet>
```

### 2-2. 컬럼 추가 (안전 패턴)
```xml
<!-- 기존 테이블에 컬럼 추가 시 NOT NULL 제약은 단계적으로 적용 -->
<changeSet id="20260515120000-2" author="architect">
    <addColumn tableName="orders">
        <column name="status" type="varchar(50)" defaultValue="PENDING"/>
    </addColumn>
</changeSet>

<!-- 데이터 마이그레이션 후 NOT NULL 제약 추가 -->
<changeSet id="20260515120000-3" author="architect">
    <addNotNullConstraint tableName="orders" columnName="status" defaultNullValue="PENDING"/>
</changeSet>
```

### 2-3. 인덱스 생성
```xml
<!-- 조회 성능 최적화: 상태별 주문 검색 빈도 높음 -->
<changeSet id="20260515120000-4" author="architect">
    <createIndex indexName="idx_orders_status" tableName="orders">
        <column name="status"/>
    </createIndex>
</changeSet>
```

## 3. Rollback Strategy

- **MUST**: 모든 DDL changeset에 `<rollback>` 블록을 포함한다.
- **자동 롤백 가능**: `createTable`, `addColumn`, `createIndex` (Liquibase가 자동 생성)
- **수동 롤백 필요**: `sql`, `modifyDataType`, 데이터 변환 등

```xml
<changeSet id="20260515120000-5" author="architect">
    <sql>UPDATE orders SET status = 'ACTIVE' WHERE status IS NULL</sql>
    <rollback>
        <sql>UPDATE orders SET status = NULL WHERE status = 'ACTIVE'</sql>
    </rollback>
</changeSet>
```

## 4. Safety Rules

- **Production 배포 전 검증**: `./mvnw liquibase:status` 로 pending changeset 확인.
- **데이터 손실 방지**: `dropTable`, `dropColumn`은 반드시 별도 changeset으로 분리하고 Architect 승인 필수.
- **대용량 테이블**: `addColumn`이 테이블 락을 유발할 수 있으므로, 운영 환경에서는 online DDL 전략 검토.
- **JHipster 통합**: `src/main/resources/config/liquibase/changelog/` 경로에 파일 배치.
- **Master Changelog**: `src/main/resources/config/liquibase/master.xml`에 새 changeset 파일을 include.

## 5. Forbidden Patterns

- ❌ 이미 적용된 changeset 수정 (checksum 에러 유발)
- ❌ `DROP TABLE`/`DROP COLUMN` 없이 데이터 마이그레이션
- ❌ 대용량 데이터 변환을 단일 changeset에서 수행
- ❌ `runOnChange="true"`를 DDL changeset에 사용 (데이터 손실 위험)

## 6. Korean Comment Rule

모든 changeset에 한글 주석으로 변경 사유를 기록한다:
```xml
<!-- 사용자 프로필 테이블에 프로필 이미지 URL 컬럼 추가 (요구사항: #123) -->
```
