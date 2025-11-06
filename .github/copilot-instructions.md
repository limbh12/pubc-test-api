# Copilot 지침: PUBC 테스트 API 프로젝트

## 프로젝트 개요

이 프로젝트는 PUBC(공통연계모듈) 인증/로깅 프레임워크를 사용하는 **공공데이터 포털 OpenAPI 통합 시스템**을 위한 **문서 전용 설계 프로젝트**입니다. **아직 소스코드는 없으며** - 포괄적인 아키텍처 및 설계 문서만 존재합니다.

**도메인**: 읽기 전용 문화시설 정보 API (박물관, 도서관, 미술관)  
**아키텍처**: PUBC 통합 계층을 포함한 7계층 아키텍처  
**기술 스택**: JDK 1.8, Spring 4.3.30, Apache CXF 3.3.13, MyBatis 3.5.13, iBATIS 2.3.4

## 핵심 아키텍처 개념

### 1. PUBC 통합 패턴

PUBC는 한국 정부 공개 API를 위한 필수 인증/로깅 게이트웨이입니다. **모든 API 호출은 반드시**:

1. **인증** `CommonProc.authServiceCall(request)` 호출 → `UserCrtfcVO` 반환
2. **검증** `troblTyCode` 필드 확인:
   - `"00"` = 성공, 비즈니스 로직 진행
   - `"30"` = 유효하지 않은 서비스키
   - `"32"` = 허가되지 않은 IP
3. **사용 로깅** `finally` 블록에서 `UseSttusServiceCall(userCrtfcVO)` 호출 (항상 실행)

```java
// 필수 패턴 - 모든 컨트롤러가 반드시 따라야 함:
UserCrtfcVO userCrtfcVO = commonProc.authServiceCall(request);
if (!"00".equals(userCrtfcVO.getTroblTyCode())) {
    return createErrorResponse(userCrtfcVO);
}
try {
    // 비즈니스 로직
} finally {
    commonProc.useSttusServiceCall(userCrtfcVO); // 항상 로깅
}
```

**이유**: PUBC는 수정할 수 없는 레거시 정부 모듈(`iros_pubc_1.1.jar`)입니다. 모든 통합은 PUBC API에 맞춰야 합니다.

### 2. 읽기 전용 API 설계

**이 시스템은 SELECT 쿼리만 지원합니다** - INSERT/UPDATE/DELETE 작업 없음.

- **도메인 모델**: `TB_CULTURE_FACILITY` (문화시설 마스터 데이터)
- **API 작업**: `getFacilityList()`, `getFacility()`, `getTotalCount()`
- **Mapper 메서드**: MyBatis `CultureFacilityMapper.java`에 `select*` 메서드만 사용

코드 생성 시 **절대로** POST/PUT/DELETE 엔드포인트나 CUD SQL 작업을 생성하지 마세요.

### 3. 이중 SQL Mapper 아키텍처

**두 개의 SQL 매핑 프레임워크가 공존**:

- **MyBatis 3.5.13**: 애플리케이션 코드용 (새로운 `@Mapper` 인터페이스 스타일)
- **iBATIS 2.3.4**: PUBC 모듈 내부용 (레거시 `SqlMapClient`, 수정 금지)

동일한 `DataSource`를 공유하지만 별도 네임스페이스를 사용합니다. Spring 설정에서 둘 다 초기화해야 합니다:

```xml
<!-- 애플리케이션용 MyBatis -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource"/>
</bean>

<!-- PUBC용 iBATIS (iros_pubc_1.1.jar 필요) -->
<bean id="sqlMapClient" class="org.springframework.orm.ibatis.SqlMapClientFactoryBean">
    <property name="dataSource" ref="dataSource"/>
    <property name="configLocation" value="classpath:iros/pubc/sql-map-config.xml"/>
</bean>
```

### 4. Mock 데이터 개발 전략

**권장 방식**: 데이터베이스 설정 대신 인메모리 mock 구현을 사용합니다.

