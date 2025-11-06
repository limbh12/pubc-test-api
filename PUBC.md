# PUBC 소스코드 상세 분석

공공데이터 포털 OpenAPI 게이트웨이 공통연계모듈(PUBC) 소스코드에 대한 상세 분석 문서입니다.

## 프로젝트 개요

- **프로젝트명**: pubc
- **타입**: Eclipse Dynamic Web Project
- **Java 버전**:
  - **컴파일 타겟**: Java 5 (1.5) - Maven `source/target=1.5`
  - **개발 환경**: JDK 1.6.0_45 - Eclipse 프로젝트 설정
  - **설명**: JDK 6으로 개발하되 Java 5 수준 문법만 사용하여 하위 호환성 보장
- **WAS**: Apache Tomcat 6.0
- **마지막 수정**: 2018년 2월
- **위치**: `(대외비) 05.공통연계모듈(PUBC)/pubc_소스/`

## 소스코드 통계

- **PUBC 모듈**: 62개 Java 파일
- **PUBR 모듈**: 12개 Java 파일
- **지원 DBMS**: 11종
- **라이브러리**: 136개 JAR 파일

## 패키지 구조

```
src/iros/
├── cmm/                              # 공통 기능 레이어
│   ├── web/
│   │   ├── BaseController.java      # 모든 컨트롤러의 부모 클래스
│   │   └── CommonCodeController.java
│   ├── service/
│   │   ├── CommonVO.java
│   │   ├── LoginVO.java
│   │   ├── SessionVO.java
│   │   └── AuthorVO.java
│   ├── interceptor/
│   │   └── LoginCheckInterceptor.java
│   ├── PubcUtil.java                 # JSON/XML 변환, 날짜 처리
│   ├── IrosUtil.java
│   ├── IrosFileScrty.java
│   ├── RsaFunction.java
│   ├── CommonMessageSource.java
│   └── IrosExceptionHandler.java
│
├── pubc/                             # PUBC 관리 모듈 (62 files)
│   ├── cmm/                          # 공통
│   │   ├── web/
│   │   │   └── CMPubcLoginController.java
│   │   ├── service/
│   │   │   ├── CMPubcLoginVO.java
│   │   │   ├── EsbCntcInterface.java
│   │   │   └── impl/
│   │   │       ├── CMPubcLoginServiceImpl.java
│   │   │       └── OracleAbstractDAO.java
│   │   └── PubcProperties.java
│   │
│   ├── mng/                          # 관리 (Management)
│   │   └── web/
│   │       ├── MGPrcuseLmttController.java      # 이용 제한 관리
│   │       └── MGSysAttrbController.java        # 시스템 속성 관리
│   │
│   ├── mnt/                          # 모니터링 (Monitoring)
│   │   ├── web/
│   │   │   ├── MTSvcTroblController.java        # 서비스 장애 모니터링
│   │   │   ├── MTSvcProvdSttusController.java   # 서비스 제공 현황
│   │   │   └── MTSystemMonController.java       # 시스템 모니터링
│   │   └── service/
│   │       ├── MTSvcTroblService.java
│   │       ├── MTSvcTroblVO.java
│   │       └── impl/
│   │
│   ├── rqs/                          # 요청 관리 (Request)
│   │   ├── web/
│   │   │   ├── RQPrcuseReqstMngController.java  # 이용 요청 관리
│   │   │   ├── RQSvcPrcuseController.java       # 서비스 이용 관리
│   │   │   ├── RQDataPrcuseController.java      # 데이터 이용 관리
│   │   │   └── RQReqstDataController.java       # 요청 데이터 관리
│   │   └── service/
│   │       ├── vo/
│   │       └── impl/
│   │
│   ├── qty/                          # 임계값 관리 (Quantity/Threshold)
│   │   ├── web/
│   │   └── service/
│   │       ├── vo/
│   │       └── impl/
│   │
│   └── stt/                          # 통계 (Statistics)
│       ├── web/
│       └── service/
│           ├── vo/
│           └── impl/
│
├── pubr/                             # 포털 연동 모듈 (12 files)
│   ├── cmm/                          # 포털 공통
│   │   ├── web/
│   │   │   ├── CMPubrLoginController.java       # 포털 로그인
│   │   │   └── CMPrcuseCaseController.java      # 활용 사례
│   │   └── service/
│   │       └── CMPubrLoginVO.java
│   │
│   ├── rqs/                          # 포털 요청 관리
│   │   └── web/
│   │       ├── RQDevGuideController.java        # 개발 가이드
│   │       ├── RQPrcuseConfmController.java     # 이용 승인
│   │       └── RQPrcuseReqstController.java     # 이용 요청
│   │
│   └── sta/                          # 포털 통계
│       ├── web/
│       │   ├── STSvcPrcuseController.java       # 서비스 이용
│       │   └── STSvcPrcuseStatsController.java  # 서비스 이용 통계
│       └── service/
│           └── impl/
│               ├── STSvcPrcuseStatsDAO.java
│               └── STSvcPrcuseStatsServiceImpl.java
│
├── sqlmap/                           # iBATIS SQL 매핑
│   ├── config/                       # DBMS별 설정 파일
│   │   ├── iros-pubc-sqlmap-config.xml
│   │   ├── iros-pubc-sqlmap-config-oracle.xml
│   │   ├── iros-pubc-sqlmap-config-sybase.xml
│   │   ├── iros-pubc-sqlmap-config-postgresql.xml
│   │   ├── iros-pubc-sqlmap-config-tibero.xml
│   │   ├── iros-pubc-sqlmap-config-mysql.xml
│   │   ├── iros-pubc-sqlmap-config-mssql.xml
│   │   ├── iros-pubc-sqlmap-config-informix.xml
│   │   ├── iros-pubc-sqlmap-config-cubrid.xml
│   │   ├── iros-pubc-sqlmap-config-altibase.xml
│   │   └── iros-pubc-sqlmap-config-unisql.xml
│   │
│   ├── oracle/pubc/                  # Oracle SQL 매핑
│   │   ├── mnt/, rqs/, qty/, stt/
│   │   └── ...
│   ├── sybase/pubc/                  # Sybase SQL 매핑
│   ├── postgresql/pubc/              # PostgreSQL SQL 매핑
│   ├── tibero/pubc/                  # Tibero SQL 매핑
│   ├── mysql/pubc/                   # MySQL SQL 매핑
│   ├── mssql/pubc/                   # MS SQL Server SQL 매핑
│   ├── informix/pubc/                # Informix SQL 매핑
│   ├── cubrid/pubc/                  # CUBRID SQL 매핑
│   ├── altibase/pubc/                # Altibase SQL 매핑
│   └── unisql/pubc/                  # UniSQL SQL 매핑
│
├── validator/                        # 유효성 검증
│   └── qna/
│
└── message/                          # 다국어 메시지
    ├── message-common_ko_KR.properties
    └── message-common_en_US.properties
```

## 핵심 컴포넌트

### 1. BaseController (공통 컨트롤러)

**위치**: `iros.cmm.web.BaseController`

모든 컨트롤러의 부모 클래스로, 공통 기능을 제공합니다.

#### 세션 관리

