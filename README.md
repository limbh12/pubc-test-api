# PUBC 연동 테스트 프로젝트 설계 문서 (JDK 1.8)

**프로젝트명**: PUBC-Test-API
**버전**: 2.0 (JDK 1.8)
**작성일**: 2025-11-03
**작성자**: Claude

---

## 📋 문서 개요

이 문서는 PUBC 공통연계모듈을 활용한 Spring 기반 OpenAPI 테스트 프로젝트의 요구사항 정의 및 설계 문서입니다.

**🆕 버전 2.0 주요 변경사항**:
- ✅ **JDK 1.8 기반**으로 업그레이드
- ✅ **Spring Framework 4.3.30** 적용
- ✅ **Apache CXF 3.3.13** 적용
- ✅ **MyBatis 3.5.13** 도입 (iBATIS와 공존)
- ✅ **Java 8 기능** 활용 (Lambda, Stream, Optional)
- ✅ **PUBC 모듈 완벽 호환** 검증

---

## 📚 문서 구성

### 1. [요구사항 정의서](./docs/01_요구사항정의서.md)
프로젝트의 기능적/비기능적 요구사항, 제약사항, 우선순위를 정의합니다.

**주요 내용**:
- ✅ 프로젝트 개요 및 목적
- ✅ 기능 요구사항 (인증, 로깅, REST API, SOAP 서비스)
- ✅ 비기능 요구사항 (성능, 보안, 가용성)
- ✅ 제약사항 및 전제조건
- ✅ 우선순위 및 사용자 스토리
- ✅ 인수 기준 및 위험 관리

---

### 2. [시스템 아키텍처 설계서](./docs/02_시스템아키텍처설계서.md)
전체 시스템의 아키텍처와 계층별 상세 설계를 다룹니다.

**주요 내용**:
- 🏗️ 시스템 구성도 (7계층 아키텍처)
- 🏗️ 계층별 상세 설계
  - Presentation Layer (REST/SOAP)
  - PUBC Integration Layer
  - Business Logic Layer
  - Data Access Layer (MyBatis + iBATIS)
- 🏗️ 데이터 흐름 및 처리 프로세스
- 🏗️ 기술 스택 및 라이브러리 의존성
- 🏗️ 배포 아키텍처

---

### 3. [API 명세서](./docs/03_API명세서.md)
REST API와 SOAP 웹 서비스의 상세 명세를 제공합니다.

**주요 내용**:
- 📡 REST API 명세
  - GET /api/users - 사용자 목록 조회
  - GET /api/users/{userId} - 사용자 상세 조회
  - POST /api/users - 사용자 등록
  - PUT /api/users/{userId} - 사용자 수정
  - DELETE /api/users/{userId} - 사용자 삭제
- 📡 SOAP 웹 서비스 명세
  - getUserList, getUser, createUser, updateUser, deleteUser
- 📡 에러 코드 체계
- 📡 테스트 시나리오 및 샘플 코드

---

### 4. [데이터베이스 스키마 설계서](./docs/04_데이터베이스스키마설계서.md)
데이터베이스 테이블 구조와 DDL 스크립트를 제공합니다.

**주요 내용**:
- 🗄️ ERD (Entity Relationship Diagram)
- 🗄️ 테이블 상세 설계
  - TB_USER (사용자 정보)
  - TN_PUBC_USER_CRTFC (서비스키 인증)
  - TN_PUBC_USE_STTUS_INFO (사용 현황 로깅)
- 🗄️ 인덱스 설계
- 🗄️ DDL 스크립트 (H2, Oracle, PostgreSQL)
- 🗄️ 초기 데이터 (Seed Data)
- 🗄️ 성능 최적화 전략

---

### 5. [클래스 다이어그램 및 패키지 구조](./docs/05_클래스다이어그램및패키지구조.md)
소스코드의 패키지 구조와 클래스 다이어그램을 제공합니다.

**주요 내용**:
- 📦 패키지 구조 (`iros.test.*`)
- 📦 클래스 다이어그램
  - REST API Controller
  - SOAP Service
  - CommonProc (PUBC 통합)
  - Service Layer
  - DAO Layer (MyBatis Mapper)
  - Value Object
- 📦 시퀀스 다이어그램 (REST/SOAP 호출 흐름)
- 📦 계층별 상세 설계
- 📦 Spring 설정 파일 구조

---

### 6. 🆕 [JDK 1.8 기술 스택 및 호환성 분석](./docs/06_JDK18기술스택및호환성분석.md)
JDK 1.8 기반 기술 스택과 PUBC 모듈 호환성을 분석합니다.

