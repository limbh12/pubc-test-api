# Copilot 지침: PUBC 테스트 API 프로젝트

## 프로젝트 개요

한국 정부 공개 API 표준인 **PUBC(공통연계모듈)** 기반 문화시설 정보 조회 시스템입니다.

**도메인**: 읽기 전용 문화시설 정보 API (박물관, 도서관, 미술관)  
**아키텍처**: PUBC 인증/로깅 계층 + 7계층 아키텍처  
**기술 스택**: JDK 1.8, Spring 4.3.30, Apache CXF 3.3.13, MyBatis 3.5.13, JBoss EAP 7.4  
**특징**: Mock 데이터로 DB 없이 실행 가능, JAX-RS REST API 제공

## 핵심 아키텍처 개념

### 1. PUBC 통합 패턴 (필수)

**모든 컨트롤러는 반드시 PUBC 인증/로깅을 거쳐야 합니다:**

```java
// 현재 구현: MockCommonProc 사용 (DB 없이 테스트 가능)
@Autowired
private MockCommonProc mockCommonProc;

public Response getList(@Context HttpServletRequest request) {
    // 1. 인증: serviceKey 파라미터 검증
    UserCrtfcVO userCrtfc = mockCommonProc.authServiceCall(request);
    
    try {
        // 2. 비즈니스 로직
        List<CultureFacilityVO> result = cultureFacilityService.getFacilityList(...);
        return Response.ok(result).build();
    } finally {
        // 3. 로깅: API 호출 기록 (항상 실행)
        mockCommonProc.insertPubcLog(request, userCrtfc, result);
    }
}
```

**유효한 테스트 서비스키** (MockCommonProc.java 참조):
- `TEST_KEY_001`, `TEST_KEY_002`, `DEMO_KEY`, `DEV_KEY`

**실제 운영 환경**에서는 `MockCommonProc` 대신 실제 `CommonProc`(PUBC 모듈 연동)을 사용해야 합니다.

### 2. 읽기 전용 API 설계 (강제)

**절대 생성하지 말 것**: POST/PUT/DELETE 엔드포인트, INSERT/UPDATE/DELETE SQL

**구현된 엔드포인트** (`CultureFacilityRestController.java`):
```java
@GET @Path("")                    // 목록 조회
@GET @Path("/{facilityId}")       // 상세 조회
@GET @Path("/types")              // 시설 유형 목록
```

**Mapper 메서드** (`CultureFacilityMapper.java`):
```java
List<CultureFacilityVO> selectFacilityList(Map<String, Object> params);
CultureFacilityVO selectFacilityById(String facilityId);
int selectTotalCount(Map<String, Object> params);
```

### 3. Mock 데이터 개발 전략 (기본)

**현재 구현 방식**: 데이터베이스 없이 인메모리 Mock 데이터 사용

**Mock Mapper** (`CultureFacilityMockMapper.java`):
```java
@Repository("cultureFacilityMapper")
public class CultureFacilityMockMapper implements CultureFacilityMapper {
    private static final List<CultureFacilityVO> MOCK_DATA = new ArrayList<>();
    
    static {
        // 정적 초기화: 50개 샘플 시설 데이터
        MOCK_DATA.add(createFacility("FAC001", "국립중앙박물관", "박물관", "11", ...));
        MOCK_DATA.add(createFacility("FAC002", "서울시립미술관", "미술관", "11", ...));
        // ...
    }
    
    @Override
    public List<CultureFacilityVO> selectFacilityList(Map<String, Object> params) {
        return MOCK_DATA.stream()
            .filter(f -> matchesCriteria(f, params))  // Java 8 Stream 활용
            .collect(Collectors.toList());
    }
}
```

**장점**: DB 설치 불필요, 빠른 개발/테스트, 일관된 데모 데이터

## 개발 워크플로우

### 빌드 및 실행

```bash
# 1. 프로젝트 전용 JDK/JBoss 설치 (최초 1회)
./scripts/install-jdk.sh
./scripts/install-jboss.sh

# 2. Maven 빌드
mvn clean package

# 3. WAR 파일 배포
cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 4. JBoss 시작
./bin/jboss-start.sh

# 5. API 테스트
curl "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001"
```