**이유**: DB 설치 없이 개발, 테스트, 데모를 단순화합니다. `CLAUDE.md` 170-400줄에서 완전한 mock 구현을 확인하세요:

- `CultureFacilityMockMapper.java`: Java 8 Stream 필터링을 사용한 정적 mock 데이터
- `MockCommonProc.java`: 미리 정의된 테스트 키를 사용한 PUBC 인증 스텁
- 유효한 테스트 키: `TEST_KEY_001`, `DEMO_KEY`, `DEV_KEY`

Mock 모드는 `context-app.xml`에서 PUBC Spring import를 제외하여 활성화됩니다.

## 개발 워크플로우

### 프로젝트 구조 (목표 구현)

```
src/iros/test/
├── common/
│   ├── CommonProc.java          # PUBC 통합 파사드 (중요)
│   ├── Constants.java
│   ├── exception/               # 커스텀 예외
│   └── util/                    # JSON/XML/날짜 유틸리티
├── rest/
│   ├── controller/              # JAX-RS 엔드포인트 (GET만)
│   └── dto/                     # ApiResponse, ErrorResponse
├── soap/
│   ├── service/                 # JAX-WS 서비스 (조회만)
│   └── dto/                     # SOAP 응답 래퍼
├── facility/                    # 도메인 계층
│   ├── service/                 # 비즈니스 로직 (SELECT만)
│   ├── dao/                     # MyBatis @Mapper 인터페이스
│   └── vo/                      # CultureFacilityVO
└── config/                      # Spring Java Config 클래스
```

### 주요 설정 파일

- `docs/00_기술스택_JDK18.md`: 정식 의존성 버전
- `docs/02_시스템아키텍처설계서.md`: 7계층 아키텍처 상세
- `CLAUDE.md`: AI 전용 구현 패턴 및 mock 코드 예제
- `PUBC.md`: PUBC 모듈 내부 (62개 소스파일 분석)
- `OASIS.md`: OpenAPI 자동생성툴 참조 아키텍처

### REST API 패턴

**엔드포인트 템플릿** (`docs/03_API명세서.md`에서):

```
GET /api/cultureFacilities?serviceKey={key}&facilityType={type}&regionCode={code}&pageNo=1&numOfRows=10
```

**응답 형식**:
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    "totalCount": 150,
    "pageNo": 1,
    "numOfRows": 10,
    "items": [/* 시설 목록 */]
  }
}
```

**에러 코드** (PUBC에서):
- `000`: 성공
- `030`: 유효하지 않은 서비스키 (`SERVICE_KEY_IS_NOT_REGISTERED_ERROR`)
- `031`: 만료된 서비스 (`DEADLINE_HAS_EXPIRED_ERROR`)
- `032`: 허가되지 않은 IP (`UNREGISTERED_IP_ERROR`)

### 데이터베이스 스키마

**애플리케이션 테이블** (`docs/04_데이터베이스스키마설계서.md`):
```sql
TB_CULTURE_FACILITY (
  FACILITY_ID      VARCHAR(20) PK,
  FACILITY_NAME    VARCHAR(100),
  FACILITY_TYPE    VARCHAR(50),   -- 박물관/미술관/도서관
  REGION_CODE      VARCHAR(2),    -- 11=서울, 26=부산, 41=경기
  ADDRESS, PHONE, LATITUDE, LONGITUDE, OPEN_TIME, HOMEPAGE, MANAGE_AGENCY
)
```

**PUBC 테이블** (`iros_pubc_1.1.jar`에서 관리):
- `TN_PUBC_USER_CRTFC`: 서비스키 인증
- `TN_PUBC_USE_STTUS_INFO`: API 사용 현황 로깅

## Java 8 사용 패턴

**Java 8 최신 기능 활용** (`docs/00_기술스택_JDK18.md`에 따라):

```java
// 시설 필터링을 위한 Stream API
facilities.stream()
    .filter(f -> "박물관".equals(f.getFacilityType()))
    .filter(f -> "11".equals(f.getRegionCode()))
    .collect(Collectors.toList());