```java
// PUBC 관리자 세션
public CMPubcLoginVO getPubcLoginVO(HttpServletRequest request)
public void setPubcLoginVO(CMPubcLoginVO pubcLoginVO, HttpServletRequest request)

// PUBR 포털 사용자 세션
public CMPubrLoginVO getPubrLoginVO(HttpServletRequest request)
public void setPubrLoginVO(CMPubrLoginVO pubrLoginVO, HttpServletRequest request)

// 세션 메시지
public void setPubcSessionMsg(String msg, HttpServletRequest request)
public void setPubrSessionMsg(String msg, HttpServletRequest request)

// 메뉴 ID 설정
public void setMenuId(String menuId, HttpServletRequest request)
```

#### ESB 연동 (Enterprise Service Bus)

```java
/**
 * ESB를 통한 포털 시스템 연동
 * @param ifId 인터페이스 ID (properties에서 로드)
 * @param ifMap 전송할 데이터 맵
 * @return ESB 응답 맵
 */
public Map<String, String> esbCntcSend(String ifId, HashMap<String, String> ifMap)
```

**동작 방식**:
1. `PubcProperties`에서 인터페이스 ID, 기관코드, 대표서비스코드 로드
2. 현재 시간(`yyyyMMddHHmmss`)을 조회 일시로 설정
3. Map을 JSON으로 변환
4. `EsbCntcInterface.esbCntcSend()` 호출
5. 응답 Map 반환

#### ESB 응답 검증

```java
/**
 * ESB 응답 성공 여부 확인
 * @param ret ESB 응답 맵
 * @return 에러 원인 ("" = 성공, 그 외 = 실패 원인)
 */
public String chkEsbSuccess(Map ret)
```

**검증 로직**:
- `Status == "FAIL"` → 연결 실패, `Cause` 반환
- `BIZ_STATUS != "SUCCESS"` → 비즈니스 로직 실패
  - `BIZ_STATUS == "FAIL"` → `BIZ_CAUSE` 반환
  - 그 외 → `BIZ_STATUS` 값 반환
- 모두 성공 → 빈 문자열 반환

#### 유틸리티 메서드

```java
/**
 * 페이징 최대 페이지 계산
 */
public int getMaxPage(int totalCount, int rows)
```

### 2. PubcUtil (유틸리티 클래스)

**위치**: `iros.cmm.PubcUtil`

JSON/XML 변환 및 날짜 처리 유틸리티를 제공합니다.

#### JSON 변환

```java
// Map ↔ JSON
public static String map2json(Map map)
public static Map json2map(String json)

// List ↔ JSON
public String list2json(List list)
public List json2list(String json)
```

**사용 라이브러리**: `net.sf.json.JSONObject`, `net.sf.json.JSONArray`

#### XML 변환

```java
/**
 * 객체를 XML로 변환
 * @param obj 변환할 객체
 * @param rootName 루트 엘리먼트 이름
 * @param type 객체 타입
 */
public static String makeToXml(Object obj, String rootName, Class type)

/**
 * XML을 객체로 변환
 */
public static Object makeFromXml(String xmlStr, String rootName, Class type)
```

**사용 라이브러리**: XStream (UTF-8, XmlFriendlyReplacer)

#### 날짜/시간 처리

```java
/**
 * 현재 시간 조회
 * @param dateFormat "yyyyMMddHHmmss" 등
 */
public static String getCurrentTime(String dateFormat)

/**
 * 이전 날짜 계산
 * @param beforeDateType "MINUTE", "HOUR", "DAY", "WEEK", "MONTH"
 * @param cnd 이전 시간 단위 수
 */
public static String getPreDate(String beforeDateType, int cnd)

/**
 * 응답 시간 계산 (초 단위)
 */
public static double getResponseTime(double starttime, double endtime, String format)

/**
 * 문자열 날짜를 Timestamp로 변환
 * @param stringDate "yyyyMMddhhmmss" 형식
 */
public static Timestamp getTimestamp(String stringDate)
```

#### 데이터 처리

```java
/**
 * ESB 응답에서 특정 키의 데이터 추출
 */
public static Map getDataMap(Map<String, String> ret, String dataKey)

/**
 * 구분자($$, ||)로 구분된 문자열을 리스트로 변환
 */
public static List getDataToken(String str)

/**
 * null을 빈 문자열로 변환
 */
public static String isNullToString(Object object)
```

### 3. 컨트롤러 패턴

#### 표준 컨트롤러 구조

```java
@Controller
public class MTSvcTroblController extends BaseController {

    // JSP 경로
    private String path = "/iros/pubc/mnt/";

    // Service 주입
    @Resource(name="mtSvcTroblService")
    protected MTSvcTroblService mtSvcTroblService;

    /**
     * 뷰 페이지 렌더링
     */
    @RequestMapping("/pubc/mnt/MTSvcTrobl/viewMTSvcTroblList.do")
    public String viewMTSvcTroblList(HttpServletRequest request) {
        setMenuId("3_3", request);  // 메뉴 ID 설정
        return this.path + "MTSvcTroblList";  // JSP 경로 반환
    }

    /**
     * JSON 데이터 조회
     */
    @RequestMapping("/pubc/mnt/MTSvctrobl/listMTSvcTrobl.do")
    public String listMTSvcTrobl(
            Model model,
            @ModelAttribute("MTSvcTroblVO") MTSvcTroblVO vo,
            @RequestParam("svcPk") String svcPk,
            @RequestParam("srchDtFrom") String srchDtFrom,
            @RequestParam("srchDtTo") String srchDtTo,
            HttpServletRequest request) throws Exception {

        // 1. 페이징 정보 설정
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setCurrentPageNo(vo.getPage());
        paginationInfo.setRecordCountPerPage(vo.getRows());

        vo.setFirstIndex(paginationInfo.getFirstRecordIndex());
        vo.setLastIndex(paginationInfo.getLastRecordIndex());
        vo.setSvcPk(svcPk);
        vo.setSrchDtFrom(srchDtFrom.replaceAll("-", ""));
        vo.setSrchDtTo(srchDtTo.replaceAll("-", ""));

        // 2. 데이터 조회
        List listResult = mtSvcTroblService.selectSvcTroblList(vo);
        List count = mtSvcTroblService.selectSvcTroblListTotalCount(vo);

        int totalCount = 0;
        if (count != null && count.size() > 0) {
            totalCount = ((MTSvcTroblVO)count.get(0)).getTotalCount();
        }

        paginationInfo.setTotalRecordCount(totalCount);

        // 3. JSON 모델 구성
        Map jsonModel = new HashMap();
        int maxPage = (int)(totalCount / vo.getRecordCountPerPage() + 0.97D);

        jsonModel.put("page", vo.getPage());
        jsonModel.put("total", maxPage);
        jsonModel.put("records", totalCount);
        jsonModel.put("rows", listResult);

        model.addAllAttributes(jsonModel);

        // 4. JSON View 반환
        return "jsonView";
    }
}
```

#### ESB 연동 컨트롤러 패턴

```java
@RequestMapping("/pubc/mnt/MTSvctrobl/listOprtinNm.do")
public ModelAndView listOprtinNm(
        @RequestParam("pblonsipResrcePk") String pblonsipResrcePk,
        HttpServletRequest request) {

    ModelAndView mav = new ModelAndView("jsonView");

    // 1. 로그인 사용자 정보 조회
    CMPubcLoginVO pubcUser = getPubcLoginVO(request);

    // 2. ESB 요청 데이터 구성
    HashMap mapInterface = new HashMap();
    mapInterface.put("FIRSTINDEX", String.valueOf(0));
    mapInterface.put("RECORDCOUNTPERPAGE", "10");
    mapInterface.put("PBLONSIP_RESRCE_PK", pblonsipResrcePk);
    mapInterface.put("MBER_ID", pubcUser.getMberId());
    mapInterface.put("MBER_TY", pubcUser.getMberTy());

    // 3. ESB 호출
    Map ret = esbCntcSend("IFCICSMP102", mapInterface);

    // 4. 응답 검증
    String chkRetVal = chkEsbSuccess(ret);

    if ("".equals(chkRetVal)) {
        // 성공 처리
        String retStr = (String)ret.get("Result");
        Map resultMap = PubcUtil.json2map(retStr);
        // ...
    } else {
        // 실패 처리
        mav.addObject("message", chkRetVal);
    }

    return mav;
}
```