**중요**: 프로젝트는 `env/` 디렉토리에 독립적인 JDK 1.8과 JBoss를 설치하여 시스템 환경과 완전히 분리됩니다.

### 프로젝트 구조 (실제 구현)

```
src/main/java/iros/test/
├── facility/                           # 문화시설 도메인 (핵심)
│   ├── controller/
│   │   └── CultureFacilityRestController.java  # JAX-RS 컨트롤러
│   ├── service/
│   │   ├── CultureFacilityService.java      # 서비스 인터페이스
│   │   └── CultureFacilityServiceImpl.java  # 서비스 구현
│   ├── dao/
│   │   └── CultureFacilityMapper.java       # MyBatis Mapper 인터페이스
│   ├── mock/
│   │   └── CultureFacilityMockMapper.java   # Mock 데이터 구현 ⭐
│   └── domain/
│       └── CultureFacilityVO.java           # Value Object
│
├── user/                               # PUBC 통합 (Mock)
│   ├── domain/
│   │   └── UserCrtfcVO.java                 # 인증 정보
│   └── mock/
│       ├── MockCommonProc.java              # PUBC Mock 구현 ⭐
│       └── MockUserCrtfcVO.java             # Mock 인증 객체
│
├── common/                             # 공통 모듈
│   └── interceptor/
│       └── PubcAuthInterceptor.java         # 인증 인터셉터
│
src/main/resources/
├── config/
│   ├── application.properties               # 설정
│   └── spring/
│       ├── root-context.xml                 # 루트 컨텍스트
│       └── servlet-context.xml              # 웹 컨텍스트
└── mybatis/
    └── mapper/                              # MyBatis XML (향후)
```

### Java 8 기능 활용

**Stream API** (필터링/변환):
```java
// CultureFacilityMockMapper.java 참조
facilities.stream()
    .filter(f -> facilityType == null || facilityType.equals(f.getFacilityType()))
    .filter(f -> regionCode == null || regionCode.equals(f.getRegionCode()))
    .skip((pageNum - 1) * pageSize)
    .limit(pageSize)
    .collect(Collectors.toList());
```

**Lambda Expression** (간결한 콜백):
```java
facilities.forEach(f -> logger.debug("Facility: {}", f.getFacilityName()));
```

**Optional** (null 안전):
```java
Optional.ofNullable(mapper.selectFacilityById(id))
    .orElseThrow(() -> new NotFoundException("시설을 찾을 수 없습니다"));
```

## REST API 패턴

### 엔드포인트 규칙

**기본 URL**: `http://localhost:8080/pubc-test-api/api/facilities`

**구현된 엔드포인트**:
```java
@Path("/facilities")
public class CultureFacilityRestController {
    
    @GET @Path("")
    // GET /api/facilities?serviceKey={key}&facilityType={type}&pageNum=1&pageSize=10
    public Response getList(...) { }
    
    @GET @Path("/{facilityId}")
    // GET /api/facilities/FAC001?serviceKey={key}
    public Response getById(@PathParam("facilityId") String facilityId) { }
    
    @GET @Path("/types")
    // GET /api/facilities/types?serviceKey={key}
    public Response getTypes(...) { }
}
```

### 응답 형식

**성공 응답** (200 OK):
```json
{
  "code": "000",
  "message": "정상 처리되었습니다",
  "totalCount": 50,
  "items": [
    {
      "facilityId": "FAC001",
      "facilityName": "국립중앙박물관",
      "facilityType": "박물관",
      "regionCode": "11",
      "address": "서울특별시 용산구 서빙고로 137",
      "phone": "02-2077-9000"
    }
  ]
}
```

**에러 응답** (400/500):
```json
{
  "code": "400",
  "message": "유효하지 않은 서비스 키입니다",
  "timestamp": "2025-11-06T10:30:00"
}
```

### 필수 파라미터

모든 엔드포인트는 **serviceKey** 필수:
- Query Parameter: `?serviceKey=TEST_KEY_001`
- Header: `X-Service-Key: TEST_KEY_001`

