#  _Liquibase + Oracle + Maven 정리 가이드_  
Daangcool Stack Project | 2025 Edition

---

## ️ 1️⃣ Liquibase란?

> Liquibase는 데이터베이스의 스키마(테이블 구조, 시퀀스, 제약조건 등)를  
> 버전별로 관리하고, 코드처럼 추적할 수 있게 해주는 DB 형상관리 도구입니다.

---

##  2️⃣ 주요 개념

| 용어 | 설명 |
|------|------|
| changelog | Liquibase가 사용하는 XML 파일로, DB 변경 내역(DDL)을 저장 |
| master.xml | 모든 changelog를 include 하는 “루트 changelog” |
| generateChangeLog | 실제 DB 스키마를 XML changelog로 내보내기 |
| diffChangeLog | Hibernate 엔티티(JPA 모델)와 DB를 비교하여 차이점만 changelog로 생성 |
| update | changelog를 실행하여 실제 DB에 반영 |

---

##  3️⃣ Oracle에서 대문자로 나오는 이유

| 원인 | 설명 |
|------|------|
| Oracle은 모든 객체명을 내부적으로 대문자로 저장 | Liquibase는 DB 메타데이터를 그대로 읽기 때문에 changelog가 전부 대문자로 생성됨 |
| `objectQuotingStrategy=LEGACY` | 따옴표를 제거하지만, 대문자 → 소문자 변환은 안 함 |

---

##  4️⃣ Steve의 표준 전략

###  목표
> Liquibase changelog를 소문자 기반으로 유지하면서  
> 오라클·PostgreSQL 등 어떤 DB에서도 작동하게 만들기

###  전략 요약

| 단계 | 설명 |
|------|------|
| ① `generateChangeLog` 로 changelog 생성 | Oracle → XML 파일 (대문자) |
| ② 소문자 변환 스크립트 실행 | XML 내부 이름을 소문자로 일괄 변경 |
| ③ `LEGACY` 모드로 update 실행 | DB마다 알아서 대/소문자 규칙 적용 |
| ④ changelog는 항상 소문자 버전으로 유지 | PostgreSQL, H2, Oracle 호환 완벽 |

---

##  5️⃣ changelog 생성 명령어

```bash
./mvnw liquibase:generateChangeLog \
  -Pdev \
  -Dliquibase.outputChangeLogFile=src/main/resources/config/liquibase/changelog/202510081636.xml \
  -Dliquibase.objectQuotingStrategy=LEGACY
```

---

##  6️⃣ Hibernate 엔티티 기준으로 diff 생성

```bash
./mvnw liquibase:diffChangeLog \
  -Pdev \
  -Dliquibase.objectQuotingStrategy=LEGACY \
  -Dliquibase.referenceUrl="hibernate:spring:com.daangcool.stack.domain?dialect=org.hibernate.dialect.Oracle12cDialect"
```

 `diffChangeLog`는 Hibernate Naming 전략을 사용하기 때문에  
테이블/컬럼 이름이 자동으로 소문자로 나옵니다.

---

## ️ 7️⃣ changelog 소문자 변환 스크립트 (macOS용)

 lowercase-liquibase.sh
```bash
#!/bin/bash
# Liquibase XML을 소문자로 변환하는 스크립트
# 사용법: ./lowercase-liquibase.sh ./202510081636.xml

if [ -z "$1" ]; then
  echo " XML 파일 경로를 입력하세요."
  exit 1
fi

FILE="$(cd "$(dirname "$1")" && pwd)/$(basename "$1")"

perl -i.bak -pe '
  s/(tableName=")([A-Z0-9_]+)"/$1.lc($2)."/eg;
  s/(column name=")([A-Z0-9_]+)"/$1.lc($2)."/eg;
  s/(sequenceName=")([A-Z0-9_]+)"/$1.lc($2)."/eg;
  s/(foreignKeyName=")([A-Z0-9_]+)"/$1.lc($2)."/eg;
  s/(constraintName=")([A-Z0-9_]+)"/$1.lc($2)."/eg;
' "$FILE"

echo " 소문자 변환 완료 → ${FILE}"
```