## 다중 DBMS 지원

### 지원 데이터베이스 (11종)

| DBMS | 설정 파일 | 용도 |
|------|----------|------|
| **Oracle** | `iros-pubc-sqlmap-config-oracle.xml` | 오라클 데이터베이스 |
| **Sybase** | `iros-pubc-sqlmap-config-sybase.xml` | 사이베이스 (기본) |
| **PostgreSQL** | `iros-pubc-sqlmap-config-postgresql.xml` | 포스트그레SQL |
| **Tibero** | `iros-pubc-sqlmap-config-tibero.xml` | 티베로 (국산 DBMS) |
| **MySQL** | `iros-pubc-sqlmap-config-mysql.xml` | MySQL |
| **MS SQL Server** | `iros-pubc-sqlmap-config-mssql.xml` | MS SQL 서버 |
| **Informix** | `iros-pubc-sqlmap-config-informix.xml` | IBM Informix |
| **CUBRID** | `iros-pubc-sqlmap-config-cubrid.xml` | 큐브리드 (국산 DBMS) |
| **Altibase** | `iros-pubc-sqlmap-config-altibase.xml` | 알티베이스 (국산 DBMS) |
| **UniSQL** | `iros-pubc-sqlmap-config-unisql.xml` | 유니SQL (국산 DBMS) |
| **SQL** | `iros-pubc-sqlmap-config.xml` | 공통 SQL |

### iBATIS SQL Map 구조

```
sqlmap/{dbms}/
├── pubc/
│   ├── mnt/                  # 모니터링 SQL
│   │   └── MTSvcTroblSQL.xml
│   ├── rqs/                  # 요청 관리 SQL
│   │   └── RQSvcPrcuseSQL.xml
│   ├── qty/                  # 임계값 SQL
│   │   ├── QTThrhldMngSQL.xml
│   │   └── QTThrhldSttusSQL.xml
│   └── stt/                  # 통계 SQL
│       ├── STProvdStatsSQL.xml
│       └── STSvcTroblStatsSQL.xml
└── pubr/
    └── sta/                  # 포털 통계 SQL
```

## 웹 애플리케이션 구조

### WebContent 디렉토리

```
WebContent/
├── css/                      # 스타일시트
├── images/                   # 이미지 리소스
├── js/                       # JavaScript 라이브러리
├── upload/                   # 파일 업로드 디렉토리
├── META-INF/
│   └── maven/iros-pubc/pubc/
│       └── pom.properties
├── WEB-INF/
│   ├── jsp/                  # JSP 뷰 파일
│   │   └── iros/
│   │       ├── pubc/         # PUBC 관리 화면
│   │       └── pubr/         # 포털 사용자 화면
│   ├── lib/                  # JAR 라이브러리 (136개)
│   ├── config/
│   │   └── springmvc/
│   │       └── dispatcher-servlet.xml
│   ├── web.xml               # 웹 애플리케이션 설정
│   ├── jeus-web-dd.xml       # JEUS WAS 설정
│   ├── views.xml             # 뷰 리졸버 설정
│   ├── log4j.xml             # 로깅 설정
│   ├── sitemesh.xml          # SiteMesh 데코레이터
│   └── decorators.xml        # 레이아웃 템플릿
├── index.jsp                 # 메인 페이지
├── manage.jsp                # 관리 페이지 진입점
├── error.jsp                 # 에러 페이지
├── pubcRedirect.jsp          # PUBC 관리자 리다이렉트
├── pubrRedirect.jsp          # 포털 사용자 리다이렉트
└── potalRedirect.jsp         # 포털 리다이렉트
```

### Spring 설정

#### dispatcher-servlet.xml
- Component Scan: `<context:component-scan base-package="iros">`
- Annotation-driven MVC
- LoginCheckInterceptor 등록
- View Resolvers:
  - XmlViewResolver (JSON View)
  - InternalResourceViewResolver (JSP)

### SiteMesh 데코레이터

레이아웃 템플릿 엔진으로 SiteMesh 사용:
- `sitemesh.xml`: SiteMesh 설정
- `decorators.xml`: 데코레이터 매핑

## 기술 스택

### 백엔드

| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | JDK 1.6 | 개발 언어 |
| **Spring Framework** | 2.5.6 | MVC, IoC, AOP |
| **iBATIS** | 2.3.4 | SQL 매핑 (MyBatis 이전 버전) |
| **eGovFramework** | 1.0.0 | 전자정부 표준 프레임워크 |
| **Apache CXF** | 2.2.9 | 웹 서비스 (REST/SOAP) |

### 라이브러리

| 라이브러리 | 용도 |
|-----------|------|
| **net.sf.json** | JSON 처리 (JSONObject, JSONArray) |
| **XStream** | XML 직렬화/역직렬화 |
| **Commons Collections** | 컬렉션 유틸리티 |
| **Commons Lang** | 문자열, 날짜 유틸리티 |
| **Commons IO** | 파일 I/O |
| **Commons FileUpload** | 파일 업로드 |
| **Log4j** | 로깅 |
| **SiteMesh** | 레이아웃 데코레이터 |
| **Quartz** | 스케줄러 |

### 프론트엔드

- **JSP**: 뷰 템플릿
- **JSTL**: JSP 태그 라이브러리
- **SiteMesh**: 레이아웃 관리
- **JavaScript**: 클라이언트 스크립트

## 이중 사용자 시스템

### PUBC 사용자 (관리자)

**클래스**: `CMPubcLoginVO`

시스템 관리자용 세션으로, 다음 기능에 접근:
- 서비스 장애 모니터링
- 서비스 제공 현황
- 시스템 모니터링
- 이용 제한 관리
- 시스템 속성 관리
- 통계

**세션 키**: `"pubcUser"`

### PUBR 사용자 (포털)

**클래스**: `CMPubrLoginVO`

포털 사용자용 세션으로, 다음 기능에 접근:
- 개발 가이드 조회
- 서비스 이용 요청
- 이용 승인 관리
- 서비스 이용 통계
- 활용 사례

**세션 키**: `"pubrUser"`

**회원 유형**:
- `MBTY06`: 기관 사용자 유형 1
- `MBTY07`: 기관 사용자 유형 2
- `MBTY08`: 기관 사용자 유형 3

## ESB 연동 아키텍처

### ESB의 역할과 사용 목적

**ESB (Enterprise Service Bus)**는 PUBC 관리 모듈이 **공공데이터 포털 시스템과 데이터를 교환**할 때 사용하는 통합 인터페이스입니다.

#### 시스템 구조