유효한 테스트 키: `TEST_KEY_001`, `TEST_KEY_002`, `DEMO_KEY`, `DEV_KEY`

## 데이터 모델

### 문화시설 VO (CultureFacilityVO)

```java
public class CultureFacilityVO {
    private String facilityId;        // 시설 ID (PK)
    private String facilityName;      // 시설명
    private String facilityType;      // 시설 유형 (박물관/미술관/도서관)
    private String regionCode;        // 지역 코드 (11:서울, 26:부산, 41:경기)
    private String address;           // 주소
    private String phone;             // 전화번호
    private Double latitude;          // 위도
    private Double longitude;         // 경도
    private String openTime;          // 운영 시간
    private String homepage;          // 홈페이지
    private String manageAgency;      // 관리 기관
    private LocalDateTime updateDate; // 수정일
}
```

### 인증 정보 VO (UserCrtfcVO)

```java
public class UserCrtfcVO {
    private String serviceKey;        // 서비스 키
    private String userName;          // 사용자명
    private String userIp;            // IP 주소
    private LocalDateTime authTime;   // 인증 시각
}
```

## 주요 설정 파일

- `docs/00_기술스택_JDK18.md`: 정식 의존성 버전
- `docs/02_시스템아키텍처설계서.md`: 7계층 아키텍처 상세
- `docs/03_API명세서.md`: 완전한 REST/SOAP API 명세
- `CLAUDE.md`: AI 전용 구현 패턴 및 mock 코드 예제 (2500+ 라인)
- `PUBC.md`: PUBC 모듈 내부 분석
- `pom.xml`: Maven 의존성 (Spring 4.3.30, CXF 3.3.13, MyBatis 3.5.13)

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

---

## 실제 운영 환경 구성

### 1. PUBC 모듈 실제 연동

**Mock 대신 실제 PUBC 사용 시:**

#### CommonProc 구현 (실제 PUBC 연동)

```java
package iros.test.common;

import iros.pubc.UserCrtfcProcessService;
import iros.pubc.UseSttusService;
import iros.pubc.vo.UserCrtfcVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 실제 PUBC 모듈 연동 클래스
 * @Profile("production") - 운영 환경에서만 활성화
 */
@Component
@Profile("production")
public class CommonProc {

    @Autowired
    private UserCrtfcProcessService userCrtfcProcessService;  // PUBC 인증 서비스
    
    @Autowired
    private UseSttusService useSttusService;  // PUBC 사용현황 로깅 서비스

    /**
     * PUBC 인증 처리
     * @param request HTTP 요청
     * @return 사용자 인증 정보
     */
    public UserCrtfcVO authServiceCall(HttpServletRequest request) {
        try {
            // PUBC 모듈의 인증 서비스 호출
            UserCrtfcVO userCrtfc = userCrtfcProcessService.userCrtfcProcess(request);
            
            // 인증 결과 검증
            String troblTyCode = userCrtfc.getTroblTyCode();
            
            if ("00".equals(troblTyCode)) {
                logger.info("PUBC 인증 성공 - ServiceKey: {}", userCrtfc.getServiceKey());
            } else {
                logger.warn("PUBC 인증 실패 - Code: {}, Message: {}", 
                    troblTyCode, userCrtfc.getTroblCn());
            }
            
            return userCrtfc;
            
        } catch (Exception e) {
            logger.error("PUBC 인증 오류", e);
            throw new RuntimeException("인증 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * PUBC 사용현황 로깅
     * @param userCrtfc 사용자 인증 정보
     */
    public void useSttusServiceCall(UserCrtfcVO userCrtfc) {
        try {
            // PUBC 모듈의 사용현황 로깅 서비스 호출
            useSttusService.insertUseSttus(userCrtfc);
            logger.debug("PUBC 로깅 완료 - ServiceKey: {}", userCrtfc.getServiceKey());
            
        } catch (Exception e) {
            // 로깅 실패는 비즈니스 로직에 영향을 주지 않음
            logger.error("PUBC 로깅 오류 (무시)", e);
        }
    }
}
```

