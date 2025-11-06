# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

**PUBC 연동 테스트 프로젝트 (PUBC-Test-API)**는 공공데이터 포털의 PUBC 공통연계모듈을 활용한 OpenAPI 테스트 프로젝트입니다.

- **목적**: PUBC 모듈(`iros_pubc_1.1.jar`)을 사용한 서비스키 인증 및 사용 현황 로깅 기능 구현
- **API 유형**: 조회 전용 (Read-Only) - 문화시설 정보 조회
- **제공 방식**: REST API (JAX-RS) 및 SOAP 웹 서비스 (JAX-WS)

---

## 기술 스택 (JDK 1.8)

| 구분 | 기술 | 버전 |
|------|------|------|
| **언어** | Java | JDK 1.8 (8u202+) |
| **프레임워크** | Spring Framework | 4.3.30.RELEASE |
| **웹 서비스** | Apache CXF | 3.3.13 |
| **SQL Mapper (프로젝트)** | MyBatis | 3.5.13 |
| **SQL Mapper (PUBC)** | iBATIS | 2.3.4 |
| **PUBC 모듈** | iros_pubc | Ver.2.3 (1.1) |
| **JSON** | Jackson | 2.15.2 |
| **Logging** | SLF4J + Logback | 1.7.36 + 1.2.12 |
| **WAS** | JBoss EAP | 7.4+ |
| **빌드** | Maven | 3.6+ |
| **데이터베이스** | Mock (개발) / CUBRID (운영) | - / 11.2+ |

### 중요: Java 8 기능 활용
- **Lambda Expression**: 코드 간결성 향상
- **Stream API**: 컬렉션 처리
- **Optional**: NullPointerException 방지
- **java.time**: 날짜/시간 API

---

## 아키텍처 특징

### 7계층 아키텍처
```
클라이언트 (REST/SOAP)
    ↓
프레젠테이션 계층 (CXF JAX-RS/JAX-WS)
    ↓
인증/로깅 계층 (PUBC Integration - CommonProc)
    ↓
비즈니스 로직 계층 (Service)
    ↓
데이터 접근 계층 (MyBatis Mapper - SELECT만)
    ↓
데이터베이스 (TB_CULTURE_FACILITY + PUBC 테이블)
```

### 조회 전용 API
- **도메인**: 문화시설(CultureFacility) 정보
- **제공 메서드**: `getFacilityList()`, `getFacility()`, `getTotalCount()`
- **제공하지 않음**: POST, PUT, DELETE 엔드포인트

### PUBC 모듈 통합
- **인증**: `UserCrtfcProcessService.userCrtfcProcess()` - 서비스키 검증
- **로깅**: `UseSttusService.insertUseSttus()` - 사용 현황 자동 기록
- **통합 클래스**: `CommonProc.java`
  - `authServiceCall()`: 인증 처리
  - `useSttusServiceCall()`: 로깅 처리

### MyBatis + iBATIS 공존 구조
- **프로젝트 코드**: MyBatis 3.5.13 (Mapper Interface)
- **PUBC 모듈**: iBATIS 2.3.4 (SqlMapClient)
- **데이터소스**: 동일 DataSource 공유
- **충돌 방지**: 별도 네임스페이스 사용

---

## 패키지 구조

```
src/iros/test/
├── common/                    # 공통 모듈
│   ├── CommonProc.java       # PUBC 통합 클래스 (필수)
│   ├── Constants.java
│   ├── exception/            # 예외 클래스
│   └── util/                 # JSON, XML, 날짜 유틸리티
│
├── rest/                     # REST API (조회 전용)
│   ├── controller/
│   │   └── CultureFacilityRestController.java
│   └── dto/
│       ├── ApiResponse.java
│       └── ErrorResponse.java
│
├── soap/                     # SOAP 웹 서비스 (조회 전용)
│   ├── service/
│   │   └── CultureFacilitySoapService.java
│   └── dto/
│       └── FacilityListResponse.java
│
├── facility/                 # 문화시설 도메인 (조회 전용)
│   ├── service/
│   │   ├── CultureFacilityService.java        # 인터페이스
│   │   └── CultureFacilityServiceImpl.java    # 구현체
│   ├── dao/
│   │   └── CultureFacilityMapper.java         # MyBatis Mapper (SELECT만)
│   └── vo/
│       └── CultureFacilityVO.java
│
└── config/                   # 설정 클래스
```

---

## 주요 API 엔드포인트

### REST API
- **GET** `/api/cultureFacilities` - 문화시설 목록 조회
  - 필수: `serviceKey`
  - 선택: `facilityType` (박물관/미술관/도서관), `regionCode` (11:서울, 26:부산), `facilityName`, `pageNo`, `numOfRows`
- **GET** `/api/cultureFacilities/{facilityId}` - 문화시설 상세 조회

### SOAP 웹 서비스
- **WSDL**: `http://host:port/services/CultureFacilityService?wsdl`
- **메서드**:
  - `getCultureFacilityList()` - 목록 조회
  - `getCultureFacility()` - 상세 조회

---

## PUBC 인증 및 로깅 프로세스

### 인증 흐름
1. HTTP 요청에서 `serviceKey` 추출 (Query Parameter 또는 Header)
2. `CommonProc.authServiceCall(request)` 호출
3. PUBC `UserCrtfcProcessService.userCrtfcProcess()` 실행
4. 인증 결과 `UserCrtfcVO` 반환
   - `troblTyCode = "00"`: 정상
   - `troblTyCode = "30"`: 서비스키 미등록
   - `troblTyCode = "32"`: IP 불일치
5. 인증 성공 시 비즈니스 로직 실행, 실패 시 에러 응답

### 로깅 흐름
1. try-finally 블록의 finally에서 `commonProc.useSttusServiceCall(userCrtfcVO)` 호출
2. PUBC `UseSttusService.insertUseSttus()` 실행
3. `TN_PUBC_USE_STTUS_INFO` 테이블에 사용 현황 기록

---

## 데이터베이스 스키마

### Mock 데이터 사용 (권장)

**이 프로젝트는 실제 데이터베이스 연결 없이 Mock 데이터로 동작하도록 설계되었습니다.**

#### 장점
- ✅ 데이터베이스 설치 및 설정 불필요
- ✅ 빠른 개발 및 테스트 환경 구축
- ✅ 데모 및 프로토타이핑에 적합
- ✅ 네트워크 의존성 제거
- ✅ 일관된 테스트 데이터 제공

### Application Tables (Mock 데이터 구조)
- **TB_CULTURE_FACILITY** (문화시설 정보 - 조회 전용)
  - FACILITY_ID (PK)
  - FACILITY_NAME, FACILITY_TYPE, ADDRESS, PHONE
  - LATITUDE, LONGITUDE, OPEN_TIME, HOMEPAGE
  - REGION_CODE, MANAGE_AGENCY, UPDATE_DATE

### PUBC Tables (Mock 데이터 구조)
- **TN_PUBC_USER_CRTFC** (서비스키 인증)
- **TN_PUBC_USE_STTUS_INFO** (사용 현황 로깅)

---

## 개발 가이드

### Mock 데이터 구현 방식

#### 1. Mock Mapper 구현 (MyBatis 대체)

**CultureFacilityMockMapper.java** (Mock 데이터 제공):
```java
package iros.test.facility.dao;

import iros.test.facility.vo.CultureFacilityVO;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository("cultureFacilityMapper")
public class CultureFacilityMockMapper implements CultureFacilityMapper {

    // Mock 데이터 저장소 (인메모리)
    private static final List<CultureFacilityVO> MOCK_DATA = new ArrayList<>();

    // 정적 초기화 블록 (Mock 데이터 생성)
    static {
        MOCK_DATA.add(createFacility("F001", "국립중앙박물관", "박물관",
            "서울특별시 용산구 서빙고로 137", "02-2077-9000",
            37.5240, 126.9803, "10:00-18:00 (월요일 휴관)",
            "https://www.museum.go.kr", "11", "문화체육관광부"));

        MOCK_DATA.add(createFacility("F002", "국립현대미술관", "미술관",
            "경기도 과천시 광명로 313", "02-2188-6000",
            37.4354, 127.0146, "10:00-18:00 (월요일 휴관)",
            "https://www.mmca.go.kr", "41", "문화체육관광부"));

        MOCK_DATA.add(createFacility("F003", "국립중앙도서관", "도서관",
            "서울특별시 서초구 반포대로 201", "02-535-4142",
            37.5034, 127.0376, "09:00-21:00 (연중무휴)",
            "https://www.nl.go.kr", "11", "문화체육관광부"));

        MOCK_DATA.add(createFacility("F004", "부산시립미술관", "미술관",
            "부산광역시 해운대구 APEC로 58", "051-744-2602",
            35.1689, 129.1316, "10:00-18:00 (월요일 휴관)",
            "https://art.busan.go.kr", "26", "부산광역시"));

        MOCK_DATA.add(createFacility("F005", "서울역사박물관", "박물관",
            "서울특별시 종로구 새문안로 55", "02-724-0274",
            37.5703, 126.9691, "09:00-20:00 (월요일 휴관)",
            "https://museum.seoul.go.kr", "11", "서울특별시"));

        MOCK_DATA.add(createFacility("F006", "경기도립미술관", "미술관",
            "경기도 안산시 단원구 동산로 268", "031-481-7000",
            37.3261, 126.8310, "10:00-18:00 (월요일 휴관)",
            "https://www.gmoma.ggcf.kr", "41", "경기도"));

        MOCK_DATA.add(createFacility("F007", "대구시립중앙도서관", "도서관",
            "대구광역시 중구 동성로 2가 141", "053-231-2000",
            35.8697, 128.5938, "09:00-22:00 (월요일 휴관)",
            "https://jungang.daegu.go.kr", "27", "대구광역시"));

        MOCK_DATA.add(createFacility("F008", "광주시립미술관", "미술관",
            "광주광역시 북구 하서로 52", "062-613-7100",
            35.1817, 126.9012, "10:00-18:00 (월요일 휴관)",
            "https://artmuse.gwangju.go.kr", "29", "광주광역시"));
    }

    private static CultureFacilityVO createFacility(String id, String name, String type,
                                                     String address, String phone,
                                                     double lat, double lng, String openTime,
                                                     String homepage, String regionCode,
                                                     String agency) {
        CultureFacilityVO vo = new CultureFacilityVO();
        vo.setFacilityId(id);
        vo.setFacilityName(name);
        vo.setFacilityType(type);
        vo.setAddress(address);
        vo.setPhone(phone);
        vo.setLatitude(lat);
        vo.setLongitude(lng);
        vo.setOpenTime(openTime);
        vo.setHomepage(homepage);
        vo.setRegionCode(regionCode);
        vo.setManageAgency(agency);
        vo.setUpdateDate(LocalDate.now().toString());
        return vo;
    }

    @Override
    public List<CultureFacilityVO> selectFacilityList(Map<String, Object> params) {
        String facilityType = (String) params.get("facilityType");
        String regionCode = (String) params.get("regionCode");
        String facilityName = (String) params.get("facilityName");
        Integer offset = (Integer) params.get("offset");
        Integer limit = (Integer) params.get("limit");

        // Java 8 Stream API를 사용한 필터링
        return MOCK_DATA.stream()
            .filter(f -> facilityType == null || facilityType.isEmpty()
                      || f.getFacilityType().equals(facilityType))
            .filter(f -> regionCode == null || regionCode.isEmpty()
                      || f.getRegionCode().equals(regionCode))
            .filter(f -> facilityName == null || facilityName.isEmpty()
                      || f.getFacilityName().contains(facilityName))
            .skip(offset != null ? offset : 0)
            .limit(limit != null ? limit : 10)
            .collect(Collectors.toList());
    }

    @Override
    public CultureFacilityVO selectFacility(String facilityId) {
        // Java 8 Optional 사용
        return MOCK_DATA.stream()
            .filter(f -> f.getFacilityId().equals(facilityId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public int selectTotalCount(Map<String, Object> params) {
        String facilityType = (String) params.get("facilityType");
        String regionCode = (String) params.get("regionCode");
        String facilityName = (String) params.get("facilityName");

        return (int) MOCK_DATA.stream()
            .filter(f -> facilityType == null || facilityType.isEmpty()
                      || f.getFacilityType().equals(facilityType))
            .filter(f -> regionCode == null || regionCode.isEmpty()
                      || f.getRegionCode().equals(regionCode))
            .filter(f -> facilityName == null || facilityName.isEmpty()
                      || f.getFacilityName().contains(facilityName))
            .count();
    }
}
```