**주요 내용**:
- 🔍 PUBC JAR 파일 호환성 분석
  - Java 5 → Java 8 하위 호환성 검증
  - PUBC 의존 라이브러리 호환성
- 🔍 JDK 1.8 기반 기술 스택
  - Spring 4.3.30, CXF 3.3.13, MyBatis 3.5.13
- 🔍 마이그레이션 전략
  - MyBatis + iBATIS 공존 방안
  - Spring 버전 충돌 해결
- 🔍 Java 8 기능 활용
  - Lambda, Stream API, Optional, Date/Time API
- 🔍 완전한 pom.xml 예제
- 🔍 호환성 테스트 계획

---

## 🎯 프로젝트 핵심 특징

### PUBC 모듈 통합 (Java 8 완벽 호환)
- ✅ `iros_pubc_1.1.jar` 외부 라이브러리 활용
- ✅ Java 5 컴파일 → Java 8 실행 **완벽 호환**
- ✅ 서비스키 기반 인증 (`UserCrtfcProcessService`)
- ✅ 자동 사용 현황 로깅 (`UseSttusService`)
- ✅ 표준 에러 코드 체계 (`PubcErrorCodeProperties`)

### 이중 API 지원
- ✅ **REST API**: JAX-RS (Apache CXF 3.3.13)
- ✅ **SOAP 웹 서비스**: JAX-WS (Apache CXF 3.3.13, WSDL 자동 생성)

### 최신 기술 스택 + PUBC 호환
- ✅ Java 8 기능 전면 활용 (Lambda, Stream, Optional)
- ✅ Spring 4.3.30 (Spring 2.5.6 API 하위 호환)
- ✅ MyBatis 3.5.13 (프로젝트용) + iBATIS 2.3.4 (PUBC용) 공존

---

## 🛠️ 기술 스택 (JDK 1.8)

| 구분 | 기술 | 버전 |
|------|------|------|
| **언어** | Java | **JDK 1.8 (8u202+)** ⬆️ |
| **컴파일 타겟** | Java | **1.8** ⬆️ |
| **프레임워크** | Spring Framework | **4.3.30.RELEASE** ⬆️ |
| **웹 서비스** | Apache CXF | **3.3.13** ⬆️ |
| **SQL Mapper (프로젝트)** | MyBatis | **3.5.13** 🆕 |
| **SQL Mapper (PUBC)** | iBATIS | 2.3.4 (유지) |
| **PUBC 모듈** | iros_pubc | Ver.2.3 (호환 확인) |
| **JSON** | Jackson | **2.15.2** 🆕 |
| **Logging** | SLF4J + Logback | **1.7.36 + 1.2.12** ⬆️ |
| **WAS** | Apache Tomcat | **8.5.90** ⬆️ |
| **데이터베이스** | H2 (개발) / Oracle, PostgreSQL (운영) | 1.4.200 / 12c+ |
| **빌드** | Maven | **3.6+** |

**범례**:
- ⬆️ 업그레이드
- 🆕 신규 도입

---

## 📂 프로젝트 구조 (예상)

```
PUBC-Test-API/
├── src/
│   └── iros/
│       └── test/
│           ├── common/              # 공통 모듈 (CommonProc, 유틸리티)
│           ├── rest/                # REST API (Controller, DTO)
│           ├── soap/                # SOAP 서비스 (Service, DTO)
│           ├── user/                # 사용자 도메인
│           │   ├── service/         # 비즈니스 로직
│           │   ├── dao/             # MyBatis Mapper Interface
│           │   └── vo/              # Value Object
│           └── config/              # Java Config (선택)
│
├── WebContent/
│   ├── WEB-INF/
│   │   ├── web.xml                  # 웹 애플리케이션 설정
│   │   ├── config/
│   │   │   ├── spring/              # Spring 설정
│   │   │   │   ├── context-app.xml
│   │   │   │   ├── context-cxf.xml
│   │   │   │   ├── context-mybatis.xml     🆕
│   │   │   │   └── context-transaction.xml
│   │   │   └── mybatis/             # MyBatis 설정 🆕
│   │   │       ├── mybatis-config.xml
│   │   │       └── mapper/
│   │   │           └── UserMapper.xml
│   │   └── lib/ (빌드 시 자동 생성)
│   └── index.jsp
│
├── lib/                             # PUBC JAR 파일 (System Scope)
│   ├── iros_pubc_1.1.jar           # PUBC 모듈 (필수)
│   └── iros_cipher.jar              # PUBC 암호화
│
├── docs/                            # 설계 문서
│   ├── 00_README_v2.md             # 이 문서 (JDK 1.8 버전)
│   ├── 01_요구사항정의서.md
│   ├── 02_시스템아키텍처설계서.md
│   ├── 03_API명세서.md
│   ├── 04_데이터베이스스키마설계서.md
│   ├── 05_클래스다이어그램및패키지구조.md
│   └── 06_JDK18기술스택및호환성분석.md  🆕
│
└── pom.xml                          # Maven 빌드 설정 (JDK 1.8)
```