#### Spring 설정 (PUBC 활성화)

**root-context.xml** - PUBC 모듈 Import:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd">

    <!-- 실제 PUBC 모듈 설정 Import (iros_pubc_1.1.jar에서 제공) -->
    <import resource="classpath:iros/pubc/spring/context-common-pubc.xml"/>
    <import resource="classpath:iros/pubc/spring/context-datasource-pubc.xml"/>
    <import resource="classpath:iros/pubc/spring/context-sqlMap-pubc.xml"/>
    
    <!-- 애플리케이션 Bean 스캔 -->
    <context:component-scan base-package="iros.test"/>
</beans>
```

#### PUBC 에러 코드 처리

```java
// 컨트롤러에서 PUBC 에러 처리
public Response getList(@Context HttpServletRequest request) {
    UserCrtfcVO userCrtfc = commonProc.authServiceCall(request);
    
    // PUBC 표준 에러 코드 체크
    String errorCode = userCrtfc.getTroblTyCode();
    
    if (!"00".equals(errorCode)) {
        // 인증 실패 처리
        Map<String, Object> error = new HashMap<>();
        error.put("code", errorCode);
        error.put("message", getPubcErrorMessage(errorCode));
        return Response.status(getHttpStatus(errorCode)).entity(error).build();
    }
    
    // ... 비즈니스 로직
}

private String getPubcErrorMessage(String code) {
    switch (code) {
        case "11": return "필수 파라미터 누락";
        case "30": return "등록되지 않은 서비스키";
        case "31": return "기한 만료된 서비스키";
        case "32": return "허가되지 않은 IP";
        case "99": return "시스템 오류";
        default: return "알 수 없는 오류";
    }
}
```

### 2. SOAP 웹 서비스 구현

**JAX-WS 기반 SOAP 서비스:**

#### SOAP 서비스 인터페이스

```java
package iros.test.facility.soap;

import iros.test.facility.domain.CultureFacilityVO;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * 문화시설 SOAP 웹 서비스
 */
@WebService(name = "CultureFacilityService", 
            targetNamespace = "http://soap.facility.test.iros/")
public interface CultureFacilitySoapService {

    /**
     * 문화시설 목록 조회
     */
    @WebMethod
    CultureFacilityListResponse getFacilityList(
        @WebParam(name = "serviceKey") String serviceKey,
        @WebParam(name = "facilityType") String facilityType,
        @WebParam(name = "regionCode") String regionCode,
        @WebParam(name = "pageNum") Integer pageNum,
        @WebParam(name = "pageSize") Integer pageSize
    );

    /**
     * 문화시설 상세 조회
     */
    @WebMethod
    CultureFacilityResponse getFacilityById(
        @WebParam(name = "serviceKey") String serviceKey,
        @WebParam(name = "facilityId") String facilityId
    );
}
```

#### SOAP 서비스 구현

```java
package iros.test.facility.soap.impl;

import iros.test.facility.service.CultureFacilityService;
import iros.test.facility.soap.CultureFacilitySoapService;
import iros.test.user.mock.MockCommonProc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * 문화시설 SOAP 서비스 구현
 */
@Service
@WebService(
    serviceName = "CultureFacilityService",
    portName = "CultureFacilityPort",
    targetNamespace = "http://soap.facility.test.iros/",
    endpointInterface = "iros.test.facility.soap.CultureFacilitySoapService"
)
public class CultureFacilitySoapServiceImpl implements CultureFacilitySoapService {

    @Autowired
    private CultureFacilityService cultureFacilityService;
    
    @Autowired
    private MockCommonProc mockCommonProc;
    
    @Resource
    private WebServiceContext wsContext;