#### 2. PUBC Mock 구현

**MockCommonProc.java** (PUBC 인증/로깅 Mock):
```java
package iros.test.common;

import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
public class MockCommonProc {

    // Mock 서비스키 저장소
    private static final Map<String, String> VALID_SERVICE_KEYS = new HashMap<>();

    static {
        // 테스트용 서비스키
        VALID_SERVICE_KEYS.put("TEST_KEY_001", "테스트 사용자 1");
        VALID_SERVICE_KEYS.put("TEST_KEY_002", "테스트 사용자 2");
        VALID_SERVICE_KEYS.put("DEMO_KEY", "데모 사용자");
        VALID_SERVICE_KEYS.put("DEV_KEY", "개발자");
    }

    /**
     * Mock 인증 처리 (PUBC 대체)
     */
    public MockUserCrtfcVO authServiceCall(HttpServletRequest request) {
        MockUserCrtfcVO vo = new MockUserCrtfcVO();

        try {
            // 서비스키 추출 (Query Parameter 또는 Header)
            String serviceKey = request.getParameter("serviceKey");
            if (serviceKey == null || serviceKey.isEmpty()) {
                serviceKey = request.getHeader("serviceKey");
            }

            // 서비스키 검증
            if (serviceKey == null || serviceKey.isEmpty()) {
                vo.setTroblTyCode("11"); // NO_MANDATORY_REQUEST_PARAMETERS_ERROR
                vo.setTroblCn("서비스키가 누락되었습니다.");
                return vo;
            }

            if (!VALID_SERVICE_KEYS.containsKey(serviceKey)) {
                vo.setTroblTyCode("30"); // SERVICE_KEY_IS_NOT_REGISTERED_ERROR
                vo.setTroblCn("등록되지 않은 서비스키입니다.");
                return vo;
            }

            // 인증 성공
            vo.setTroblTyCode("00");
            vo.setTroblCn("정상");
            vo.setServiceKey(serviceKey);
            vo.setUserName(VALID_SERVICE_KEYS.get(serviceKey));
            vo.setClientIp(getClientIp(request));

            System.out.println("[Mock Auth] 인증 성공 - ServiceKey: " + serviceKey);

        } catch (Exception e) {
            vo.setTroblTyCode("01"); // APPLICATION_ERROR
            vo.setTroblCn(e.getMessage());
        }

        return vo;
    }

    /**
     * Mock 로깅 처리 (PUBC 대체)
     */
    public void useSttusServiceCall(MockUserCrtfcVO userCrtfcVO) {
        try {
            // 콘솔 로깅 (실제 환경에서는 파일 또는 DB에 저장)
            System.out.println("===== [Mock Logging] =====");
            System.out.println("서비스키: " + userCrtfcVO.getServiceKey());
            System.out.println("사용자명: " + userCrtfcVO.getUserName());
            System.out.println("클라이언트 IP: " + userCrtfcVO.getClientIp());
            System.out.println("응답 코드: " + userCrtfcVO.getTroblTyCode());
            System.out.println("응답 메시지: " + userCrtfcVO.getTroblCn());
            System.out.println("시각: " + new java.util.Date());
            System.out.println("==========================");
        } catch (Exception e) {
            System.err.println("[Mock Logging] 오류 발생: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

**MockUserCrtfcVO.java** (PUBC UserCrtfcVO 대체):
```java
package iros.test.common;

public class MockUserCrtfcVO {
    private String troblTyCode;     // 장애 유형 코드
    private String troblCn;         // 장애 내용
    private String serviceKey;      // 서비스키
    private String userName;        // 사용자명
    private String clientIp;        // 클라이언트 IP