```
┌─────────────────────────────────┐
│  공공데이터 포털 (중앙 시스템)  │
│  ├── 서비스 메타데이터          │
│  ├── 회원 정보                  │
│  ├── 이용 신청/승인             │
│  └── 중앙 통계 DB               │
└─────────────────────────────────┘
                ↕ ESB 통신
┌─────────────────────────────────┐
│  제공기관 A (PUBC)              │
│  ├── API 인증 (로컬 DB)         │
│  ├── 사용 현황 로깅 (로컬 DB)   │
│  └── 관리 웹 UI                 │
└─────────────────────────────────┘
```

**핵심 개념**:
- **포털 DB**: 서비스 목록, 이용 신청, 회원 정보 등 중앙 집중식 데이터
- **제공기관 DB**: API 인증 로그, 사용 현황, 장애 정보 등 분산 데이터
- **ESB**: 두 시스템 간 안전하고 표준화된 통신 채널

#### ESB를 사용하는 이유

1. **보안**: 직접 DB 접근 금지, ESB를 통한 통제된 접근
2. **표준화**: 모든 제공기관이 동일한 인터페이스 사용
3. **느슨한 결합**: 포털과 제공기관 간 독립적 운영
4. **감사 추적**: 모든 데이터 교환을 ESB에서 기록
5. **트랜잭션 관리**: ESB에서 중앙 관리

### ESB를 사용하는 기능

총 **40개 이상**의 ESB 인터페이스가 20개 파일에서 사용됩니다.

#### 1. 로그인/인증 (CMPubcLoginController, CMPubrLoginController)

| 인터페이스 ID | 용도 | 컨트롤러 |
|--------------|------|---------|
| `IFCICSMP001` | 포털 사용자 로그인 인증 | CMPubrLoginController |
| `IFCICSMP031` | PUBC 관리자 로그인 인증 | CMPubcLoginController |

#### 2. 서비스 관리 (RQProvdSvcController, RQPrcuseReqstController)

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP101` | 제공 서비스 목록 조회 |
| `IFCICSMP102` | 서비스 상세 정보 조회 |
| `IFCICSMP110` | 테스트 이용 신청 목록 |

#### 3. 이용 신청/승인 (RQPrcuseReqstMngController, RQPrcuseConfmController)

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP002` | 이용 요청 목록 조회 |
| `IFCICSMP003` | 테스트 신청 승인/반려 |
| `IFCICSMP004` | 이용 반려 |
| `IFCICSMP005` | 이용 보류 |
| `IFCICSMP006` | 이용 요청 상세 조회 |
| `IFCICSMP007` | 승인 처리 |
| `IFCICSMP103` | 테스트 이용 신청 |
| `IFCICSMP106` | 승인 대상 목록 조회 |
| `IFCICSMP107` | 서비스 키 목록 조회 |
| `IFCICSMP108` | 서비스 키 상세 조회 |
| `IFCICSMP109` | 중복 신청 확인 |
| `IFCICSMP111` | 서비스 키 생성 |
| `IFCICSMP112` | 승인 처리 |
| `IFCICSMP119` | 서비스 키 삭제 |
| `IFCICSMP120` | 서비스 키 수정 |

#### 4. 이용 제한 관리 (MGPrcuseLmttController)

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP009` | 이용 제한 목록 조회 |
| `IFCICSMP010` | 차단 대상 조회 |
| `IFCICSMP011` | 차단 정보 등록 |
| `IFCICSMP012` | 차단 정보 수정 |
| `IFCICSMP013` | 차단 정보 삭제 |
| `IFCICSMP014` | 차단 사유 정보 조회 |
| `IFCICSMP015` | 차단 사유 수정 |
| `IFCICSMP016` | 화이트리스트 목록 조회 |
| `IFCICSMP017` | 화이트리스트 등록 |
| `IFCICSMP018` | 화이트리스트 삭제 |

#### 5. 통계 (STSvcPrcuseController, STReqstConfmStatsController)

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP115` | 요청/승인 통계 (기간별) |
| `IFCICSMP116` | 요청/승인 통계 (서비스별) |
| `IFCICSMP117` | 요청/승인 통계 (기관별) |
| `IFCICSMP118` | 기간별 트래픽 통계 |
| `IFCICSMP129` | 일별 트래픽 통계 |

#### 6. 파일 관리

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP121` | 첨부파일 업로드 (분할 전송) |
| `IFCICSMP122` | 파일 다운로드 |

#### 7. 기타 관리 기능

| 인터페이스 ID | 용도 |
|--------------|------|
| `IFCICSMP008` | 공통코드 조회 |
| `IFCICSMP032` | 기관 정보 조회 |
| `IFCICSMP033` | 담당자 정보 조회 |
| `IFCICSMP113` | 서비스 제공 현황 조회 |
| `IFCICSMP123` | 평가 정보 등록 |
| `IFCICSMP124` | 활용 사례 조회 |
| `IFCICSMP125` | 개발 가이드 목록 조회 |
| `IFCICSMP126` | 유효기간 목록 조회 |
| `IFCICSMP127` | 유효기간 연장 |

### ESB를 사용하지 않는 기능

**중요**: 다음 핵심 기능들은 ESB 없이 PUBC 로컬 DB에서 처리합니다:

| 기능 | ESB 사용 여부 | 처리 방식 |
|-----|--------------|----------|
| **OpenAPI 인증** | ✗ 불필요 | 로컬 DB (UserCrtfcProcessService) |
| **사용 현황 로깅** | ✗ 불필요 | 로컬 DB (UseSttusService) |
| **장애 정보 기록** | ✗ 불필요 | 로컬 DB |
| **임계값 모니터링** | ✗ 불필요 | 로컬 DB |
| **로컬 통계 집계** | ✗ 불필요 | 로컬 DB |
| 포털 데이터 조회 | ✓ 필요 | ESB 연동 |
| 이용 신청/승인 | ✓ 필요 | ESB 연동 |
| 포털 로그인 인증 | ✓ 필요 | ESB 연동 |

### 연동 흐름

```
[PUBC Controller]
    ↓
    esbCntcSend(ifId, dataMap)
    ↓
[PubcProperties] → 인터페이스 ID, 기관코드, 서비스코드 로드
    ↓
[PubcUtil] → Map을 JSON으로 변환
    ↓
[EsbCntcInterface]
    ↓
    esbCntcSend(interfaceID, encryption, content)
    ↓
[ESB 시스템] ← JSON 전송
    ↓
[포털 시스템]
    ↓
[ESB 응답] → Map 형태
    ↓
[chkEsbSuccess()] → 응답 검증
    ↓
[Controller] → 결과 처리
```

### BaseController ESB 메서드

**위치**: `iros.cmm.web.BaseController`

```java
/**
 * ESB를 통한 포털 시스템 연동
 * @param ifId 인터페이스 ID (properties 키)
 * @param ifMap 전송할 데이터 맵
 * @return ESB 응답 맵
 */
public Map<String, String> esbCntcSend(String ifId, HashMap<String, String> ifMap) {
    // 1. Properties에서 설정 로드
    String interfaceID = PubcProperties.getProperty(ifId);
    String insttCode = PubcProperties.getProperty("instt_code");
    String reprsntSvcCode = PubcProperties.getProperty("reprsnt_svc_code");
    String encryption = PubcProperties.getProperty("encryption");
    String nowDate = PubcUtil.getCurrentTime("yyyyMMddHHmmss");

    // 2. 공통 파라미터 추가
    ifMap.put("INTERFACE_ID", interfaceID);
    ifMap.put("INSTT_CODE", insttCode);
    ifMap.put("INQIRE_DATE", nowDate);
    ifMap.put("REPRSNT_SVC_CODE", reprsntSvcCode);

    // 3. JSON 변환 및 전송
    String content = PubcUtil.map2json(ifMap);
    EsbCntcInterface esbCntcInterface = new EsbCntcInterface();
    Map ret = esbCntcInterface.esbCntcSend(interfaceID, encryption, content);

    return ret;
}