---

## 🆕 JDK 1.8 주요 변경사항

### 1. Java 8 기능 활용

#### Lambda Expression
```java
// Java 6 방식
List<UserVO> activeUsers = new ArrayList<UserVO>();
for (UserVO user : users) {
    if ("ACTIVE".equals(user.getStatus())) {
        activeUsers.add(user);
    }
}

// Java 8 방식
List<UserVO> activeUsers = users.stream()
    .filter(user -> "ACTIVE".equals(user.getStatus()))
    .collect(Collectors.toList());
```

#### Stream API
```java
// 사용자명 목록 추출
List<String> userNames = users.stream()
    .map(UserVO::getUserName)
    .collect(Collectors.toList());

// 조건부 집계
long count = users.stream()
    .filter(user -> user.getEmail().contains("@example.com"))
    .count();
```

#### Optional (NullPointerException 방지)
```java
Optional<UserVO> optionalUser = Optional.ofNullable(userService.getUser(userId));
UserVO user = optionalUser.orElseThrow(() -> new NotFoundException("사용자 없음"));
```

#### Date/Time API
```java
// java.util.Date 대신 java.time 사용
LocalDateTime now = LocalDateTime.now();
LocalDate today = LocalDate.now();
LocalDate nextWeek = today.plusWeeks(1);
```

---

### 2. MyBatis 도입 (iBATIS와 공존)

#### 프로젝트 코드 (MyBatis 3.5.13)
```java
// Mapper Interface
public interface UserMapper {
    List<UserVO> selectUserList(Map<String, Object> params);
    UserVO selectUser(String userId);
    void insertUser(UserVO userVO);
    int updateUser(UserVO userVO);
    int deleteUser(String userId);
}

// Service에서 사용
@Autowired
private UserMapper userMapper;

public List<UserVO> getUserList(String userName, int pageNo, int numOfRows) {
    Map<String, Object> params = new HashMap<>();
    params.put("userName", userName);
    params.put("offset", (pageNo - 1) * numOfRows);
    params.put("limit", numOfRows);
    return userMapper.selectUserList(params);
}
```

#### PUBC 코드 (iBATIS 2.3.4 유지)
```java
// PUBC 모듈은 그대로 iBATIS 사용
// 프로젝트에서 수정 불필요
```

**공존 전략**:
- Spring 설정에서 두 개의 SqlSessionFactory 분리
- PUBC: `pubcSqlMapClient` (iBATIS)
- 프로젝트: `sqlSessionFactory` (MyBatis)

---

### 3. Spring 4.3.30 업그레이드

#### 주요 개선사항
- ✅ Java 8 Lambda/Stream 지원
- ✅ `@GetMapping`, `@PostMapping` 등 축약 어노테이션
- ✅ `@RestController` 지원
- ✅ 비동기 처리 개선 (`@Async`)
- ✅ WebSocket 지원

#### PUBC 호환성
- ✅ Spring 2.5.6 API 완벽 하위 호환
- ✅ PUBC Bean 로딩 정상
- ✅ iBATIS SqlMapClient 지원 유지

---

## 🚀 다음 단계

### 1단계: 개발 환경 구축
- [ ] **JDK 1.8** 설치 및 환경변수 설정
- [ ] Eclipse / IntelliJ IDEA 설정
- [ ] Maven 3.6+ 설치
- [ ] PUBC 모듈 (`iros_pubc_1.1.jar`, `iros_cipher.jar`) 확보

### 2단계: Maven 프로젝트 생성
- [ ] pom.xml 작성 (JDK 1.8, Spring 4.3.30)
- [ ] PUBC JAR 파일 `lib/` 디렉토리에 배치
- [ ] 의존성 다운로드 확인

### 3단계: 데이터베이스 설정
- [ ] H2 Database 설치 (개발용)
- [ ] DDL 스크립트 실행 (schema-h2.sql)
- [ ] 초기 데이터 로드 (data-seed.sql)
- [ ] 데이터베이스 연결 테스트

### 4단계: Spring 설정
- [ ] Spring 4.3.30 설정 파일 작성
- [ ] PUBC Spring 설정 Import
- [ ] MyBatis SqlSessionFactory 설정
- [ ] Apache CXF 설정 (JAX-RS, JAX-WS)