    // Getters and Setters
    public String getTroblTyCode() { return troblTyCode; }
    public void setTroblTyCode(String troblTyCode) { this.troblTyCode = troblTyCode; }
    public String getTroblCn() { return troblCn; }
    public void setTroblCn(String troblCn) { this.troblCn = troblCn; }
    public String getServiceKey() { return serviceKey; }
    public void setServiceKey(String serviceKey) { this.serviceKey = serviceKey; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
}
```

#### 3. Spring 설정 (Mock 모드)

**context-app.xml** (Mock 사용 시):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context-4.3.xsd">

    <!-- Component Scan -->
    <context:component-scan base-package="iros.test"/>

    <!-- Mock 모드: PUBC 설정 Import 생략 -->
    <!--
    <import resource="classpath:iros/pubc/spring/context-common-pubc.xml"/>
    <import resource="classpath:iros/pubc/spring/context-datasource-pubc.xml"/>
    <import resource="classpath:iros/pubc/spring/context-sqlMap-pubc.xml"/>
    -->

    <!-- Mock Bean 수동 등록 (필요 시) -->
    <bean id="mockCommonProc" class="iros.test.common.MockCommonProc"/>
    <bean id="cultureFacilityMapper" class="iros.test.facility.dao.CultureFacilityMockMapper"/>

</beans>
```

#### 4. Controller에서 Mock 사용

**CultureFacilityRestController.java**:
```java
@Path("/api")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CultureFacilityRestController {

    @Autowired
    private CultureFacilityService facilityService;

    @Autowired
    private MockCommonProc mockCommonProc;  // Mock 사용

    @GET
    @Path("/cultureFacilities")
    public Response getCultureFacilityList(
        @QueryParam("serviceKey") String serviceKey,
        @QueryParam("facilityType") String facilityType,
        @QueryParam("regionCode") String regionCode,
        @QueryParam("facilityName") String facilityName,
        @QueryParam("pageNo") @DefaultValue("1") int pageNo,
        @QueryParam("numOfRows") @DefaultValue("10") int numOfRows,
        @Context HttpServletRequest request
    ) {
        // 1. Mock 인증
        MockUserCrtfcVO userCrtfcVO = mockCommonProc.authServiceCall(request);

        // 2. 인증 검증
        if (!"00".equals(userCrtfcVO.getTroblTyCode())) {
            return createErrorResponse(userCrtfcVO);
        }

        try {
            // 3. 비즈니스 로직 실행 (Mock 데이터 조회)
            List<CultureFacilityVO> facilities = facilityService.getFacilityList(
                facilityType, regionCode, facilityName, pageNo, numOfRows
            );

            int totalCount = facilityService.getTotalCount(
                facilityType, regionCode, facilityName
            );

            // 4. 응답 생성
            return createSuccessResponse(facilities, totalCount, pageNo, numOfRows);

        } catch (Exception e) {
            userCrtfcVO.setTroblTyCode("99");
            userCrtfcVO.setTroblCn(e.getMessage());
            return createErrorResponse(userCrtfcVO);

        } finally {
            // 5. Mock 로깅
            mockCommonProc.useSttusServiceCall(userCrtfcVO);
        }
    }
}
```

### Mock 데이터 테스트

#### 테스트용 서비스키
- `TEST_KEY_001`: 테스트 사용자 1
- `TEST_KEY_002`: 테스트 사용자 2
- `DEMO_KEY`: 데모 사용자
- `DEV_KEY`: 개발자

#### API 호출 예시
```bash
# 전체 목록 조회
curl "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=TEST_KEY_001"

# 박물관만 조회
curl "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=TEST_KEY_001&facilityType=박물관"

# 서울 지역 조회
curl "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=TEST_KEY_001&regionCode=11"

# 이름 검색
curl "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=TEST_KEY_001&facilityName=미술관"

# 잘못된 서비스키
curl "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=INVALID_KEY"
```

### Mock 데이터 추가 방법

Mock 데이터를 추가하려면 `CultureFacilityMockMapper.java`의 static 블록에 데이터를 추가:

```java
static {
    // 기존 데이터...

    // 새 데이터 추가
    MOCK_DATA.add(createFacility("F009", "인천시립박물관", "박물관",
        "인천광역시 연수구 청량로 160", "032-440-6700",
        37.4102, 126.6784, "09:00-18:00 (월요일 휴관)",
        "https://museum.incheon.go.kr", "28", "인천광역시"));
}
```

### Mock vs 실제 DB 전환

프로파일 기반 전환:

```java
@Configuration
@Profile("mock")
public class MockConfiguration {
    @Bean
    public CultureFacilityMapper cultureFacilityMapper() {
        return new CultureFacilityMockMapper();
    }
}

@Configuration
@Profile("production")
public class ProductionConfiguration {

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("cubrid.jdbc.driver.CUBRIDDriver");
        dataSource.setUrl("jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8");
        dataSource.setUsername("dba");
        dataSource.setPassword("cubrid_password");

        // Connection Pool 설정
        dataSource.setInitialSize(10);
        dataSource.setMaxTotal(50);
        dataSource.setMaxIdle(20);
        dataSource.setMinIdle(10);
        dataSource.setMaxWaitMillis(10000);

        // Connection 검증
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1 FROM db_root");

        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setConfigLocation(
            new ClassPathResource("mybatis/mybatis-config.xml")
        );
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis/mapper/**/*.xml")
        );
        return sessionFactory.getObject();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("iros.test.facility.dao");
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return configurer;
    }
}
```

application.properties:
```properties
# Mock 모드 (기본)
spring.profiles.active=mock

# 프로덕션 모드 (CUBRID)
# spring.profiles.active=production
```

### 필수 외부 라이브러리

#### Mock 모드
- **PUBC 모듈**: 불필요 (Mock으로 대체)
- **암호화 모듈**: 불필요 (Mock으로 대체)
- **데이터베이스**: 불필요 (인메모리 Mock 데이터)

#### 프로덕션 모드 (CUBRID)
- **PUBC 모듈**: `lib/iros_pubc_1.1.jar` (필수)
- **암호화 모듈**: `lib/iros_cipher.jar` (필수)
- **CUBRID JDBC Driver**: `cubrid-jdbc-11.2.jar` (필수)

### Spring 설정 파일

#### Mock 모드
- **PUBC 설정 Import**: 불필요 (주석 처리)
- **데이터소스 설정**: 불필요 (주석 처리)
- **MyBatis 설정**: 불필요 (Mock Mapper 사용)

#### 프로덕션 모드 (CUBRID)
- **PUBC 설정 Import**: 필수
- **데이터소스 설정**: CUBRID 연결 정보
- **MyBatis 설정**: Mapper XML 사용

### 에러 코드 체계
| 코드 | 설명 |
|------|------|
| `000` | 정상 |
| `001` | APPLICATION_ERROR |
| `010` | INVALID_REQUEST_PARAMETER_ERROR |
| `030` | SERVICE_KEY_IS_NOT_REGISTERED_ERROR |
| `031` | DEADLINE_HAS_EXPIRED_ERROR |
| `032` | UNREGISTERED_IP_ERROR |

---

## 개발 시 주의사항

### DO
- ✅ JDK 1.8 및 Java 8 기능 적극 활용 (Lambda, Stream, Optional)
- ✅ MyBatis Mapper Interface 사용 (Annotation 또는 XML)
- ✅ PUBC 인증/로깅은 반드시 `CommonProc` 통해 호출
- ✅ SQL Injection 방어: MyBatis `#{parameter}` 사용
- ✅ 조회 전용 API이므로 SELECT 쿼리만 작성

### DON'T
- ❌ PUBC JAR 파일 수정 금지
- ❌ iBATIS를 프로젝트 코드에 사용 금지 (PUBC 전용)
- ❌ POST/PUT/DELETE 엔드포인트 추가 금지 (조회 전용 프로젝트)
- ❌ PUBC Spring 설정 파일 수정 금지
- ❌ `UserCrtfcVO` 인증 결과 무시 금지

### PUBC 호환성
- **Java 하위 호환**: PUBC는 Java 5로 컴파일되었으나 Java 8에서 완벽 동작
- **Spring 하위 호환**: Spring 4.3.30이 Spring 2.5.6 API 하위 호환
- **iBATIS 공존**: MyBatis와 iBATIS가 독립적으로 동작

---

## 테스트

### 단위 테스트
- **프레임워크**: JUnit 4.13.2
- **대상**: Service, Mapper 계층
- **Mock**: PUBC 모듈은 Mock 가능

### 통합 테스트
1. PUBC 인증 테스트 (서비스키 검증)
2. REST API 호출 테스트 (cURL, Postman)
3. SOAP 웹 서비스 테스트 (SoapUI)
4. 로깅 검증 (TN_PUBC_USE_STTUS_INFO 테이블 확인)

### 성능 테스트
- **도구**: Apache JMeter
- **목표**: API 평균 응답 시간 1초 이내, TPS 50 이상

---

## 프로덕션 모드 (CUBRID) 설정

### CUBRID 데이터베이스 설정

#### 1. CUBRID 설치 및 데이터베이스 생성

```bash
# CUBRID 서비스 시작
cubrid service start

# 데이터베이스 생성
cubrid createdb pubcdb en_US.utf8

# 데이터베이스 시작
cubrid server start pubcdb

# 데이터베이스 상태 확인
cubrid server status
```

#### 2. CUBRID JDBC 드라이버 설치

**Maven pom.xml**:
```xml
<dependency>
    <groupId>cubrid</groupId>
    <artifactId>cubrid-jdbc</artifactId>
    <version>11.2.0.0008</version>
</dependency>
```

**또는 수동 설치** (JBoss EAP):
```bash
# 1. CUBRID JDBC 드라이버 모듈 디렉토리 생성
mkdir -p $JBOSS_HOME/modules/cubrid/jdbc/main

# 2. JDBC 드라이버 복사
cp $CUBRID/jdbc/cubrid_jdbc.jar $JBOSS_HOME/modules/cubrid/jdbc/main/

# 3. module.xml 생성
cat > $JBOSS_HOME/modules/cubrid/jdbc/main/module.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.5" name="cubrid.jdbc">
    <resources>
        <resource-root path="cubrid_jdbc.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
EOF
```

#### 3. 데이터베이스 스키마 생성

**schema-cubrid.sql**:
```sql
-- 문화시설 정보 테이블
CREATE TABLE TB_CULTURE_FACILITY (
    FACILITY_ID      VARCHAR(20) PRIMARY KEY,
    FACILITY_NAME    VARCHAR(200) NOT NULL,
    FACILITY_TYPE    VARCHAR(50) NOT NULL,
    ADDRESS          VARCHAR(500),
    PHONE            VARCHAR(20),
    LATITUDE         DOUBLE,
    LONGITUDE        DOUBLE,
    OPEN_TIME        VARCHAR(200),
    HOMEPAGE         VARCHAR(500),
    REGION_CODE      VARCHAR(10),
    MANAGE_AGENCY    VARCHAR(200),
    UPDATE_DATE      VARCHAR(10),
    CONSTRAINT CK_FACILITY_TYPE CHECK (FACILITY_TYPE IN ('박물관', '미술관', '도서관'))
);

-- 인덱스 생성
CREATE INDEX IDX_FACILITY_TYPE ON TB_CULTURE_FACILITY(FACILITY_TYPE);
CREATE INDEX IDX_REGION_CODE ON TB_CULTURE_FACILITY(REGION_CODE);
CREATE INDEX IDX_FACILITY_NAME ON TB_CULTURE_FACILITY(FACILITY_NAME);

-- PUBC 서비스키 인증 테이블
CREATE TABLE TN_PUBC_USER_CRTFC (
    SERVICE_KEY      VARCHAR(100) PRIMARY KEY,
    USER_NAME        VARCHAR(100),
    IP_ADDRESS       VARCHAR(50),
    START_DATE       VARCHAR(10),
    END_DATE         VARCHAR(10),
    DAILY_LIMIT      INTEGER DEFAULT 1000,
    STATUS           VARCHAR(10) DEFAULT 'ACTIVE',
    REG_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PUBC 사용 현황 로깅 테이블
CREATE TABLE TN_PUBC_USE_STTUS_INFO (
    USE_SEQ          INTEGER AUTO_INCREMENT PRIMARY KEY,
    SERVICE_KEY      VARCHAR(100),
    REQUEST_TIME     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    REQUEST_PARAMS   VARCHAR(1000),
    RESPONSE_CODE    VARCHAR(10),
    RESPONSE_TIME    INTEGER,
    CLIENT_IP        VARCHAR(50),
    USER_AGENT       VARCHAR(500)
);

-- 인덱스
CREATE INDEX IDX_USE_SERVICE_KEY ON TN_PUBC_USE_STTUS_INFO(SERVICE_KEY);
CREATE INDEX IDX_USE_REQUEST_TIME ON TN_PUBC_USE_STTUS_INFO(REQUEST_TIME);

COMMIT;
```

**초기 데이터 삽입 (data-cubrid.sql)**:
```sql
-- 문화시설 샘플 데이터
INSERT INTO TB_CULTURE_FACILITY VALUES
('F001', '국립중앙박물관', '박물관', '서울특별시 용산구 서빙고로 137',
 '02-2077-9000', 37.5240, 126.9803, '10:00-18:00 (월요일 휴관)',
 'https://www.museum.go.kr', '11', '문화체육관광부', '2025-01-15');

INSERT INTO TB_CULTURE_FACILITY VALUES
('F002', '국립현대미술관', '미술관', '경기도 과천시 광명로 313',
 '02-2188-6000', 37.4354, 127.0146, '10:00-18:00 (월요일 휴관)',
 'https://www.mmca.go.kr', '41', '문화체육관광부', '2025-01-15');

INSERT INTO TB_CULTURE_FACILITY VALUES
('F003', '국립중앙도서관', '도서관', '서울특별시 서초구 반포대로 201',
 '02-535-4142', 37.5034, 127.0376, '09:00-21:00 (연중무휴)',
 'https://www.nl.go.kr', '11', '문화체육관광부', '2025-01-15');

-- 테스트 서비스키
INSERT INTO TN_PUBC_USER_CRTFC VALUES
('PROD_KEY_001', '운영 사용자 1', '192.168.1.100', '2025-01-01', '2025-12-31', 10000, 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO TN_PUBC_USER_CRTFC VALUES
('PROD_KEY_002', '운영 사용자 2', '192.168.1.101', '2025-01-01', '2025-12-31', 5000, 'ACTIVE', CURRENT_TIMESTAMP);

COMMIT;
```

**스키마 실행**:
```bash
# csql 접속
csql -u dba pubcdb

# 스키마 실행
csql -u dba pubcdb < schema-cubrid.sql
csql -u dba pubcdb < data-cubrid.sql
```

#### 4. MyBatis Mapper XML (CUBRID용)

**CultureFacilityMapper.xml**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="iros.test.facility.dao.CultureFacilityMapper">