/**
 * ESB 응답 검증
 * @param ret ESB 응답 맵
 * @return 에러 원인 ("" = 성공)
 */
public String chkEsbSuccess(Map ret) {
    String retCause = "";
    if ("FAIL".equals(ret.get("Status"))) {
        retCause = String.valueOf(ret.get("Cause"));
    } else if (!"SUCCESS".equals(ret.get("BIZ_STATUS"))) {
        if ("FAIL".equals(ret.get("BIZ_STATUS"))) {
            retCause = String.valueOf(ret.get("BIZ_CAUSE"));
        } else {
            retCause = String.valueOf(ret.get("BIZ_STATUS"));
        }
    }
    return retCause;
}
```

### 사용 예시

#### 예시 1: 서비스 목록 조회

```java
@RequestMapping("/pubr/rqs/RQPrcuseReqst/listRQProvdSvcList")
public ModelAndView listRQProvdSvcList(
        @RequestParam("page") String page,
        @RequestParam("rows") String rows,
        HttpServletRequest request) {

    ModelAndView mav = new ModelAndView("jsonView");
    CMPubrLoginVO pubrUser = getPubrLoginVO(request);

    // ESB 요청 데이터 구성
    HashMap mapInterface = new HashMap();
    Integer firstIdx = (Integer.valueOf(page) - 1) * Integer.valueOf(rows);
    mapInterface.put("FIRSTINDEX", String.valueOf(firstIdx));
    mapInterface.put("RECORDCOUNTPERPAGE", rows);

    if ("MBTY06".equals(pubrUser.getMberTy()) ||
        "MBTY07".equals(pubrUser.getMberTy()) ||
        "MBTY08".equals(pubrUser.getMberTy())) {
        mapInterface.put("SET_INSTT", "Y");
    }

    // ESB 호출
    Map ret = esbCntcSend("IFCICSMP101", mapInterface);

    // 응답 처리
    String chkRetVal = chkEsbSuccess(ret);
    if ("".equals(chkRetVal)) {
        String retStr = (String)ret.get("Result");
        Map resultMap = PubcUtil.json2map(retStr);

        int totalCount = Integer.parseInt(String.valueOf(resultMap.get("TOTALCOUNT")));
        int maxPage = getMaxPage(totalCount, Integer.parseInt(rows));

        mav.addObject("status", ret.get("Status"));
        Map mapData = PubcUtil.getDataMap(ret, "DATA_LIST");
        mav.addObject("result", mapData.get("DATA_LIST"));
        mav.addObject("page", page);
        mav.addObject("total", maxPage);
        mav.addObject("records", totalCount);
    } else {
        mav.addObject("status", "FAIL");
        mav.addObject("cause", chkRetVal);
    }

    return mav;
}
```

#### 예시 2: 첨부파일 업로드 (분할 전송)

```java
// 파일을 2MB씩 분할하여 ESB로 전송
MultipartFile multiPartFile = multiRequest.getFile(fileTagName);
long orgFileSize = multiPartFile.getSize();
int splitSize = 2048000; // 2MB
byte[] fileByteArray = new byte[splitSize];

InputStream inst = multiPartFile.getInputStream();
int currentFileSizeTotalPosition = (int)Math.ceil(orgFileSize / splitSize);
int currentFileSizeSplitPosition = 0;

while ((len = inst.read(fileByteArray)) != -1) {
    currentFileSizeSplitPosition++;

    // Base64 인코딩
    String fileToString = new String(Base64.encodeBase64(fileByteArray));

    // ESB 파일 업로드 요청
    mapInterfaceFile.put("FILE_NAME", multiPartFile.getOriginalFilename());
    mapInterfaceFile.put("FILE_CONTENT", fileToString);
    mapInterfaceFile.put("CUR_FILE_SIZE_TOT_POSITN", String.valueOf(currentFileSizeTotalPosition));
    mapInterfaceFile.put("CUR_FILE_SIZE_SPLIT_POSITN", String.valueOf(currentFileSizeSplitPosition));

    Map retFile = esbCntcSend("IFCICSMP121", mapInterfaceFile);

    String chkRetVal = chkEsbSuccess(retFile);
    if (!"".equals(chkRetVal)) {
        // 업로드 실패 처리
        break;
    }
}
```

### ESB 요청 구조

**BaseController.esbCntcSend()가 자동으로 추가하는 공통 파라미터**:

```java
{
    "INTERFACE_ID": "IFCICSMP102",      // 인터페이스 ID (자동 추가)
    "INSTT_CODE": "...",                // 기관 코드 (자동 추가)
    "INQIRE_DATE": "20180208153000",    // 조회 일시 (자동 추가)
    "REPRSNT_SVC_CODE": "...",          // 대표 서비스 코드 (자동 추가)

    // 컨트롤러에서 추가하는 비즈니스 파라미터
    "FIRSTINDEX": "0",                  // 시작 인덱스
    "RECORDCOUNTPERPAGE": "10",         // 페이지당 레코드 수
    "MBER_ID": "user123",               // 회원 ID
    "MBER_TY": "MBTY06",                // 회원 유형
    // ... 기타 파라미터
}
```

### ESB 응답 구조

```java
{
    "Status": "SUCCESS" | "FAIL",       // 연결 상태
    "Cause": "...",                     // 실패 원인 (Status=FAIL 시)
    "BIZ_STATUS": "SUCCESS" | "FAIL",   // 비즈니스 상태
    "BIZ_CAUSE": "...",                 // 비즈니스 실패 원인
    "Result": "{...}"                   // JSON 문자열 결과
}
```

**응답 처리 흐름**:

1. **chkEsbSuccess()** 호출하여 성공 여부 확인
   - `Status == "FAIL"` → ESB 연결 실패, 에러 메시지 반환
   - `BIZ_STATUS != "SUCCESS"` → 비즈니스 로직 실패, 에러 메시지 반환
   - 모두 성공 → 빈 문자열 반환

2. **Result 파싱**:
   ```java
   String retStr = (String)ret.get("Result");
   Map resultMap = PubcUtil.json2map(retStr);
   ```

3. **데이터 추출**:
   ```java
   Map mapData = PubcUtil.getDataMap(ret, "DATA_LIST");
   List dataList = (List)mapData.get("DATA_LIST");
   ```

### ESB vs 로컬 DB 처리 비교

| 항목 | ESB 연동 | 로컬 DB 처리 |
|-----|---------|-------------|
| **목적** | 포털 DB 데이터 조회/수정 | OpenAPI 인증/로깅 |
| **데이터 위치** | 공공데이터 포털 서버 | 제공기관 로컬 서버 |
| **네트워크** | 외부 통신 필요 | 내부 통신 |
| **응답 속도** | 느림 (네트워크 지연) | 빠름 |
| **의존성** | 포털 서버 가용성 의존 | 독립적 |
| **사용 컨트롤러** | PUBR, 일부 PUBC | OpenAPI 서비스 |
| **핵심 사례** | 서비스 목록, 이용 신청 | API 인증, 사용 로깅 |

**핵심**:
- **ESB는 관리 기능**에서 포털 데이터 접근 시 사용
- **OpenAPI 인증/로깅은 ESB 없이** 로컬 DB에서 고속 처리

## 개발 가이드

### 새로운 기능 모듈 추가

#### 1. 패키지 생성

```
src/iros/pubc/[module]/
├── web/                      # 컨트롤러
│   └── XXModuleController.java
├── service/                  # 서비스 인터페이스
│   ├── XXModuleService.java
│   ├── XXModuleVO.java
│   └── impl/                 # 서비스 구현
│       ├── XXModuleServiceImpl.java
│       └── XXModuleDAO.java
```

#### 2. 컨트롤러 작성

```java
package iros.pubc.[module].web;