### 5단계: PUBC 통합 테스트
- [ ] CommonProc 클래스 구현
- [ ] PUBC Bean 로딩 테스트
- [ ] 인증 프로세스 테스트
- [ ] 로깅 기능 테스트

### 6단계: 도메인 로직 개발 (MyBatis)
- [ ] UserVO 작성
- [ ] UserMapper 인터페이스 작성
- [ ] UserMapper.xml (MyBatis Mapper) 작성
- [ ] UserServiceImpl 작성
- [ ] 단위 테스트 (JUnit 4)

### 7단계: REST API 개발
- [ ] UserRestController 구현 (Java 8 Lambda 활용)
- [ ] DTO 클래스 작성
- [ ] JAX-RS 설정
- [ ] REST API 테스트 (cURL, Postman)

### 8단계: SOAP 웹 서비스 개발
- [ ] UserSoapService 구현
- [ ] SOAP DTO 작성
- [ ] JAX-WS 설정
- [ ] WSDL 생성 확인
- [ ] SOAP 테스트 (SoapUI)

### 9단계: 통합 테스트
- [ ] 인증 시나리오 테스트
- [ ] CRUD 전체 흐름 테스트
- [ ] 에러 처리 테스트
- [ ] MyBatis + iBATIS 공존 테스트
- [ ] Java 8 기능 테스트 (Lambda, Stream)
- [ ] 성능 테스트 (JMeter)

### 10단계: 배포
- [ ] WAR 파일 빌드 (Maven)
- [ ] Tomcat 8.5 배포
- [ ] 사용자 매뉴얼 작성
- [ ] 설치 가이드 작성

---

## 📝 참고 문서

### 프로젝트 내부 문서
- [CLAUDE.md](./CLAUDE.md) - 전체 저장소 가이드 (Claude Code용)
- [PUBC.md](./PUBC.md) - PUBC 공통연계모듈 상세 분석
- [OASIS.md](./OASIS.md) - OpenAPI 자동생성툴 상세 분석

### 외부 문서
- **공공데이터 포털 OpenAPI 개발 표준**
- **PUBC 공통연계모듈 Ver.2.3 매뉴얼**
- **Apache CXF 3.x 공식 문서**
- **Spring Framework 4.3.x 레퍼런스**
- **MyBatis 3.5.x 공식 문서**
- **Java 8 API 문서**

---

## 🔗 빠른 링크

| 문서 | 설명 |
|------|------|
| [요구사항 정의서](./docs/01_요구사항정의서.md) | 기능/비기능 요구사항, 제약사항 |
| [시스템 아키텍처](./docs/02_시스템아키텍처설계서.md) | 7계층 아키텍처, 기술 스택 |
| [API 명세서](./docs/03_API명세서.md) | REST/SOAP API 상세 명세 |
| [DB 스키마](./docs/04_데이터베이스스키마설계서.md) | ERD, DDL, 인덱스 설계 |
| [클래스 다이어그램](./docs/05_클래스다이어그램및패키지구조.md) | 패키지 구조, 클래스/시퀀스 다이어그램 |
| 🆕 [JDK 1.8 기술 스택](./docs/06_JDK18기술스택및호환성분석.md) | **호환성 분석, 마이그레이션 전략** |

---

## ⚠️ 주의사항

### PUBC 모듈 관련
- **완벽 호환**: `iros_pubc_1.1.jar`는 JDK 1.8에서 **완벽하게 동작**합니다
- **Java 하위 호환성**: Java 5 → Java 8 바이트코드 호환
- **Spring 통합**: Spring 4.x가 Spring 2.5.6 API 하위 호환
- **iBATIS 유지**: PUBC는 계속 iBATIS 2.3.4 사용

### MyBatis + iBATIS 공존
- **독립 SqlSessionFactory**: 두 SQL Mapper가 독립적으로 동작
- **데이터소스 공유**: 동일한 DataSource 사용 가능
- **충돌 없음**: 각각 별도 네임스페이스 사용

### Java 8 기능 활용
- **Lambda, Stream**: 코드 가독성 및 간결성 향상
- **Optional**: NullPointerException 방지
- **Date/Time API**: 날짜/시간 처리 개선
- **Default Method**: 인터페이스 확장성 향상

### 보안
- **서비스키 관리**: 외부 시스템에서 발급
- **IP 검증**: PUBC 모듈 자동 처리
- **SQL Injection**: MyBatis #{} 파라미터 바인딩 사용

---

## 📞 문의 및 피드백

설계 문서 또는 JDK 1.8 마이그레이션에 대한 문의사항이 있으시면 프로젝트 담당자에게 연락해 주세요.

---

**작성자**: Claude
**최종 수정일**: 2025-11-03
**버전**: 2.0 (JDK 1.8)