    <resultMap id="facilityResult" type="iros.test.facility.vo.CultureFacilityVO">
        <id property="facilityId" column="FACILITY_ID"/>
        <result property="facilityName" column="FACILITY_NAME"/>
        <result property="facilityType" column="FACILITY_TYPE"/>
        <result property="address" column="ADDRESS"/>
        <result property="phone" column="PHONE"/>
        <result property="latitude" column="LATITUDE"/>
        <result property="longitude" column="LONGITUDE"/>
        <result property="openTime" column="OPEN_TIME"/>
        <result property="homepage" column="HOMEPAGE"/>
        <result property="regionCode" column="REGION_CODE"/>
        <result property="manageAgency" column="MANAGE_AGENCY"/>
        <result property="updateDate" column="UPDATE_DATE"/>
    </resultMap>

    <!-- 문화시설 목록 조회 (CUBRID LIMIT 문법) -->
    <select id="selectFacilityList" parameterType="map" resultMap="facilityResult">
        SELECT
            FACILITY_ID, FACILITY_NAME, FACILITY_TYPE,
            ADDRESS, PHONE, LATITUDE, LONGITUDE,
            OPEN_TIME, HOMEPAGE, REGION_CODE,
            MANAGE_AGENCY, UPDATE_DATE
        FROM TB_CULTURE_FACILITY
        WHERE 1=1
        <if test="facilityType != null and facilityType != ''">
            AND FACILITY_TYPE = #{facilityType}
        </if>
        <if test="regionCode != null and regionCode != ''">
            AND REGION_CODE = #{regionCode}
        </if>
        <if test="facilityName != null and facilityName != ''">
            AND FACILITY_NAME LIKE '%' || #{facilityName} || '%'
        </if>
        ORDER BY UPDATE_DATE DESC
        LIMIT #{offset}, #{limit}
    </select>

    <!-- 문화시설 상세 조회 -->
    <select id="selectFacility" parameterType="string" resultMap="facilityResult">
        SELECT
            FACILITY_ID, FACILITY_NAME, FACILITY_TYPE,
            ADDRESS, PHONE, LATITUDE, LONGITUDE,
            OPEN_TIME, HOMEPAGE, REGION_CODE,
            MANAGE_AGENCY, UPDATE_DATE
        FROM TB_CULTURE_FACILITY
        WHERE FACILITY_ID = #{facilityId}
    </select>