// null 처리를 위한 Optional
Optional.ofNullable(mapper.selectFacility(id))
    .orElseThrow(() -> new NotFoundException("시설 없음"));

// 서비스 계층의 Lambda
userList.forEach(user -> log.info("User: {}", user.getUserName()));
```

## 프로젝트별 규칙

1. **패키지 네이밍**: `iros.test.*` (기관 표준, `com.*` 사용 금지)
2. **VO 네이밍**: Value Object는 `VO`로 끝남 (예: `CultureFacilityVO`, `UserCrtfcVO`)
3. **에러 메시지**: 사용자 대면 메시지는 한국어
4. **지역 코드**: 표준 한국 행정구역 코드 사용 (11=서울, 26=부산 등)
5. **시설 유형**: 박물관 (museum), 미술관 (art gallery), 도서관 (library)

## 외부 의존성

**중요 JAR 파일** (`lib/`에 위치):
- `iros_pubc_1.1.jar`: PUBC 모듈 (Ver 2.3, Java 5로 컴파일, Java 8에서 실행)
- `iros_cipher.jar`: PUBC 암호화 유틸리티

**Maven 좌표** (`docs/00_기술스택_JDK18.md`에서):
```xml
<spring.version>4.3.30.RELEASE</spring.version>
<cxf.version>3.3.13</cxf.version>
<mybatis.version>3.5.13</mybatis.version>
```

## 테스트 전략

**데이터베이스 불필요** - mock 구현 사용:

1. 정적 시설 목록으로 `CultureFacilityMapper` Mock
2. 하드코딩된 서비스키로 `CommonProc` Mock
3. CXF 테스트 클라이언트로 REST 엔드포인트 테스트
4. Mock WSDL로 SoapUI를 통한 SOAP 서비스 테스트

**유효한 테스트 자격증명**:
- 서비스키: `TEST_KEY_001`, `TEST_KEY_002`, `DEMO_KEY`
- 지역 코드: `11` (서울), `26` (부산), `41` (경기)

## 참조 문서 색인

- `docs/01_요구사항정의서.md`: 기능적/비기능적 요구사항
- `docs/02_시스템아키텍처설계서.md`: 7계층 아키텍처, 컴포넌트 다이어그램
- `docs/03_API명세서.md`: 완전한 REST/SOAP API 명세
- `docs/04_데이터베이스스키마설계서.md`: DDL, ERD, 시드 데이터
- `docs/05_클래스다이어그램및패키지구조.md`: UML 다이어그램, 패키지 구조
- `docs/06_JDK18기술스택및호환성분석.md`: 기술 스택 호환성 매트릭스

## 피해야 할 함정

- ❌ **PUBC 인증 건너뛰지 말 것** - 모든 엔드포인트는 반드시 `authServiceCall()` 호출
- ❌ **`finally`에서 로깅 잊지 말 것** - 한국 정부 API는 법적으로 사용 추적 필수
- ❌ **쓰기 작업 생성 금지** - 설계상 읽기 전용 API
- ❌ **MyBatis와 iBATIS 혼용 금지** - 별도 네임스페이스 유지
- ❌ **PUBC JAR 내부 수정 금지** - 블랙박스 레거시 모듈
- ❌ **Java 9+ 기능 사용 금지** - 프로젝트는 JDK 1.8 호환성 목표

## 확인이 필요한 사항

스펙으로부터 구현 시 확인할 사항:

1. Mock 모드를 기본으로 할지, 또는 mock과 실제 DB 설정 둘 다 제공할지?
2. 박물관/미술관/도서관 외에 추가 시설 유형이 있는지?
3. SOAP과 REST가 동일한 서비스 계층을 공유할지, 또는 별도 구현할지?
4. 규정 준수를 위해 어떤 수준의 로깅 상세도가 필요한지 (최소 vs. 전체 요청/응답)?