    @Override
    public CultureFacilityListResponse getFacilityList(
            String serviceKey, String facilityType, String regionCode,
            Integer pageNum, Integer pageSize) {
        
        HttpServletRequest request = getHttpRequest();
        UserCrtfcVO userCrtfc = mockCommonProc.authServiceCall(request);
        
        CultureFacilityListResponse response = new CultureFacilityListResponse();
        
        try {
            if (!"00".equals(userCrtfc.getTroblTyCode())) {
                response.setCode(userCrtfc.getTroblTyCode());
                response.setMessage(userCrtfc.getTroblCn());
                return response;
            }
            
            List<CultureFacilityVO> facilities = 
                cultureFacilityService.getFacilityList(facilityType, regionCode, 
                    pageNum != null ? pageNum : 1, 
                    pageSize != null ? pageSize : 10);
            
            response.setCode("000");
            response.setMessage("정상 처리되었습니다");
            response.setFacilities(facilities);
            response.setTotalCount(facilities.size());
            
        } finally {
            mockCommonProc.insertPubcLog(request, userCrtfc, response);
        }
        
        return response;
    }
    
    private HttpServletRequest getHttpRequest() {
        MessageContext mc = wsContext.getMessageContext();
        return (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
    }
}
```

#### CXF SOAP 설정

**servlet-context.xml**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:jaxws="http://cxf.apache.org/jaxws"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://cxf.apache.org/jaxws
           http://cxf.apache.org/schemas/jaxws.xsd">

    <!-- CXF SOAP 서비스 등록 -->
    <jaxws:endpoint 
        id="cultureFacilitySoapService"
        implementor="#cultureFacilitySoapServiceImpl"
        address="/CultureFacilityService">
        
        <!-- WSDL 자동 생성 활성화 -->
        <jaxws:properties>
            <entry key="publishedEndpointUrl" 
                   value="http://localhost:8080/pubc-test-api/services/CultureFacilityService"/>
        </jaxws:properties>
    </jaxws:endpoint>
</beans>
```

#### SOAP 테스트

**WSDL 확인**:
```
http://localhost:8080/pubc-test-api/services/CultureFacilityService?wsdl
```

**SoapUI 요청 예시**:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                  xmlns:soap="http://soap.facility.test.iros/">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getFacilityList>
         <serviceKey>TEST_KEY_001</serviceKey>
         <facilityType>박물관</facilityType>
         <regionCode>11</regionCode>
         <pageNum>1</pageNum>
         <pageSize>10</pageSize>
      </soap:getFacilityList>
   </soapenv:Body>
</soapenv:Envelope>
```

### 3. CUBRID 데이터베이스 연동

**운영 환경에서 실제 CUBRID DB 사용:**

#### CUBRID JDBC 드라이버 추가

**pom.xml**:

```xml
<!-- CUBRID JDBC Driver -->
<dependency>
    <groupId>cubrid</groupId>
    <artifactId>cubrid-jdbc</artifactId>
    <version>11.2.0.0005</version>
</dependency>
```

#### DataSource 설정

**root-context.xml** (운영 프로파일):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd">

    <!-- CUBRID DataSource (HikariCP 사용) -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="cubrid.jdbc.driver.CUBRIDDriver"/>
        <property name="jdbcUrl" value="jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8"/>
        <property name="username" value="dba"/>
        <property name="password" value="${db.password}"/>
        
        <!-- Connection Pool 설정 -->
        <property name="maximumPoolSize" value="20"/>
        <property name="minimumIdle" value="5"/>
        <property name="connectionTimeout" value="30000"/>
        <property name="idleTimeout" value="600000"/>
        <property name="maxLifetime" value="1800000"/>
    </bean>

    <!-- MyBatis SqlSessionFactory -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <property name="configLocation" value="classpath:config/mybatis/mybatis-config.xml"/>
        <property name="mapperLocations" value="classpath:mybatis/mapper/**/*.xml"/>
    </bean>

    <!-- MyBatis Mapper 스캔 -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="iros.test.**.dao"/>
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
    </bean>
    
    <!-- Transaction Manager -->
    <bean id="transactionManager" 
          class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>
</beans>
```

#### MyBatis Mapper XML

**CultureFacilityMapper.xml**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="iros.test.facility.dao.CultureFacilityMapper">