    <!-- 문화시설 총 개수 -->
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
            AND FACILITY_NAME LIKE '%' || #{facilityName} || '%'
        </if>
    </select>

</mapper>
```

#### 5. PUBC 설정 (CUBRID 연동)

**context-datasource-pubc.xml** (CUBRID용):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans-4.3.xsd">

    <!-- CUBRID 데이터소스 -->
    <bean id="dataSource" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="cubrid.jdbc.driver.CUBRIDDriver"/>
        <property name="url" value="jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8"/>
        <property name="username" value="dba"/>
        <property name="password" value="cubrid_password"/>

        <!-- Connection Pool 설정 -->
        <property name="initialSize" value="10"/>
        <property name="maxTotal" value="50"/>
        <property name="maxIdle" value="20"/>
        <property name="minIdle" value="10"/>
        <property name="maxWaitMillis" value="10000"/>

        <!-- Connection 검증 -->
        <property name="testOnBorrow" value="true"/>
        <property name="validationQuery" value="SELECT 1 FROM db_root"/>
    </bean>

</beans>
```

#### 6. 프로덕션 모드 Controller (PUBC 연동)

**CultureFacilityRestController.java** (프로덕션):
```java
@Path("/api")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CultureFacilityRestController {

    @Autowired
    private CultureFacilityService facilityService;

    @Autowired(required = false)
    private CommonProc commonProc;  // PUBC (프로덕션)

    @Autowired(required = false)
    private MockCommonProc mockCommonProc;  // Mock (개발)

    @GET
    @Path("/cultureFacilities")
    public Response getCultureFacilityList(
        @QueryParam("serviceKey") String serviceKey,
        @QueryParam("facilityType") String facilityType,
        @QueryParam("regionCode") String regionCode,
        @QueryParam("facilityName") String facilityName,
        @QueryParam("pageNo") @DefaultValue("1") int pageNo,
        @QueryParam("numOfRows") @DefaultValue("10") int numOfRows,
        @Context HttpServletRequest request
    ) {
        Object userCrtfcVO;

        // 프로파일에 따라 인증 방식 선택
        if (commonProc != null) {
            // 프로덕션: PUBC 사용
            userCrtfcVO = commonProc.authServiceCall(request);
        } else {
            // 개발: Mock 사용
            userCrtfcVO = mockCommonProc.authServiceCall(request);
        }

        // ... 비즈니스 로직 동일 ...
    }
}
```

### CUBRID 연결 정보

#### 기본 연결 정보
- **호스트**: localhost
- **포트**: 33000 (기본)
- **데이터베이스**: pubcdb
- **사용자**: dba
- **비밀번호**: cubrid_password (변경 필요)
- **문자셋**: UTF-8

#### JDBC URL 형식
```
jdbc:cubrid:<host>:<port>:<database>:<user>:<password>:?<properties>
```

#### 연결 예시
```java
// 기본 연결
jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8

// 사용자/비밀번호 포함
jdbc:cubrid:localhost:33000:pubcdb:dba:cubrid_password:?charset=utf8

// 원격 서버
jdbc:cubrid:192.168.1.200:33000:pubcdb:::?charset=utf8

// HA (고가용성) 연결
jdbc:cubrid:192.168.1.200:33000,192.168.1.201:33000:pubcdb:::?charset=utf8&altHosts=192.168.1.201:33000
```

### CUBRID 관리 명령어

```bash
# 데이터베이스 시작/중지
cubrid server start pubcdb
cubrid server stop pubcdb
cubrid server restart pubcdb

# 데이터베이스 상태 확인
cubrid server status

# 백업
cubrid backupdb -S -D /backup/path pubcdb

# 복구
cubrid restoredb -d /backup/path pubcdb

# 데이터베이스 최적화
cubrid optimizedb pubcdb

# 로그 확인
tail -f $CUBRID/log/pubcdb_<date>.err
```

### CUBRID 성능 튜닝

**cubrid.conf**:
```ini
# 메모리 설정
data_buffer_size=512M
sort_buffer_size=8M

# 연결 설정
max_clients=100

# 로그 설정
log_buffer_size=8M

# 쿼리 캐시
max_query_cache_entries=1000
query_cache_size=100M
```

---

## 배포 (JBoss EAP)

### WAR 파일 빌드
```bash
# Mock 모드 빌드
mvn clean package -Pdev

# 프로덕션 모드 빌드 (CUBRID)
mvn clean package -Pprod
```

### JBoss EAP 배포

#### 1. JBoss EAP 버전
- **권장 버전**: JBoss EAP 7.4 이상
- **JDK 호환성**: JDK 1.8 지원
- **배포 모드**: Standalone 또는 Domain Mode
- **데이터베이스**: CUBRID 11.2+ (프로덕션)

#### 2. 배포 방법

##### 방법 1: 관리 콘솔 (Web Console)
1. JBoss EAP 관리 콘솔 접속: `http://localhost:9990/console`
2. **Deployments** 메뉴 선택
3. **Add** 버튼 클릭
4. WAR 파일 업로드: `target/pubc-test-api.war`
5. **Enable** 클릭하여 배포 활성화

##### 방법 2: CLI (Command Line Interface)
```bash
# JBoss CLI 접속
cd $JBOSS_HOME/bin
./jboss-cli.sh --connect

# WAR 파일 배포
deploy /path/to/pubc-test-api.war

# 배포 확인
deployment-info

# 배포 해제 (필요 시)
undeploy pubc-test-api.war

# CLI 종료
quit
```

##### 방법 3: Hot Deployment (개발 환경)
```bash
# WAR 파일을 deployments 디렉토리에 복사
cp target/pubc-test-api.war $JBOSS_HOME/standalone/deployments/

# 배포 완료 확인
# pubc-test-api.war.deployed 파일이 생성되면 성공
ls $JBOSS_HOME/standalone/deployments/
```

#### 3. JBoss EAP 설정

##### standalone.xml 데이터소스 설정 (CUBRID)
```xml
<subsystem xmlns="urn:jboss:domain:datasources:6.0">
    <datasources>
        <!-- CUBRID 데이터소스 (프로덕션) -->
        <datasource jndi-name="java:jboss/datasources/PubcTestDS"
                    pool-name="PubcTestDS" enabled="true"
                    use-java-context="true">
            <connection-url>jdbc:cubrid:localhost:33000:pubcdb:::?charset=utf8</connection-url>
            <driver>cubrid</driver>
            <security>
                <user-name>dba</user-name>
                <password>${VAULT::DB::password::1}</password>
            </security>
            <pool>
                <min-pool-size>10</min-pool-size>
                <max-pool-size>50</max-pool-size>
                <prefill>true</prefill>
            </pool>
            <timeout>
                <idle-timeout-minutes>5</idle-timeout-minutes>
                <query-timeout>30</query-timeout>
            </timeout>
            <validation>
                <validate-on-match>true</validate-on-match>
                <background-validation>false</background-validation>
                <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.novendor.NullValidConnectionChecker"/>
                <check-valid-connection-sql>SELECT 1 FROM db_root</check-valid-connection-sql>
            </validation>
        </datasource>

        <drivers>
            <driver name="cubrid" module="cubrid.jdbc">
                <driver-class>cubrid.jdbc.driver.CUBRIDDriver</driver-class>
                <xa-datasource-class>cubrid.jdbc.driver.CUBRIDXADataSource</xa-datasource-class>
            </driver>
        </drivers>
    </datasources>
</subsystem>
```

##### JDBC 드라이버 모듈 설치 (CUBRID)

**CUBRID JDBC 드라이버 설치**:
```bash
# 1. 모듈 디렉토리 생성
mkdir -p $JBOSS_HOME/modules/cubrid/jdbc/main

# 2. CUBRID JDBC 드라이버 복사
cp $CUBRID/jdbc/cubrid_jdbc.jar $JBOSS_HOME/modules/cubrid/jdbc/main/

# 3. module.xml 생성
cat > $JBOSS_HOME/modules/cubrid/jdbc/main/module.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.5" name="cubrid.jdbc">
    <resources>
        <resource-root path="cubrid_jdbc.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
EOF

# 4. 드라이버 확인
ls -la $JBOSS_HOME/modules/cubrid/jdbc/main/
```

##### jboss-web.xml 설정 (프로젝트 내)
```xml
<!-- WebContent/WEB-INF/jboss-web.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<jboss-web xmlns="http://www.jboss.com/xml/ns/javaee"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://www.jboss.com/xml/ns/javaee
                               http://www.jboss.org/schema/jbossas/jboss-web_14_0.xsd">
    <context-root>/pubc-test-api</context-root>

    <!-- 데이터소스 참조 -->
    <resource-ref>
        <res-ref-name>jdbc/PubcTestDS</res-ref-name>
        <jndi-name>java:jboss/datasources/PubcTestDS</jndi-name>
    </resource-ref>
</jboss-web>
```

#### 4. 접근 URL
- **관리 콘솔**: `http://localhost:9990/console`
- **REST API**: `http://localhost:8080/pubc-test-api/api/`
- **SOAP 서비스**: `http://localhost:8080/pubc-test-api/services/`
- **WSDL**: `http://localhost:8080/pubc-test-api/services/CultureFacilityService?wsdl`

#### 5. JBoss EAP 시작/중지

```bash
# Standalone 모드 시작
cd $JBOSS_HOME/bin
./standalone.sh

# 백그라운드 실행
nohup ./standalone.sh > /dev/null 2>&1 &

# 특정 설정 파일로 시작
./standalone.sh -c standalone-full.xml

# 포트 변경
./standalone.sh -Djboss.socket.binding.port-offset=100

# JBoss EAP 중지
./jboss-cli.sh --connect command=:shutdown
```

#### 6. 로그 확인
```bash
# 서버 로그
tail -f $JBOSS_HOME/standalone/log/server.log

# 애플리케이션 로그
tail -f $JBOSS_HOME/standalone/log/pubc-test-api.log
```

#### 7. 배포 검증
```bash
# 배포 상태 확인
curl http://localhost:9990/management/deployments

# Health Check (REST API)
curl http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=TEST_KEY

# WSDL 확인
curl http://localhost:8080/pubc-test-api/services/CultureFacilityService?wsdl
```

### 환경별 설정

#### 개발 환경 (Mock 모드)
- **데이터소스**: Mock 데이터 (인메모리)
- **프로파일**: `spring.profiles.active=mock`
- **로그 레벨**: DEBUG
- **포트**: 8080 (기본)
- **JVM 옵션**: `-Xms512m -Xmx1024m`
- **PUBC 모듈**: Mock으로 대체

#### 테스트 환경
- **데이터소스**: 테스트 CUBRID DB
- **프로파일**: `spring.profiles.active=test`
- **로그 레벨**: INFO
- **포트**: 8080
- **JVM 옵션**: `-Xms1024m -Xmx2048m`
- **PUBC 모듈**: 통합 테스트

#### 운영 환경 (프로덕션)
- **데이터소스**: 운영 CUBRID DB
- **프로파일**: `spring.profiles.active=production`
- **로그 레벨**: WARN, ERROR
- **포트**: 8080 (또는 Load Balancer 뒤)
- **JVM 옵션**: `-Xms2048m -Xmx4096m -XX:+UseG1GC`
- **PUBC 모듈**: 완전 통합
- **클러스터링**: Domain Mode 권장
- **CUBRID**: HA 구성 권장

### JBoss EAP 성능 튜닝

#### JVM 옵션 설정 (standalone.conf)
```bash
# $JBOSS_HOME/bin/standalone.conf

# Heap 메모리
JAVA_OPTS="$JAVA_OPTS -Xms2048m -Xmx4096m"

# GC 설정
JAVA_OPTS="$JAVA_OPTS -XX:+UseG1GC"
JAVA_OPTS="$JAVA_OPTS -XX:MaxGCPauseMillis=200"

# 메타스페이스
JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"

# GC 로그
JAVA_OPTS="$JAVA_OPTS -Xloggc:$JBOSS_HOME/standalone/log/gc.log"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps"

# 애플리케이션 파라미터
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=production"
```

#### 커넥션 풀 튜닝
```xml
<datasource ...>
    <pool>
        <min-pool-size>10</min-pool-size>
        <max-pool-size>50</max-pool-size>
        <prefill>true</prefill>
    </pool>
    <timeout>
        <idle-timeout-minutes>5</idle-timeout-minutes>
        <query-timeout>30</query-timeout>
    </timeout>
</datasource>
```

### JBoss EAP 모니터링

#### JMX 모니터링 활성화
```bash
# standalone.sh 실행 시 옵션 추가
./standalone.sh \
  -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false
```

#### CLI를 통한 모니터링
```bash
# 메모리 사용량
/core-service=platform-mbean/type=memory:read-resource(include-runtime=true)

# 스레드 정보
/core-service=platform-mbean/type=threading:read-resource(include-runtime=true)

# 데이터소스 통계
/subsystem=datasources/data-source=PubcTestDS/statistics=pool:read-resource(include-runtime=true)
```

### 트러블슈팅

#### 배포 실패 시
1. 로그 확인: `$JBOSS_HOME/standalone/log/server.log`
2. 클래스 로딩 문제: `jboss-deployment-structure.xml` 검토
3. 라이브러리 충돌: JBoss 제공 모듈과 WAR 내 라이브러리 비교

#### 메모리 부족
```bash
# Heap Dump 생성
jmap -dump:format=b,file=heapdump.hprof <pid>

# 메모리 분석
jvisualvm
```

#### 성능 저하
1. GC 로그 분석
2. 스레드 덤프 확인: `jstack <pid>`
3. 데이터소스 커넥션 풀 상태 확인

---

## JBoss EAP 도메인 모드 (멀티 노드 클러스터링)

### 도메인 모드 개요

도메인 모드는 여러 JBoss EAP 인스턴스를 중앙에서 관리하고 클러스터링하는 방식입니다.

#### 아키텍처
```
┌─────────────────────────────────────────────────────────────┐
│                    Domain Controller                        │
│              (Master - 192.168.1.100)                       │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  - domain.xml (도메인 전체 설정)                        │  │
│  │  - host-master.xml (마스터 호스트 설정)                 │  │
│  │  - 중앙 관리 콘솔 (9990 포트)                           │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          ↓ (관리 및 배포)
        ┌─────────────────┴───────────────┐
        ↓                                 ↓
┌─────────────────────┐         ┌─────────────────────┐
│   Host Controller   │         │   Host Controller   │
│  (Slave - Node1)    │         │  (Slave - Node2)    │
│   192.168.1.101     │         │   192.168.1.102     │
├─────────────────────┤         ├─────────────────────┤
│ ┌─────────────────┐ │         │ ┌─────────────────┐ │
│ │  Server: app1   │ │         │ │  Server: app3   │ │
│ │  Port: 8080     │ │         │ │  Port: 8080     │ │
│ │  Offset: 0      │ │         │ │  Offset: 0      │ │
│ └─────────────────┘ │         │ └─────────────────┘ │
│ ┌─────────────────┐ │         │ ┌─────────────────┐ │
│ │  Server: app2   │ │         │ │  Server: app4   │ │
│ │  Port: 8180     │ │         │ │  Port: 8180     │ │
│ │  Offset: 100    │ │         │ │  Offset: 100    │ │
│ └─────────────────┘ │         │ └─────────────────┘ │
└─────────────────────┘         └─────────────────────┘
```

### 1. 도메인 모드 설정

#### 디렉토리 구조
```
$JBOSS_HOME/
├── domain/
│   ├── configuration/
│   │   ├── domain.xml              # 도메인 전체 설정
│   │   ├── host-master.xml         # 마스터 호스트 설정
│   │   ├── host-slave.xml          # 슬레이브 호스트 설정
│   │   └── host.xml                # 기본 호스트 설정
│   ├── servers/                    # 서버 인스턴스 디렉토리
│   │   ├── server-one/
│   │   └── server-two/
│   └── data/                       # 도메인 데이터
└── bin/
    ├── domain.sh                   # 도메인 모드 시작 스크립트
    └── jboss-cli.sh                # CLI 도구
```

### 2. Domain Controller (Master) 설정

#### host-master.xml 설정
```xml
<?xml version="1.0" encoding="UTF-8"?>
<host xmlns="urn:jboss:domain:17.0" name="master">

    <!-- 관리 인터페이스 -->
    <management>
        <security-realms>
            <security-realm name="ManagementRealm">
                <server-identities>
                    <!-- 슬레이브 인증용 시크릿 -->
                    <secret value="c2xhdmVfc2VjcmV0MTIz"/>
                </server-identities>
                <authentication>
                    <local default-user="$local" skip-group-loading="true"/>
                    <properties path="mgmt-users.properties"
                               relative-to="jboss.domain.config.dir"/>
                </authentication>
            </security-realm>
        </security-realms>

        <management-interfaces>
            <!-- HTTP 관리 인터페이스 -->
            <http-interface security-realm="ManagementRealm">
                <http-upgrade enabled="true"/>
                <socket interface="management" port="${jboss.management.http.port:9990}"/>
            </http-interface>
        </management-interfaces>
    </management>

    <!-- 도메인 컨트롤러 (마스터) -->
    <domain-controller>
        <local/>
    </domain-controller>

    <!-- 인터페이스 -->
    <interfaces>
        <interface name="management">
            <inet-address value="${jboss.bind.address.management:192.168.1.100}"/>
        </interface>
        <interface name="public">
            <inet-address value="${jboss.bind.address:192.168.1.100}"/>
        </interface>
    </interfaces>

    <!-- JVM 설정 -->
    <jvms>
        <jvm name="default">
            <heap size="1024m" max-size="2048m"/>
            <jvm-options>
                <option value="-server"/>
                <option value="-XX:+UseG1GC"/>
                <option value="-Djava.net.preferIPv4Stack=true"/>
            </jvm-options>
        </jvm>
    </jvms>

    <!-- 서버 그룹에 속할 서버 정의 (마스터에는 보통 서버 없음) -->
    <!--
    <servers>
        <server name="master-server" group="pubc-api-servers">
            <socket-bindings port-offset="0"/>
        </server>
    </servers>
    -->
</host>
```

### 3. Host Controller (Slave) 설정

#### Node1 (192.168.1.101) - host-slave.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<host xmlns="urn:jboss:domain:17.0" name="node1">