import iros.cmm.web.BaseController;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class XXModuleController extends BaseController {

    private String path = "/iros/pubc/[module]/";

    @Resource(name="xxModuleService")
    protected XXModuleService xxModuleService;

    @RequestMapping("/pubc/[module]/XX/viewXXList.do")
    public String viewXXList(HttpServletRequest request) {
        setMenuId("[menu_id]", request);
        return this.path + "XXList";
    }

    @RequestMapping("/pubc/[module]/XX/listXX.do")
    public String listXX(Model model, ...) {
        // 페이징, 조회 로직
        return "jsonView";
    }
}
```

#### 3. Service 인터페이스 작성

```java
package iros.pubc.[module].service;

public interface XXModuleService {
    List<XXModuleVO> selectXXList(XXModuleVO vo) throws Exception;
    int selectXXListTotalCount(XXModuleVO vo) throws Exception;
    void insertXX(XXModuleVO vo) throws Exception;
    void updateXX(XXModuleVO vo) throws Exception;
    void deleteXX(XXModuleVO vo) throws Exception;
}
```

#### 4. Service 구현 작성

```java
package iros.pubc.[module].service.impl;

import egovframework.rte.fdl.cmmn.AbstractServiceImpl;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service("xxModuleService")
public class XXModuleServiceImpl extends AbstractServiceImpl
                                 implements XXModuleService {

    @Resource(name="xxModuleDAO")
    private XXModuleDAO xxModuleDAO;

    @Override
    public List<XXModuleVO> selectXXList(XXModuleVO vo) throws Exception {
        return xxModuleDAO.selectXXList(vo);
    }

    // ...
}
```

#### 5. DAO 작성

```java
package iros.pubc.[module].service.impl;

import egovframework.rte.psl.dataaccess.EgovAbstractDAO;
import org.springframework.stereotype.Repository;

@Repository("xxModuleDAO")
public class XXModuleDAO extends EgovAbstractDAO {

    public List<XXModuleVO> selectXXList(XXModuleVO vo) throws Exception {
        return list("XXModuleDAO.selectXXList", vo);
    }

    public int selectXXListTotalCount(XXModuleVO vo) throws Exception {
        return (Integer)getSqlMapClientTemplate()
            .queryForObject("XXModuleDAO.selectXXListTotalCount", vo);
    }

    // ...
}
```

#### 6. SQL Map 작성

각 DBMS별로 작성:

**위치**: `src/iros/sqlmap/{dbms}/pubc/[module]/XXModuleSQL.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE sqlMap PUBLIC "-//iBATIS.com//DTD SQL Map 2.0//EN"
    "http://www.ibatis.com/dtd/sql-map-2.dtd">

<sqlMap namespace="XXModuleDAO">

    <typeAlias alias="XXModuleVO"
               type="iros.pubc.[module].service.XXModuleVO"/>

    <select id="XXModuleDAO.selectXXList"
            parameterClass="XXModuleVO"
            resultClass="XXModuleVO">
        <![CDATA[
            SELECT
                COLUMN1,
                COLUMN2,
                COLUMN3
            FROM TABLE_NAME
            WHERE 1=1
        ]]>
        <isNotEmpty property="searchKeyword">
            AND COLUMN1 LIKE '%' + #searchKeyword# + '%'
        </isNotEmpty>
        <![CDATA[
            ORDER BY COLUMN1 DESC
            LIMIT #firstIndex#, #recordCountPerPage#
        ]]>
    </select>

    <select id="XXModuleDAO.selectXXListTotalCount"
            parameterClass="XXModuleVO"
            resultClass="int">
        <![CDATA[
            SELECT COUNT(*) AS CNT
            FROM TABLE_NAME
            WHERE 1=1
        ]]>
        <isNotEmpty property="searchKeyword">
            AND COLUMN1 LIKE '%' + #searchKeyword# + '%'
        </isNotEmpty>
    </select>

</sqlMap>
```

#### 7. JSP 뷰 작성

**위치**: `WebContent/WEB-INF/jsp/iros/pubc/[module]/XXList.jsp`

### ESB 연동 기능 추가

```java
@RequestMapping("/pubc/[module]/XX/getDataFromPortal.do")
public ModelAndView getDataFromPortal(
        @RequestParam("param1") String param1,
        HttpServletRequest request) {

    ModelAndView mav = new ModelAndView("jsonView");

    // 1. 로그인 사용자 조회
    CMPubcLoginVO pubcUser = getPubcLoginVO(request);

    // 2. ESB 요청 구성
    HashMap<String, String> mapInterface = new HashMap<String, String>();
    mapInterface.put("FIRSTINDEX", "0");
    mapInterface.put("RECORDCOUNTPERPAGE", "100");
    mapInterface.put("PARAM1", param1);
    mapInterface.put("MBER_ID", pubcUser.getMberId());

    // 3. ESB 호출
    Map<String, String> ret = esbCntcSend("INTERFACE_ID", mapInterface);

    // 4. 응답 검증
    String chkRetVal = chkEsbSuccess(ret);

    if ("".equals(chkRetVal)) {
        // 성공 처리
        String retStr = ret.get("Result");
        Map resultMap = PubcUtil.json2map(retStr);

        mav.addObject("success", true);
        mav.addObject("data", resultMap);
    } else {
        // 실패 처리
        mav.addObject("success", false);
        mav.addObject("message", chkRetVal);
    }

    return mav;
}
```

### 페이징 처리

```java
// 1. PaginationInfo 생성
PaginationInfo paginationInfo = new PaginationInfo();
paginationInfo.setCurrentPageNo(vo.getPage());
paginationInfo.setRecordCountPerPage(vo.getRows());

// 2. VO에 페이징 정보 설정
vo.setFirstIndex(paginationInfo.getFirstRecordIndex());
vo.setLastIndex(paginationInfo.getLastRecordIndex());
vo.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

// 3. 데이터 조회
List listResult = service.selectList(vo);
int totalCount = service.selectTotalCount(vo);

paginationInfo.setTotalRecordCount(totalCount);

// 4. JSON 응답 구성
Map jsonModel = new HashMap();
int maxPage = (int)(totalCount / vo.getRecordCountPerPage() + 0.97D);

jsonModel.put("page", vo.getPage());
jsonModel.put("total", maxPage);
jsonModel.put("records", totalCount);
jsonModel.put("rows", listResult);