실행 예시
```bash
./lowercase-liquibase.sh ./202510081636.xml
```

---

##  8️⃣ Maven liquibase 플러그인 설정 예시

```xml
<plugin>
  <groupId>org.liquibase</groupId>
  <artifactId>liquibase-maven-plugin</artifactId>
  <version>${liquibase.version}</version>

  <configuration>
    <changeLogFile>config/liquibase/master.xml</changeLogFile>
    <driver>oracle.jdbc.OracleDriver</driver>
    <url>${liquibase-plugin.url}</url>
    <username>${liquibase-plugin.username}</username>
    <password>${liquibase-plugin.password}</password>
    <verbose>true</verbose>

    <systemProperties>
      <property>
        <name>liquibase.objectQuotingStrategy</name>
        <value>LEGACY</value>
      </property>
    </systemProperties>

    <referenceUrl>
      hibernate:spring:com.daangcool.stack.domain?
      dialect=org.hibernate.dialect.Oracle12cDialect&amp;
      hibernate.physical_naming_strategy=com.daangcool.stack.config.OracleCaseInsensitiveNamingStrategy&amp;
      hibernate.implicit_naming_strategy=org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
    </referenceUrl>
  </configuration>

  <dependencies>
    <dependency>
      <groupId>org.liquibase.ext</groupId>
      <artifactId>liquibase-hibernate6</artifactId>
      <version>${liquibase.version}</version>
    </dependency>
  </dependencies>
</plugin>
```

---

## ️ 9️⃣ 프로젝트 폴더 구조 예시

```
src/main/resources/
└── config/
    └── liquibase/
        ├── changelog/
        │   ├── 202510081636_initial_schema.xml
        │   ├── 202510091100_add_user_table.xml
        │   └── ...
        └── master.xml
```

 master.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.14.xsd">
    <include file="config/liquibase/changelog/202510081636_initial_schema.xml"/>
    <include file="config/liquibase/changelog/202510091100_add_user_table.xml"/>
</databaseChangeLog>
```

---

## ️  실행 요약 명령어

| 동작 | 명령 |
|------|------|
| DB → XML changelog 생성 | `./mvnw liquibase:generateChangeLog -Pdev -Dliquibase.outputChangeLogFile=src/main/resources/config/liquibase/changelog/202510081636.xml` |
| changelog → DB 반영 | `./mvnw liquibase:update -Pdev -Dliquibase.objectQuotingStrategy=LEGACY` |
| Hibernate 엔티티 → changelog | `./mvnw liquibase:diffChangeLog -Pdev -Dliquibase.objectQuotingStrategy=LEGACY -Dliquibase.referenceUrl="hibernate:spring:com.daangcool.stack.domain?dialect=org.hibernate.dialect.Oracle12cDialect"` |
| changelog 이름 소문자 변환 | `./lowercase-liquibase.sh ./202510081636.xml` |

---

##  11️⃣ 핵심 요약

| 항목 | 요약 |
|------|------|
| DB 스키마를 XML로 내보내기 | `generateChangeLog` |
| JPA 모델과 DB 비교 | `diffChangeLog` |
| XML 적용 | `update` |
| changelog 이름 규칙 | 소문자 유지 |
| 실행 전략 | `LEGACY` (따옴표 제거, DB 규칙 적용) |
| 대문자 문제 해결 | lowercase 변환 스크립트 |
| DB 호환성 |  Oracle 대문자 /  PostgreSQL 소문자 |

---

>  결론
>
> Liquibase는 DB 구조를 추적하고 버전 관리하는 강력한 도구지만,  
> Oracle에서는 changelog가 항상 대문자로 나오므로  
> changelog를 소문자 변환 후 LEGACY 모드로 관리하는 것이  
> 가장 안정적이며 이식성이 높습니다.

---

 Document version: 2025-10-08  
 Maintained by: *SangHyouk Jin — Daangcool Stack Project*