    <!-- Result Map -->
    <resultMap id="facilityResultMap" type="iros.test.facility.domain.CultureFacilityVO">
        <id property="facilityId" column="FACILITY_ID"/>
        <result property="facilityName" column="FACILITY_NAME"/>
        <result property="facilityType" column="FACILITY_TYPE"/>
        <result property="regionCode" column="REGION_CODE"/>
        <result property="address" column="ADDRESS"/>
        <result property="phone" column="PHONE"/>
        <result property="latitude" column="LATITUDE"/>
        <result property="longitude" column="LONGITUDE"/>
        <result property="openTime" column="OPEN_TIME"/>
        <result property="homepage" column="HOMEPAGE"/>
        <result property="manageAgency" column="MANAGE_AGENCY"/>
        <result property="updateDate" column="UPDATE_DATE"/>
    </resultMap>

    <!-- 목록 조회 -->
    <select id="selectFacilityList" parameterType="map" resultMap="facilityResultMap">
        SELECT 
            FACILITY_ID, FACILITY_NAME, FACILITY_TYPE, REGION_CODE,
            ADDRESS, PHONE, LATITUDE, LONGITUDE,
            OPEN_TIME, HOMEPAGE, MANAGE_AGENCY, UPDATE_DATE
        FROM TB_CULTURE_FACILITY
        WHERE 1=1
        <if test="facilityType != null and facilityType != ''">
            AND FACILITY_TYPE = #{facilityType}
        </if>
        <if test="regionCode != null and regionCode != ''">
            AND REGION_CODE = #{regionCode}
        </if>
        <if test="facilityName != null and facilityName != ''">
            AND FACILITY_NAME LIKE CONCAT('%', #{facilityName}, '%')
        </if>
        ORDER BY UPDATE_DATE DESC
        LIMIT #{pageSize} OFFSET #{offset}
    </select>

    <!-- 상세 조회 -->
    <select id="selectFacilityById" parameterType="string" resultMap="facilityResultMap">
        SELECT 
            FACILITY_ID, FACILITY_NAME, FACILITY_TYPE, REGION_CODE,
            ADDRESS, PHONE, LATITUDE, LONGITUDE,
            OPEN_TIME, HOMEPAGE, MANAGE_AGENCY, UPDATE_DATE
        FROM TB_CULTURE_FACILITY
        WHERE FACILITY_ID = #{facilityId}
    </select>

    <!-- 총 건수 조회 -->
    <select id="selectTotalCount" parameterType="map" resultType="int">
        SELECT COUNT(*)
        FROM TB_CULTURE_FACILITY
        WHERE 1=1
        <if test="facilityType != null and facilityType != ''">
            AND FACILITY_TYPE = #{facilityType}
        </if>
        <if test="regionCode != null and regionCode != ''">
            AND REGION_CODE = #{regionCode}
        </if>
        <if test="facilityName != null and facilityName != ''">
            AND FACILITY_NAME LIKE CONCAT('%', #{facilityName}, '%')
        </if>
    </select>
</mapper>
```

#### 환경별 프로파일 전환

**application.properties**:

```properties
# 개발 환경 (Mock 사용)
spring.profiles.active=mock

# 운영 환경 (실제 PUBC + CUBRID 사용)
# spring.profiles.active=production

# CUBRID 연결 정보
db.driver=cubrid.jdbc.driver.CUBRIDDriver
db.url=jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8
db.username=dba
db.password=YOUR_PASSWORD_HERE

# Connection Pool
db.pool.max=20
db.pool.min=5
```

#### 프로파일별 Bean 활성화

```java
// Mock 환경에서만 활성화
@Component
@Profile("mock")
public class CultureFacilityMockMapper implements CultureFacilityMapper {
    // Mock 구현
}

// 운영 환경에서만 활성화
@Component
@Profile("production")
public class CommonProc {
    // 실제 PUBC 연동
}
```

#### 배포 시 프로파일 지정

```bash
# JBoss 시작 시 프로파일 지정
export JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=production"
./bin/jboss-start.sh

# 또는 WAR 배포 시 jboss-web.xml에 지정
<jboss-web>
    <context-root>/pubc-test-api</context-root>
    <context-param>
        <param-name>spring.profiles.active</param-name>
        <param-value>production</param-value>
    </context-param>
</jboss-web>
```