model.addAllAttributes(jsonModel);
```

## 주의사항

### 레거시 기술 스택

1. **Java 버전 제약**:
   - **컴파일 타겟**: Java 5 (1.5)
   - **개발 환경**: JDK 1.6.0_45
   - **사용 가능 기능**: 제네릭스, 어노테이션 기본 수준
   - **사용 불가 기능**:
     - Lambda 표현식 (Java 8+)
     - Stream API (Java 8+)
     - try-with-resources (Java 7+)
     - Diamond 연산자 `<>` (Java 7+)
     - 멀티 catch (Java 7+)

2. **Spring 2.5.6 제약**:
   - `@RestController` 없음 (`@Controller` + `@ResponseBody` 또는 View 사용)
   - Java Config 없음 (XML 설정만)
   - `@RequestBody` / `@ResponseBody` 제한적

3. **iBATIS 2.x**:
   - MyBatis가 아닌 iBATIS 문법 사용
   - `parameterClass`, `resultClass` 속성 사용
   - `#param#` 파라미터 바인딩

### 인코딩 이슈

- 소스 파일: 일부 EUC-KR 인코딩
- 웹 애플리케이션: UTF-8 권장
- 한글 주석 깨짐 가능성 주의

### DBMS별 SQL 차이

각 DBMS의 페이징, 함수, 데이터 타입 차이를 고려하여 SQL Map 작성:
- **Oracle**: ROWNUM
- **MySQL/PostgreSQL**: LIMIT
- **MS SQL**: TOP, OFFSET FETCH

### 디컴파일된 소스

일부 Java 파일은 디컴파일된 코드로 보임:
- 주석에 라인 번호 포함 (`/* 40 */`)
- 변수명 일부 손실 가능
- 로직은 온전함

## 빌드 및 배포

### Eclipse에서 빌드

1. Eclipse에서 프로젝트 Import
2. **JDK 1.6.0_45** 설정 (개발 환경)
   - Window → Preferences → Java → Installed JREs
   - JDK 1.6.0_45 추가 및 선택
3. **컴파일 타겟은 Java 5**로 자동 설정됨 (Maven pom.xml)
4. Tomcat 6.0 서버 설정
5. Project → Build Project
6. Export → WAR file

**중요**: Eclipse에서는 JDK 6을 사용하지만, Maven 빌드 시 Java 5 타겟으로 컴파일되어 하위 호환성이 보장됩니다.

### WAR 파일 배포

**생성 위치**: `build/` 또는 Eclipse Export

**배포 대상**:
- Apache Tomcat 6.0+
- JEUS (jeus-web-dd.xml 포함)
- WebLogic (별도 설정 필요)

### 환경 설정

1. **데이터베이스 설정**: iBATIS SQL Map Config 선택
2. **ESB 설정**: `PubcProperties` 설정
3. **로깅 설정**: `log4j.xml` 수정

## PUBC의 배포 형태

**중요**: PUBC는 용도에 따라 3가지 형태로 제공되며, 각각 독립적으로 사용 가능합니다.

### 1. 인증 모듈 (경량 WAR)

**위치**: `공통연계모듈/2.인증모듈/`

**목적**: 기존 API 서비스에 인증/로깅 기능만 추가

**구조**:
```
WEB-INF/
├── classes/
│   └── iros/pubc/
│       ├── spring/*.xml (Spring 설정)
│       ├── sqlmap/ (SQL 매핑)
│       └── cmm/web/ScheduleExecuteController.class
└── lib/ (필수 라이브러리)
```

**제공 기능**:
- ✓ API 인증 (`UserCrtfcProcessService`)
- ✓ 사용 현황 로깅 (`UseSttusService`)
- ✓ 에러 코드 관리 (`PubcErrorCodeProperties`)
- ✗ 관리 웹 UI (없음)

**사용 방법**:

샘플 소스의 `CommonProc.java`를 복사하여 사용:

```java
package iros.pubc.common;

import iros.pubc.usc.service.UserCrtfcProcessService;
import iros.pubc.usi.service.UseSttusService;

public class CommonProc {

    private static ApplicationContext context = null;

    private void contextInit(){
        if(context == null){
            String[] configLocation = new String[] {
                "iros/pubc/spring/context-common-pubc.xml",
                "iros/pubc/spring/context-datasource-pubc.xml",
                "iros/pubc/spring/context-idgen-pubc.xml",
                "iros/pubc/spring/context-sqlMap-pubc.xml"
            };
            context = new ClassPathXmlApplicationContext(configLocation);
            userCrtfcProcessService = (UserCrtfcProcessService)
                context.getBean("UserCrtfcProcessService");
            useSttusService = (UseSttusService)
                context.getBean("UseSttusService");
        }
    }

    public UserCrtfcVO authServiceCall(HttpServletRequest request){
        contextInit(); // 자체적으로 Spring 컨텍스트 로드
        UserCrtfcVO userCrtfcVO = userCrtfcProcessService.userCrtfcProcess(request);
        // ...
        return userCrtfcVO;
    }
}
```

**특징**:
- CommonProc가 **자체적으로 Spring 컨텍스트를 로드**
- 기존 API WAR에 쉽게 통합 가능
- 독립적인 Spring 컨테이너 운영

**샘플 소스 위치**: `인증,연계모듈 설치 가이드/샘플소스/`
- `CommonProc.java`: 인증/로깅 래퍼 클래스
- `PubcErrorCodeProperties.java`: 에러 코드 정의
- `Test_SVC.java`: 사용 예제

### 2. JAR 라이브러리 (iros_pubc_1.1.jar)

**위치**: `oasis_소스/WEB-INF/lib/iros_pubc_1.1.jar`

**목적**: 다른 WAR에 임베드하여 사용 (OASIS 등)

**제공 기능**:
- ✓ API 인증
- ✓ 사용 현황 로깅
- ✓ 에러 코드 관리
- ✗ 관리 웹 UI (없음)

**특징**:
- 클래스 파일만 포함
- Spring 설정은 호스트 WAR에서 로드
- OASIS에서 Spring 컨테이너에 통합하여 사용

**사용 방법**:

```xml
<!-- 호스트 WAR의 web.xml -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
        /WEB-INF/config/context-*.xml,
        classpath:iros/pubc/spring/context-common-pubc.xml,
        classpath:iros/pubc/spring/context-datasource-pubc.xml,
        classpath:iros/pubc/spring/context-sqlMap-pubc.xml
    </param-value>
</context-param>
```

### 3. 관리 모듈 WAR (pubc.war)

**위치**: `공통연계모듈/3.관리모듈/pubc/pubc.war` 또는 `pubc_2017.war`

**목적**: 완전한 기능의 관리 웹 애플리케이션

**제공 기능**:
- ✓ API 인증/로깅
- ✓ 서비스 모니터링 (장애, 제공 현황)
- ✓ 사용 현황 통계
- ✓ 임계값 관리
- ✓ 이용 제한 관리
- ✓ 웹 관리 콘솔 UI
- ✓ ESB 연동

**특징**:
- 완전한 Spring MVC 애플리케이션
- 관리자(PUBC)/포털(PUBR) 이중 사용자 시스템
- JSP 기반 웹 UI
- 단독 WAR로 배포 가능

### 배포 형태 비교

| 구분 | 인증 모듈 | JAR 라이브러리 | 관리 모듈 WAR |
|-----|----------|---------------|--------------|
| **형태** | 경량 WAR | JAR 파일 | 완전 WAR |
| **크기** | 소형 | 최소 | 대형 |
| **인증/로깅** | ✓ | ✓ | ✓ |
| **관리 UI** | ✗ | ✗ | ✓ |
| **Spring 로드** | 자체 로드 | 호스트 통합 | 자체 로드 |
| **사용처** | 레거시 API | OASIS 등 | 단독 운영 |

### 실제 사용 예시

#### 예시 1: 레거시 API에 인증 추가