    <management>
        <security-realms>
            <security-realm name="ManagementRealm">
                <server-identities>
                    <!-- 마스터 인증용 시크릿 (마스터와 동일) -->
                    <secret value="c2xhdmVfc2VjcmV0MTIz"/>
                </server-identities>
                <authentication>
                    <local default-user="$local" skip-group-loading="true"/>
                    <properties path="mgmt-users.properties"
                               relative-to="jboss.domain.config.dir"/>
                </authentication>
            </security-realm>
        </security-realms>

        <management-interfaces>
            <http-interface security-realm="ManagementRealm">
                <http-upgrade enabled="true"/>
                <socket interface="management" port="${jboss.management.http.port:9990}"/>
            </http-interface>
        </management-interfaces>
    </management>

    <!-- 원격 도메인 컨트롤러 (마스터 연결) -->
    <domain-controller>
        <remote security-realm="ManagementRealm" username="slave">
            <discovery-options>
                <static-discovery name="master-controller"
                                 protocol="remote"
                                 host="192.168.1.100"
                                 port="9999"/>
            </discovery-options>
        </remote>
    </domain-controller>

    <!-- 인터페이스 -->
    <interfaces>
        <interface name="management">
            <inet-address value="${jboss.bind.address.management:192.168.1.101}"/>
        </interface>
        <interface name="public">
            <inet-address value="${jboss.bind.address:192.168.1.101}"/>
        </interface>
    </interfaces>

    <!-- JVM 설정 -->
    <jvms>
        <jvm name="default">
            <heap size="2048m" max-size="4096m"/>
            <jvm-options>
                <option value="-server"/>
                <option value="-XX:+UseG1GC"/>
                <option value="-XX:MaxGCPauseMillis=200"/>
                <option value="-Djava.net.preferIPv4Stack=true"/>
                <option value="-Dspring.profiles.active=production"/>
            </jvm-options>
        </jvm>
    </jvms>

    <!-- 서버 인스턴스 정의 -->
    <servers>
        <!-- 서버 1: 포트 8080 -->
        <server name="pubc-api-server1" group="pubc-api-servers" auto-start="true">
            <socket-bindings port-offset="0"/>
            <jvm name="default">
                <heap size="2048m" max-size="4096m"/>
            </jvm>
        </server>