```
기존 API (your-api.war)
├── 기존 코드
└── 추가
    ├── CommonProc.java (샘플 소스 복사)
    └── WEB-INF/
        ├── lib/ (PUBC 라이브러리 추가)
        └── classes/
            └── iros/pubc/spring/*.xml

사용:
CommonProc commonProc = new CommonProc();
UserCrtfcVO userCrtfcVO = commonProc.authServiceCall(request);
if("00".equals(userCrtfcVO.getTroblTyCode())){
    // API 로직 실행
}
commonProc.useSttusServiceCall(userCrtfcVO);
```

#### 예시 2: OASIS 통합 (JAR 방식)

```
oasis.war
├── oasis.* (OASIS 코드)
└── WEB-INF/
    ├── lib/iros_pubc_1.1.jar
    └── config/
        └── web.xml (PUBC Spring 설정 통합)

특징: 단일 Spring 컨테이너에서 모든 빈 공유
```

#### 예시 3: 관리 콘솔 단독 배포

```
pubc.war (포트 8080)

제공:
- 관리자 로그인
- 서비스 모니터링 대시보드
- 사용 통계
- 설정 관리
```

## OASIS와의 통합 관계

PUBC는 OASIS (OpenAPI 자동생성툴)에서 **JAR 라이브러리 형태**로 사용됩니다.

### 통합 구조

```
┌─────────────────────────────────────┐
│  OASIS (OpenAPI 자동생성툴)         │
│  ├── oasis.* 패키지 (자체 구현)    │
│  │   ├── engine/ (92 files)        │
│  │   ├── server/ (22 files)        │
│  │   ├── tools/                    │
│  │   └── extend/                   │
│  │       └── CommonProc.java       │← PUBC 인증/로깅 래퍼
│  │                                  │
│  └── WEB-INF/lib/                  │
│      └── iros_pubc_1.1.jar  ←──────┼─── PUBC 라이브러리 의존
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│  PUBC (공통연계모듈)                │
│  ├── iros.pubc.* (62 files)        │
│  ├── iros.pubr.* (12 files)        │
│  └── iros.cmm.* (공통)              │
│      ├── usc/ - 사용자 인증         │
│      ├── usi/ - 사용 현황 로깅      │
│      └── cmm/ - 공통 기능           │
└─────────────────────────────────────┘
```

### OASIS가 사용하는 PUBC 모듈

#### 1. 인증 모듈 (usc)

**OASIS 사용처**: `oasis.extend.CommonProc`

```java
import iros.pubc.usc.service.UserCrtfcProcessService;
import iros.pubc.usc.service.vo.UserCrtfcVO;

// OpenAPI 호출 시 서비스키 인증
userCrtfcVO = userCrtfcProcessService.userCrtfcProcess(request);

// 인증 성공/실패 체크
if("00".equals(userCrtfcVO.getTroblTyCode())) {
    // 인증 성공 - API 실행
} else {
    // 인증 실패 - 에러 응답
}
```

#### 2. 사용 현황 로깅 (usi)

**OASIS 사용처**: `oasis.extend.CommonProc`

```java
import iros.pubc.usi.service.UseSttusService;

// API 호출 내역 로깅 (성공/실패 모두)
useSttusService.insertUseSttus(userCrtfcVO);
```

#### 3. 에러 코드 관리 (cmm)

**OASIS 사용처**: `oasis.extend.CommonProc`

```java
import iros.pubc.cmm.PubcErrorCodeProperties;
import iros.pubc.cmm.PubcProperties;

// 표준 에러 코드 사용
String errCode = PubcErrorCodeProperties.APPLICATION_ERROR;  // "01"
String errMsg = messageSource.getMessage("Service.Error." + errCode,
                                         null,
                                         PubcProperties.LOCALE_LANG);
```

#### 4. CXF Interceptor 연동

**OASIS 사용처**: JAX-RS/JAX-WS Interceptor

```java
// JaxRsInInterceptor, JaxWsInInterceptor
import iros.pubc.usc.service.vo.UserCrtfcVO;

// REST/SOAP 요청 전처리에서 인증 VO 전달
```

### Spring 설정 통합

**OASIS web.xml**:

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
        /WEB-INF/config/context-*.xml,
        classpath:iros/pubc/spring/context-aspect-pubc.xml,
        classpath:iros/pubc/spring/context-common-pubc.xml,
        classpath:iros/pubc/spring/context-datasource-pubc.xml,
        classpath:iros/pubc/spring/context-idgen-pubc.xml,
        classpath:iros/pubc/spring/context-sqlMap-pubc.xml,
        classpath:iros/pubc/spring/context-scheduling-pubc.xml
    </param-value>
</context-param>
```

PUBC의 모든 Spring 빈(Service, DAO, DataSource 등)이 OASIS의 Spring 컨테이너에 로드됩니다.

### 패키지 독립성

| 프로젝트 | 패키지 | 역할 |
|---------|--------|------|
| **PUBC** | `iros.pubc.*` | 관리 모듈 (62 files) |
| **PUBC** | `iros.pubr.*` | 포털 연동 (12 files) |
| **PUBC** | `iros.cmm.*` | 공통 기능 |
| **OASIS** | `oasis.*` | OpenAPI 자동생성 (136 files) |
| **OASIS** | `oasis.extend.CommonProc` | PUBC 래퍼 (브리지 역할) |

**주요 특징**:
- OASIS는 `iros.*` 패키지를 직접 구현하지 않음
- `iros_pubc_1.1.jar`를 통해 완전히 캡슐화
- OASIS의 `extend` 패키지만 PUBC와 연동

### 배포 시 고려사항

#### JAR 파일 위치

**OASIS 배포 시 필수**:
```
OASIS.war/
└── WEB-INF/
    └── lib/
        ├── iros_pubc_1.1.jar          ← PUBC 모듈
        ├── iros_cipher.jar             ← PUBC 암호화
        └── pubc_common_properties.xml  ← PUBC 설정
```

#### PUBC 독립 배포

PUBC를 별도 WAR로 배포할 경우:
- 관리 콘솔 제공 (`pubc.war`)
- OASIS와 동일 데이터베이스 공유
- 동일 서버 또는 별도 서버 가능

#### OASIS 통합 배포

OASIS WAR에 PUBC JAR 포함:
- OpenAPI 서비스 자동생성 + 인증/로깅
- 단일 WAR 배포로 관리 간소화
- PUBC 관리 UI는 포함되지 않음

### 통합 버전 호환성

| 구성요소 | OASIS | PUBC |
|---------|-------|------|
| Java 버전 | JDK 1.6 | JDK 1.6 |
| 컴파일 타겟 | Java 5 | Java 5 |
| Spring 버전 | 2.5.6 | 2.5.6 |
| iBATIS 버전 | - | 2.3.4 |
| CXF 버전 | 2.3.2 | 2.2.9 |

**호환성**: 완전히 동일한 기술 스택으로 통합 가능

## 참고 문서

- Spring Framework 2.5.x 문서
- iBATIS 2.3.4 문서
- eGovFramework 1.0 가이드
- Apache CXF 2.2.9 문서
- **OASIS.md**: OpenAPI 자동생성툴 상세 문서

## 관련 문서

- **CLAUDE.md**: 전체 저장소 가이드
- **OASIS.md**: OASIS 자동생성툴 상세 분석

---

**작성일**: 2025년
**기준 버전**: PUBC Ver.2.3 (2018-02-08)
**OASIS 통합**: iros_pubc_1.1.jar