        <!-- 서버 2: 포트 8180 (offset 100) -->
        <server name="pubc-api-server2" group="pubc-api-servers" auto-start="true">
            <socket-bindings port-offset="100"/>
            <jvm name="default">
                <heap size="2048m" max-size="4096m"/>
            </jvm>
        </server>
    </servers>
</host>
```

#### Node2 (192.168.1.102) - host-slave.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<host xmlns="urn:jboss:domain:17.0" name="node2">

    <!-- management, domain-controller, interfaces는 node1과 동일 -->
    <!-- IP만 192.168.1.102로 변경 -->

    <interfaces>
        <interface name="management">
            <inet-address value="${jboss.bind.address.management:192.168.1.102}"/>
        </interface>
        <interface name="public">
            <inet-address value="${jboss.bind.address:192.168.1.102}"/>
        </interface>
    </interfaces>

    <!-- 서버 인스턴스 정의 -->
    <servers>
        <!-- 서버 3: 포트 8080 -->
        <server name="pubc-api-server3" group="pubc-api-servers" auto-start="true">
            <socket-bindings port-offset="0"/>
            <jvm name="default">
                <heap size="2048m" max-size="4096m"/>
            </jvm>
        </server>

        <!-- 서버 4: 포트 8180 (offset 100) -->
        <server name="pubc-api-server4" group="pubc-api-servers" auto-start="true">
            <socket-bindings port-offset="100"/>
            <jvm name="default">
                <heap size="2048m" max-size="4096m"/>
            </jvm>
        </server>
    </servers>
</host>
```

### 4. domain.xml 설정 (서버 그룹 및 프로파일)

#### domain.xml - 서버 그룹 정의
```xml
<?xml version="1.0" encoding="UTF-8"?>
<domain xmlns="urn:jboss:domain:17.0">

    <!-- 프로파일 -->
    <profiles>
        <profile name="pubc-api-profile">
            <!-- 기본 서브시스템 -->
            <subsystem xmlns="urn:jboss:domain:logging:8.0">
                <!-- 로깅 설정 -->
            </subsystem>

            <!-- 데이터소스 (CUBRID) -->
            <subsystem xmlns="urn:jboss:domain:datasources:6.0">
                <datasources>
                    <!-- CUBRID 데이터소스 (클러스터 공통) -->
                    <datasource jndi-name="java:jboss/datasources/PubcTestDS"
                                pool-name="PubcTestDS"
                                enabled="true"
                                use-java-context="true">
                        <connection-url>jdbc:cubrid:192.168.1.200:33000:pubcdb:::?charset=utf8</connection-url>
                        <driver>cubrid</driver>
                        <security>
                            <user-name>dba</user-name>
                            <password>${VAULT::DB::password::1}</password>
                        </security>
                        <pool>
                            <min-pool-size>10</min-pool-size>
                            <max-pool-size>50</max-pool-size>
                            <prefill>true</prefill>
                        </pool>
                        <timeout>
                            <idle-timeout-minutes>5</idle-timeout-minutes>
                            <query-timeout>30</query-timeout>
                        </timeout>
                        <validation>
                            <validate-on-match>true</validate-on-match>
                            <background-validation>false</background-validation>
                            <check-valid-connection-sql>SELECT 1 FROM db_root</check-valid-connection-sql>
                        </validation>
                    </datasource>

                    <drivers>
                        <driver name="cubrid" module="cubrid.jdbc">
                            <driver-class>cubrid.jdbc.driver.CUBRIDDriver</driver-class>
                            <xa-datasource-class>cubrid.jdbc.driver.CUBRIDXADataSource</xa-datasource-class>
                        </driver>
                    </drivers>
                </datasources>
            </subsystem>

            <!-- 인피니스팬 (세션 클러스터링) -->
            <subsystem xmlns="urn:jboss:domain:infinispan:13.0">
                <cache-container name="web" default-cache="dist" module="org.wildfly.clustering.web.infinispan">
                    <transport lock-timeout="60000"/>
                    <distributed-cache name="dist">
                        <locking isolation="REPEATABLE_READ"/>
                        <transaction mode="BATCH"/>
                        <file-store/>
                    </distributed-cache>
                </cache-container>
            </subsystem>

            <!-- JGroups (클러스터 통신) -->
            <subsystem xmlns="urn:jboss:domain:jgroups:8.0">
                <channels default="ee">
                    <channel name="ee" stack="tcp" cluster="pubc-api-cluster"/>
                </channels>
                <stacks>
                    <stack name="tcp">
                        <transport type="TCP" socket-binding="jgroups-tcp"/>
                        <protocol type="TCPPING">
                            <property name="initial_hosts">
                                192.168.1.101[7600],192.168.1.102[7600]
                            </property>
                            <property name="port_range">0</property>
                        </protocol>
                        <protocol type="MERGE3"/>
                        <protocol type="FD_SOCK"/>
                        <protocol type="FD_ALL"/>
                        <protocol type="VERIFY_SUSPECT"/>
                        <protocol type="pbcast.NAKACK2"/>
                        <protocol type="UNICAST3"/>
                        <protocol type="pbcast.STABLE"/>
                        <protocol type="pbcast.GMS"/>
                        <protocol type="MFC"/>
                        <protocol type="FRAG3"/>
                    </stack>
                </stacks>
            </subsystem>
        </profile>
    </profiles>

    <!-- 서버 그룹 -->
    <server-groups>
        <server-group name="pubc-api-servers" profile="pubc-api-profile">
            <jvm name="default">
                <heap size="2048m" max-size="4096m"/>
                <jvm-options>
                    <option value="-server"/>
                    <option value="-XX:+UseG1GC"/>
                </jvm-options>
            </jvm>
            <socket-binding-group ref="full-sockets"/>

            <!-- 배포 -->
            <deployments>
                <deployment name="pubc-test-api.war" runtime-name="pubc-test-api.war"/>
            </deployments>
        </server-group>
    </server-groups>

    <!-- 소켓 바인딩 -->
    <socket-binding-groups>
        <socket-binding-group name="full-sockets" default-interface="public">
            <socket-binding name="http" port="${jboss.http.port:8080}"/>
            <socket-binding name="https" port="${jboss.https.port:8443}"/>
            <socket-binding name="management-http" interface="management"
                           port="${jboss.management.http.port:9990}"/>
            <socket-binding name="management-https" interface="management"
                           port="${jboss.management.https.port:9993}"/>
            <socket-binding name="jgroups-tcp" port="7600"/>
            <socket-binding name="jgroups-tcp-fd" port="57600"/>
            <socket-binding name="modcluster" port="23364" multicast-port="23364"/>
        </socket-binding-group>
    </socket-binding-groups>

</domain>
```

### 5. 도메인 모드 시작

#### Master (Domain Controller) 시작
```bash
# Master 서버에서 실행
cd $JBOSS_HOME/bin

# host-master.xml 사용
./domain.sh --host-config=host-master.xml

# 백그라운드 실행
nohup ./domain.sh --host-config=host-master.xml > /dev/null 2>&1 &
```

#### Slave (Host Controller) 시작
```bash
# Node1 서버에서 실행
cd $JBOSS_HOME/bin

# host-slave.xml 사용
./domain.sh --host-config=host-slave.xml

# 호스트 이름 지정
./domain.sh --host-config=host-slave.xml -Djboss.host.name=node1

# 백그라운드 실행
nohup ./domain.sh --host-config=host-slave.xml -Djboss.host.name=node1 > /dev/null 2>&1 &
```

```bash
# Node2 서버에서 실행
cd $JBOSS_HOME/bin

./domain.sh --host-config=host-slave.xml -Djboss.host.name=node2

# 백그라운드 실행
nohup ./domain.sh --host-config=host-slave.xml -Djboss.host.name=node2 > /dev/null 2>&1 &
```

### 6. 애플리케이션 배포 (도메인 모드)

#### CLI를 통한 배포
```bash
# Master 서버의 CLI 접속
cd $JBOSS_HOME/bin
./jboss-cli.sh --connect --controller=192.168.1.100:9990

# 서버 그룹에 배포 (모든 노드에 자동 배포됨)
deploy /path/to/pubc-test-api.war --server-groups=pubc-api-servers

# 배포 확인
deployment-info --server-group=pubc-api-servers

# 특정 서버 그룹에만 배포 활성화
deployment enable pubc-test-api.war --server-groups=pubc-api-servers

# 배포 해제
undeploy pubc-test-api.war --server-groups=pubc-api-servers

# CLI 종료
quit
```

#### 관리 콘솔을 통한 배포
1. Master 서버 관리 콘솔 접속: `http://192.168.1.100:9990/console`
2. **Runtime** → **Server Groups** → **pubc-api-servers** 선택
3. **Deployments** 탭 이동
4. **Add** 버튼 클릭
5. WAR 파일 업로드
6. 대상 서버 그룹 선택 (pubc-api-servers)
7. **Enable** 클릭

### 7. 로드 밸런서 설정 (Apache httpd mod_cluster)

#### mod_cluster 설정
```xml
<!-- domain.xml의 modcluster 서브시스템 -->
<subsystem xmlns="urn:jboss:domain:modcluster:6.0">
    <proxy name="default"
           advertise-socket="modcluster"
           listener="ajp"
           proxies="proxy1">
        <dynamic-load-provider>
            <load-metric type="cpu"/>
        </dynamic-load-provider>
    </proxy>
</subsystem>
```

#### Apache httpd 설정 (mod_cluster 사용)
```apache
# httpd.conf

LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_ajp_module modules/mod_proxy_ajp.so
LoadModule slotmem_module modules/mod_slotmem.so
LoadModule manager_module modules/mod_manager.so
LoadModule proxy_cluster_module modules/mod_proxy_cluster.so
LoadModule advertise_module modules/mod_advertise.so

Listen 80

<VirtualHost *:80>
    ServerName api.example.com

    # mod_cluster 관리 페이지
    <Location /mod_cluster_manager>
        SetHandler mod_cluster-manager
        Order deny,allow
        Deny from all
        Allow from 192.168.1.0/24
    </Location>

    # API 프록시
    ProxyPass /pubc-test-api balancer://pubc-api-cluster stickysession=JSESSIONID|jsessionid
    ProxyPassReverse /pubc-test-api balancer://pubc-api-cluster

    # JBoss 노드 등록 (수동)
    # Balancer 설정
    <Proxy balancer://pubc-api-cluster>
        BalancerMember http://192.168.1.101:8080 route=pubc-api-server1
        BalancerMember http://192.168.1.101:8180 route=pubc-api-server2
        BalancerMember http://192.168.1.102:8080 route=pubc-api-server3
        BalancerMember http://192.168.1.102:8180 route=pubc-api-server4
        ProxySet lbmethod=byrequests
    </Proxy>
</VirtualHost>
```

### 8. 세션 클러스터링 설정

#### web.xml 설정 (프로젝트)
```xml
<!-- WebContent/WEB-INF/web.xml -->
<web-app>
    <!-- 세션 복제 활성화 -->
    <distributable/>

    <session-config>
        <session-timeout>30</session-timeout>
        <cookie-config>
            <http-only>true</http-only>
        </cookie-config>
    </session-config>
</web-app>
```

#### jboss-web.xml 설정 (세션 복제)
```xml
<!-- WebContent/WEB-INF/jboss-web.xml -->
<jboss-web>
    <context-root>/pubc-test-api</context-root>

    <!-- 세션 복제 설정 -->
    <replication-config>
        <replication-trigger>SET_AND_NON_PRIMITIVE_GET</replication-trigger>
        <replication-granularity>SESSION</replication-granularity>
    </replication-config>
</jboss-web>
```

### 9. 클러스터 모니터링

#### CLI 모니터링 명령
```bash
# 도메인 전체 상태 확인
/host=*/server=*:read-resource(include-runtime=true)

# 특정 서버 그룹 상태
/server-group=pubc-api-servers:read-resource(include-runtime=true)

# 특정 서버 상태
/host=node1/server=pubc-api-server1:read-resource(include-runtime=true)

# 배포 상태
/server-group=pubc-api-servers/deployment=pubc-test-api.war:read-resource(include-runtime=true)

# 데이터소스 통계 (특정 서버)
/host=node1/server=pubc-api-server1/subsystem=datasources/data-source=PubcTestDS/statistics=pool:read-resource(include-runtime=true)

# 세션 클러스터 정보
/profile=pubc-api-profile/subsystem=infinispan/cache-container=web:read-resource(include-runtime=true)
```

#### JMX 모니터링
```bash
# domain.conf에 JMX 설정 추가
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.port=9999"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
```

### 10. 클러스터 운영 시나리오

#### 서버 추가 (스케일 아웃)
```bash
# CLI 접속
./jboss-cli.sh --connect --controller=192.168.1.100:9990

# 새 서버 추가 (host-slave.xml 수정 후)
/host=node1/server-config=pubc-api-server5:add(group=pubc-api-servers, socket-binding-port-offset=200)

# 서버 시작
/host=node1/server-config=pubc-api-server5:start
```

#### 무중단 배포 (Rolling Update)
```bash
# 1단계: 서버 그룹의 일부 서버만 중지
/host=node1/server-config=pubc-api-server1:stop

# 2단계: 새 버전 배포 (stopped 서버에만)
deploy /path/to/pubc-test-api-v2.war --server-groups=pubc-api-servers

# 3단계: 중지된 서버 재시작
/host=node1/server-config=pubc-api-server1:start

# 4단계: 나머지 서버 순차적으로 반복
```

#### 서버 제거 (스케일 인)
```bash
# 서버 중지
/host=node1/server-config=pubc-api-server2:stop

# 서버 제거
/host=node1/server-config=pubc-api-server2:remove
```

### 11. 클러스터 트러블슈팅

#### 클러스터 연결 실패
```bash
# JGroups 로그 확인
tail -f $JBOSS_HOME/domain/servers/pubc-api-server1/log/server.log | grep JGRP

# 방화벽 확인 (필요 포트)
# - 9999: 도메인 컨트롤러 관리 포트
# - 7600: JGroups TCP
# - 23364: mod_cluster
firewall-cmd --list-ports
```

#### 세션 복제 문제
```bash
# Infinispan 통계 확인
/profile=pubc-api-profile/subsystem=infinispan/cache-container=web/distributed-cache=dist:read-resource(include-runtime=true,recursive=true)

# 세션 수 확인
/host=node1/server=pubc-api-server1/deployment=pubc-test-api.war/subsystem=undertow:read-resource(include-runtime=true)
```

#### 로드 밸런싱 불균형
```bash
# mod_cluster 상태 확인
curl http://192.168.1.100/mod_cluster_manager

# 서버별 요청 수 확인
/host=*/server=*/subsystem=undertow:read-attribute(name=request-count)
```

### 12. 접근 URL (도메인 모드)

#### 관리 콘솔
- Master: `http://192.168.1.100:9990/console`

#### API 엔드포인트 (직접 접근)
- Node1-Server1: `http://192.168.1.101:8080/pubc-test-api/api/`
- Node1-Server2: `http://192.168.1.101:8180/pubc-test-api/api/`
- Node2-Server3: `http://192.168.1.102:8080/pubc-test-api/api/`
- Node2-Server4: `http://192.168.1.102:8180/pubc-test-api/api/`

#### 로드 밸런서를 통한 접근
- Load Balancer: `http://api.example.com/pubc-test-api/api/`

### 13. 도메인 모드 장점

- ✅ **중앙 관리**: 단일 관리 콘솔에서 모든 서버 제어
- ✅ **일관성**: 모든 노드에 동일한 설정 자동 배포
- ✅ **확장성**: 새 노드 추가/제거 용이
- ✅ **무중단 배포**: Rolling Update 지원
- ✅ **세션 복제**: 고가용성 (HA) 보장
- ✅ **로드 밸런싱**: 자동 부하 분산

---

## Git 형상관리 정책

### 브랜치 전략 (Git Flow)

#### 주요 브랜치
- **main** (또는 master): 운영 배포 브랜치
  - 안정적인 프로덕션 코드만 포함
  - 태그를 통한 버전 관리 (v1.0.0, v1.1.0 등)
  - 직접 커밋 금지, PR을 통한 병합만 허용

- **develop**: 개발 통합 브랜치
  - 다음 릴리스를 위한 개발 코드 통합
  - 기능 개발 완료 후 이 브랜치로 병합
  - CI/CD 자동 빌드 및 테스트 실행

#### 보조 브랜치
- **feature/기능명**: 기능 개발 브랜치
  - 명명 규칙: `feature/facility-api`, `feature/auth-enhancement`
  - develop 브랜치에서 분기
  - 개발 완료 후 develop으로 병합 후 삭제

- **hotfix/이슈명**: 긴급 수정 브랜치
  - 명명 규칙: `hotfix/critical-bug-fix`, `hotfix/security-patch`
  - main 브랜치에서 분기
  - 수정 완료 후 main과 develop 양쪽에 병합

- **release/버전**: 릴리스 준비 브랜치
  - 명명 규칙: `release/v1.0.0`, `release/v1.1.0`
  - develop 브랜치에서 분기
  - 버그 수정 및 릴리스 준비 작업 수행
  - 완료 후 main과 develop에 병합

### 커밋 메시지 규칙

#### 커밋 메시지 형식
```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type 종류
- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **docs**: 문서 수정 (코드 변경 없음)
- **style**: 코드 포맷팅, 세미콜론 누락 등 (기능 변경 없음)
- **refactor**: 코드 리팩토링
- **test**: 테스트 코드 추가/수정
- **chore**: 빌드 프로세스, 라이브러리 업데이트 등

#### Scope 예시
- `facility`: 문화시설 관련
- `auth`: PUBC 인증 관련
- `logging`: 로깅 관련
- `api`: REST/SOAP API 관련
- `db`: 데이터베이스 관련

#### 예시
```
feat(facility): 문화시설 목록 조회 API 구현

- CultureFacilityRestController 추가
- facilityType, regionCode 필터링 지원
- 페이징 처리 구현 (pageNo, numOfRows)

Closes #123
```

```
fix(auth): PUBC 서비스키 인증 오류 수정

- CommonProc.authServiceCall() null 체크 추가
- UserCrtfcVO 초기화 로직 개선

Fixes #456
```

```
docs(readme): CLAUDE.md에 Git 정책 추가

- 브랜치 전략 문서화
- 커밋 메시지 규칙 추가
- GitHub 워크플로우 가이드 작성
```

### .gitignore 설정

프로젝트 루트에 `.gitignore` 파일 생성:

```gitignore
# Compiled class files
*.class
target/
build/

# Log files
*.log
logs/

# IDE
.idea/
*.iml
.vscode/
.classpath
.project
.settings/
*.swp
*.swo

# Maven
.mvn/
mvn.cmd
mvnw
mvnw.cmd

# Tomcat
MANIFEST.MF

# OS
.DS_Store
Thumbs.db

# PUBC 라이브러리 (보안 고려 - 선택적)
# lib/iros_pubc_1.1.jar
# lib/iros_cipher.jar

# 환경별 설정 파일 (민감 정보 포함 시)
*-prod.properties
*-dev.properties
database.properties

# WAR 파일 (빌드 산출물)
*.war
*.ear

# 임시 파일
*.tmp
*.bak
*~
```

### Git 워크플로우

#### 1. 새 기능 개발
```bash
# develop 브랜치로 전환 및 최신화
git checkout develop
git pull origin develop

# feature 브랜치 생성
git checkout -b feature/facility-search

# 코드 작성 및 커밋
git add .
git commit -m "feat(facility): 시설명 검색 기능 추가"

# 원격 저장소에 푸시
git push origin feature/facility-search

# GitHub에서 Pull Request 생성 (develop ← feature/facility-search)
```

#### 2. 긴급 버그 수정
```bash
# main 브랜치에서 hotfix 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/auth-error

# 버그 수정 및 커밋
git add .
git commit -m "fix(auth): 인증 실패 시 NPE 오류 수정"

# 원격 저장소에 푸시
git push origin hotfix/auth-error

# GitHub에서 Pull Request 생성 (main ← hotfix/auth-error)
# 병합 후 develop에도 체리픽 또는 별도 PR
```

#### 3. 릴리스 배포
```bash
# develop에서 release 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0

# 버전 업데이트 (pom.xml 등)
# 버그 수정 및 테스트

git add .
git commit -m "chore(release): v1.0.0 릴리스 준비"

# 원격 저장소에 푸시
git push origin release/v1.0.0

# GitHub에서 Pull Request 생성 (main ← release/v1.0.0)
# 병합 후 태그 생성
git checkout main
git pull origin main
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# develop에도 병합
git checkout develop
git merge release/v1.0.0
git push origin develop
```

---

## GitHub 원격 리파지토리 정책

### 리파지토리 설정

#### 리파지토리 생성
```bash
# GitHub에서 새 리파지토리 생성: pubc-test-api

# 로컬 저장소 초기화
git init
git add .
git commit -m "chore(init): 프로젝트 초기 설정"

# 원격 저장소 연결
git remote add origin https://github.com/your-org/pubc-test-api.git
git branch -M main
git push -u origin main

# develop 브랜치 생성 및 푸시
git checkout -b develop
git push -u origin develop
```

#### 브랜치 보호 규칙 (Branch Protection Rules)

**main 브랜치 설정** (GitHub 리파지토리 Settings → Branches):
- ✅ Require a pull request before merging
  - Require approvals: 최소 1명 승인 필요
- ✅ Require status checks to pass before merging
  - Maven Build
  - Unit Tests
- ✅ Require conversation resolution before merging
- ✅ Do not allow bypassing the above settings (관리자도 준수)
- ✅ Restrict who can push to matching branches (직접 푸시 금지)

**develop 브랜치 설정**:
- ✅ Require a pull request before merging
  - Require approvals: 최소 1명 승인 필요
- ✅ Require status checks to pass before merging

### Pull Request (PR) 가이드

#### PR 템플릿 (.github/pull_request_template.md)
```markdown
## 변경 사항
<!-- 무엇을 변경했는지 간략히 설명 -->

## 변경 이유
<!-- 왜 이 변경이 필요한지 설명 -->

## 관련 이슈
<!-- Closes #이슈번호 -->

## 테스트 방법
- [ ] 단위 테스트 작성 및 통과
- [ ] 통합 테스트 수행
- [ ] 로컬 환경에서 동작 확인

## 체크리스트
- [ ] 코드가 프로젝트 컨벤션을 준수함
- [ ] 주석 및 문서 업데이트
- [ ] PUBC 인증/로깅 정상 동작 확인
- [ ] 조회 전용 API 원칙 준수 (POST/PUT/DELETE 없음)
- [ ] 보안 취약점 검토 완료

## 스크린샷 (필요 시)
<!-- API 테스트 결과, 로그 등 -->
```

#### PR 리뷰 규칙
1. **리뷰어 지정**: 최소 1명 이상 (팀 리드 또는 시니어 개발자)
2. **필수 확인 사항**:
   - 코드 품질 및 가독성
   - PUBC 통합 정상 동작
   - 보안 취약점 (SQL Injection, XSS 등)
   - 조회 전용 원칙 준수
   - 테스트 커버리지
3. **승인 후 병합**: Squash and merge 또는 Merge commit 사용
4. **병합 후 브랜치 삭제**: feature 브랜치는 병합 후 자동 삭제

### GitHub Actions (CI/CD)

#### 워크플로우 예시 (.github/workflows/maven-build.yml)
```yaml
name: Maven Build and Test

on:
  push:
    branches: [ develop, main ]
  pull_request:
    branches: [ develop, main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 1.8
      uses: actions/setup-java@v3
      with:
        java-version: '8'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Build with Maven
      run: mvn clean package -DskipTests

    - name: Run Unit Tests
      run: mvn test

    - name: Upload WAR artifact
      uses: actions/upload-artifact@v3
      with:
        name: pubc-test-api
        path: target/*.war
```

### 이슈 관리

#### 이슈 템플릿 (.github/ISSUE_TEMPLATE/bug_report.md)
```markdown
---
name: 버그 리포트
about: 버그를 발견하셨나요? 알려주세요!
---

## 버그 설명
<!-- 버그에 대한 명확한 설명 -->

## 재현 방법
1. ...
2. ...

## 예상 동작
<!-- 어떻게 동작해야 하는지 -->

## 실제 동작
<!-- 실제로 어떻게 동작하는지 -->

## 환경
- OS: [예: Windows 10, macOS]
- JDK: [예: 1.8.0_202]
- Tomcat: [예: 8.5.90]
- 브라우저: [예: Chrome 120]

## 추가 정보
<!-- 로그, 스크린샷 등 -->
```

#### 이슈 라벨
- `bug`: 버그
- `enhancement`: 기능 개선
- `feature`: 새 기능
- `documentation`: 문서
- `pubc`: PUBC 관련
- `security`: 보안
- `priority-high`: 높은 우선순위
- `priority-low`: 낮은 우선순위

### 릴리스 관리

#### 릴리스 노트 작성
- GitHub Releases 기능 활용
- 태그 기반 릴리스 생성 (v1.0.0, v1.1.0)
- 주요 변경사항, 버그 수정, 알려진 이슈 명시
- WAR 파일 첨부

#### 시맨틱 버저닝 (Semantic Versioning)
- **Major.Minor.Patch** (예: 1.0.0)
- **Major**: 호환되지 않는 API 변경
- **Minor**: 하위 호환되는 기능 추가
- **Patch**: 하위 호환되는 버그 수정

### 협업 규칙

1. **커밋 전**: 항상 최신 코드를 pull 받기
2. **커밋 단위**: 의미 있는 단위로 작은 커밋 유지
3. **푸시 전**: 로컬에서 빌드 및 테스트 성공 확인
4. **코드 리뷰**: 모든 PR은 리뷰 필수
5. **충돌 해결**: 병합 충돌 발생 시 신중히 해결
6. **문서 업데이트**: 코드 변경 시 관련 문서도 함께 업데이트

### 보안 고려사항

#### 민감 정보 관리
- ❌ **절대 커밋 금지**:
  - 서비스키, API 키
  - 데이터베이스 비밀번호
  - 암호화 키, 인증서
  - 운영 서버 정보

- ✅ **권장 방법**:
  - `.gitignore`에 설정 파일 추가
  - 환경 변수 또는 외부 설정 파일 사용
  - GitHub Secrets를 통한 CI/CD 환경 변수 관리

#### PUBC 라이브러리 관리
- `lib/iros_pubc_1.1.jar`: **보안 정책에 따라 선택적으로 제외**
  - 공개 저장소: `.gitignore`에 추가
  - 비공개 저장소: 커밋 가능 (팀 내부 공유)
  - 대안: 별도의 내부 Maven Repository 사용

---

## 주요 문서

### 프로젝트 문서
- **README.md**: 프로젝트 전체 개요 (루트 경로)
- **docs/01_요구사항정의서.md**: 기능/비기능 요구사항
- **docs/02_시스템아키텍처설계서.md**: 7계층 아키텍처, 기술 스택
- **docs/03_API명세서.md**: REST/SOAP API 상세 명세
- **docs/04_데이터베이스스키마설계서.md**: ERD, DDL, 인덱스
- **docs/05_클래스다이어그램및패키지구조.md**: 패키지 구조, 클래스 다이어그램
- **docs/06_JDK18기술스택및호환성분석.md**: JDK 1.8 호환성, pom.xml

### 외부 문서 (참고용)
- **PUBC.md**: PUBC 공통연계모듈 상세 분석
- **OASIS.md**: OpenAPI 자동생성툴 상세 분석

---

## 문의

프로젝트 관련 문의사항은 프로젝트 담당자에게 연락해주세요.

**작성일**: 2025-11-06
**버전**: 2.0 (JDK 1.8)
