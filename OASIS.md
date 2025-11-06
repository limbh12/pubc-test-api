# OASIS 소스코드 상세 분석

OpenAPI 자동생성툴(OASIS - OpenAPI Automatic Integration System) 소스코드에 대한 상세 분석 문서입니다.

## 프로젝트 개요

- **프로젝트명**: oasis_safekorea
- **타입**: Eclipse Dynamic Web Project
- **Java 버전**: JDK 1.6.0_45
- **WAS**: Apache Tomcat 6.0
- **마지막 수정**: 2018년 2월
- **위치**: `(대외비) 06.OpenAPI_자동생성툴/oasis_소스/`

## 소스코드 통계

- **총 Java 파일**: 136개
  - **Engine 패키지**: 92개 (핵심 엔진)
  - **Server 패키지**: 22개 (관리 서버)
  - **기타**: 22개 (도구, 확장)
- **JSP 파일**: 19개
- **JAR 라이브러리**: 186개
- **JavaScript 라이브러리**: Dojo Toolkit

## 프로젝트 목적

OASIS는 데이터베이스의 테이블/뷰 정보를 기반으로 **SOAP 및 REST 방식의 OpenAPI 서비스를 자동으로 생성**하는 도구입니다.

### 주요 기능

1. **데이터베이스 연동**: 다양한 DBMS에서 테이블/뷰 메타데이터 조회
2. **자동 서비스 생성**:
   - SOAP 웹 서비스 자동 생성
   - RESTful API 자동 생성
3. **동적 서비스 관리**: 런타임에 서비스 시작/중지/재시작
4. **PUBC 연동**: 공통연계모듈(PUBC)과 통합하여 인증/로깅 지원
5. **웹 관리 UI**: Dojo 기반 관리 인터페이스

## 패키지 구조

```
src/
├── oasis/                            # OASIS 핵심 패키지
│   ├── extend/                       # 확장 기능
│   │   ├── CommonProc.java           # 공통 처리
│   │   ├── Cloud.java                # 클라우드 연동
│   │   ├── BlueSky.java
│   │   └── cxf/                      # Apache CXF 확장
│   │       ├── jaxws/                # JAX-WS (SOAP) 인터셉터
│   │       └── jaxrs/                # JAX-RS (REST) 인터셉터
│   │
│   ├── tools/                        # 개발 도구
│   │   ├── ConnectionLogManager.java
│   │   ├── ConnectionLogProxy.java  # JDBC 로깅 프록시
│   │   ├── StatementLogProxy.java
│   │   ├── PreparedStatementLogProxy.java
│   │   ├── ResultSetLogProxy.java
│   │   ├── E2KConnectionLogManager.java  # EUC-KR 변환
│   │   ├── K2EConnectionLogManager.java  # KSC5601 변환
│   │   ├── U2EConnectionLogManager.java  # UTF-8 변환
│   │   └── E2UConnectionLogManager.java
│   │
│   ├── server/                       # 서버 관리 (22 files)
│   │   ├── manager/
│   │   │   ├── web/
│   │   │   │   └── MainController.java       # 메인 컨트롤러
│   │   │   ├── admin/
│   │   │   │   ├── AdminController.java      # 관리자 인증
│   │   │   │   ├── AdminDWRService.java      # DWR 서비스
│   │   │   │   └── RsaFunction.java          # RSA 암호화
│   │   │   ├── log/
│   │   │   │   └── LogController.java        # 로그 조회
│   │   │   ├── service/
│   │   │   │   ├── soap/                     # SOAP 서비스 관리
│   │   │   │   │   ├── SOAPController.java
│   │   │   │   │   ├── SOAPService.java
│   │   │   │   │   ├── ISOAPService.java
│   │   │   │   │   └── SOAPDWRService.java
│   │   │   │   └── rest/                     # REST 서비스 관리
│   │   │   │       ├── RESTController.java
│   │   │   │       ├── RESTService.java
│   │   │   │       ├── IRESTService.java
│   │   │   │       └── RESTDWRService.java
│   │   │   └── base/
│   │   │       ├── CommonService.java        # 공통 서비스
│   │   │       └── CommonServiceImpl.java
│   │   │
│   │   └── base/
│   │       └── dwr/
│   │           └── filter/                   # DWR 필터
│   │       └── servlet/
│   │           └── filter/                   # 서블릿 필터
│   │
│   └── engine/                       # 서비스 엔진 (92 files)
│       ├── svr/                      # 서비스 런타임
│       │   └── jax/
│       │       ├── soap/
│       │       │   ├── SOAPServiceManager.java    # SOAP 서비스 매니저
│       │       │   ├── SOAPServiceClass.java      # 동적 클래스 생성
│       │       │   └── SOAPServiceDelegate.java   # 서비스 위임
│       │       ├── rest/
│       │       │   ├── RESTServiceManager.java    # REST 서비스 매니저
│       │       │   ├── RESTServiceClass.java      # 동적 클래스 생성
│       │       │   ├── RESTServiceDelegate.java   # 서비스 위임
│       │       │   └── IntegerParameterHandler.java
│       │       └── base/
│       │           └── servlet/
│       │               └── OASISCXFServlet.java   # CXF 서블릿
│       │
│       └── util/                     # 유틸리티
│           ├── el/                   # Expression Language
│           │   ├── ExpressionEvaluator.java
│           │   ├── ELFunctions.java
│           │   ├── SimpleVariableResolver.java
│           │   └── RequestMap.java
│           ├── ClassUtils.java
│           ├── SecurityUtils.java
│           └── JavassitUtils.java              # Javassist 바이트코드 조작
│
├── iros/                             # PUBC 연동 패키지
│   └── pubc/                         # PUBC 공통연계모듈 통합
│
└── org/                              # 서드파티 라이브러리
    └── apache/
```

## 웹 애플리케이션 구조

```
WebContent/
├── index.jsp                         # 메인 페이지
├── ipError.jsp                       # IP 접근 오류 페이지
├── css/                              # 스타일시트
├── images/                           # 이미지 리소스
├── js/
│   ├── dojo/                         # Dojo Toolkit (JavaScript 프레임워크)
│   └── rsa/                          # RSA 암호화 라이브러리
├── mngmain/                          # 관리 메인 화면
├── META-INF/
└── WEB-INF/
    ├── web.xml                       # 웹 애플리케이션 설정
    ├── jsp/                          # JSP 뷰 파일
    ├── lib/                          # JAR 라이브러리 (186개)
    ├── tld/                          # Tag Library Descriptors (17개)
    ├── config/                       # Spring 설정 파일
    │   ├── context-oasis.xml         # OASIS 엔진 설정
    │   ├── context-cxf.xml           # Apache CXF 설정
    │   ├── context-manager.xml       # 관리 서비스 설정
    │   ├── context-datasource.xml    # 데이터소스 설정
    │   ├── context-transaction.xml   # 트랜잭션 설정
    │   ├── context-app.xml           # 애플리케이션 설정
    │   ├── context-spring.xml        # Spring MVC 설정
    │   └── log4j.xml                 # 로깅 설정
    ├── setup/                        # 초기 설정 파일
    └── work/                         # 작업 디렉토리
```

## 핵심 컴포넌트

### 1. SOAP 서비스 자동 생성 엔진

#### SOAPServiceManager

**위치**: `oasis.engine.svr.jax.soap.SOAPServiceManager`

SOAP 웹 서비스를 동적으로 생성하고 관리하는 매니저입니다.

**주요 기능**:
- 데이터베이스 메타데이터 기반 SOAP 서비스 자동 생성
- Apache CXF를 사용한 JAX-WS 서비스 배포
- 런타임 서비스 시작/중지
- 서비스 엔드포인트 동적 등록

#### SOAPServiceClass

**위치**: `oasis.engine.svr.jax.soap.SOAPServiceClass`

**주요 기능**:
- Javassist를 사용한 동적 클래스 생성
- 데이터베이스 테이블/뷰에 매핑되는 Java 클래스 자동 생성
- JAX-WS 어노테이션 자동 추가
- WSDL 자동 생성 지원

#### SOAPServiceDelegate

**위치**: `oasis.engine.svr.jax.soap.SOAPServiceDelegate`

**주요 기능**:
- 실제 비즈니스 로직 수행 (데이터베이스 CRUD)
- SQL 쿼리 동적 생성 및 실행
- 파라미터 매핑 및 결과 변환

### 2. REST 서비스 자동 생성 엔진

#### RESTServiceManager

**위치**: `oasis.engine.svr.jax.rest.RESTServiceManager`

RESTful API를 동적으로 생성하고 관리하는 매니저입니다.

**주요 기능**:
- 데이터베이스 메타데이터 기반 REST API 자동 생성
- Apache CXF를 사용한 JAX-RS 서비스 배포
- HTTP 메서드 매핑 (GET, POST, PUT, DELETE)
- JSON/XML 응답 포맷 지원

#### RESTServiceClass

**위치**: `oasis.engine.svr.jax.rest.RESTServiceClass`

**주요 기능**:
- JAX-RS 어노테이션 자동 추가 (`@Path`, `@GET`, `@POST`, etc.)
- URI 경로 자동 매핑
- 쿼리 파라미터 및 경로 파라미터 처리

#### RESTServiceDelegate

**위치**: `oasis.engine.svr.jax.rest.RESTServiceDelegate`

**주요 기능**:
- RESTful 리소스 CRUD 작업
- HTTP 상태 코드 관리
- Content Negotiation (JSON/XML)

### 3. 서비스 관리 컨트롤러

#### SOAPService & RESTService

**위치**:
- `oasis.server.manager.service.soap.SOAPService`
- `oasis.server.manager.service.rest.RESTService`

**주요 기능**:

```java
@Service("soapService")
public class SOAPService implements ISOAPService {

    @Autowired
    private OASISRepository repository;

    // 서비스 목록 조회 (상태 포함)
    public List<SOAPServiceInfo> getServiceInfoList() {
        List<SOAPServiceInfo> serviceInfoList = repository.getSOAPRepository().getServiceInfoList();
        SOAPServiceManager sm = SOAPServiceManager.getManager();
        for (SOAPServiceInfo serviceInfo : serviceInfoList) {
            IService service = sm.findService(serviceInfo.getServiceId());
            if (service != null && service.isRunning()) {
                serviceInfo.setStatus(IConstants.SERVICE_STATUS_RUNNING);
            } else {
                serviceInfo.setStatus(IConstants.SERVICE_STATUS_STOPPED);
            }
        }
        return serviceInfoList;
    }

    // 서비스 정보 조회
    public SOAPServiceInfo getServiceInfo(String serviceId)

    // 서비스 정보 저장/업데이트
    public void upsertServiceInfo(SOAPServiceInfo serviceInfo)

    // 서비스 정보 삭제
    public void removeServiceInfo(SOAPServiceInfo serviceInfo)
}
```

#### MainController

**위치**: `oasis.server.manager.web.MainController`

**주요 기능**:
- 초기화 시 로그 테이블 자동 생성
- 관리자 세션 관리
- 공통 서비스 제공

```java
@Controller
public class MainController implements InitializingBean {

    @Autowired
    private CommonService commonService;

    @Autowired
    private OASISConfig config;

    @Autowired
    private LogDao logDao;

    // 초기화: 로그 테이블 생성
    public void afterPropertiesSet() throws Exception {
        if (config.isExecuteCreateLogTable()) {
            initializeDatabase();
        }
    }

    // schema.sql 파일 읽어서 실행
    private void initializeDatabase() throws IOException, Exception {
        Resource resource = new ClassPathResource("schema.sql");
        File sqlFile = resource.getFile();
        List<String> lines = FileUtils.readLines(sqlFile);
        // SQL 파싱 및 실행...
    }

    // 관리자 세션 조회
    protected IAdmin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        IAdmin admin = (IAdmin)session.getAttribute("admin");
        if (admin == null) {
            throw new LoginRequiredException("로그인 후 사용해주세요.");
        }
        return admin;
    }
}
```

### 4. DWR (Direct Web Remoting) 통합

#### SOAPDWRService & RESTDWRService

**위치**:
- `oasis.server.manager.service.soap.SOAPDWRService`
- `oasis.server.manager.service.rest.RESTDWRService`

DWR을 사용하여 JavaScript에서 직접 Java 메서드를 호출할 수 있게 합니다.

**사용 예**:
```javascript
// JavaScript에서 직접 호출
SOAPDWRService.getServiceInfoList(function(serviceList) {
    // 서비스 목록 처리
});

SOAPDWRService.startService(serviceId, function(result) {
    // 서비스 시작 결과 처리
});
```

### 5. Apache CXF 인터셉터

#### JAX-WS (SOAP) 인터셉터

**위치**: `oasis.extend.cxf.jaxws.*`

- **JaxWsInInterceptor**: 요청 인터셉터
- **JaxWsOutInterceptor**: 응답 인터셉터
- **JaxWsFaultOutInterceptor**: SOAP Fault 인터셉터
- **InternalActServiceFaultOutInterceptor**: 내부 서비스 Fault 처리

#### JAX-RS (REST) 인터셉터

**위치**: `oasis.extend.cxf.jaxrs.*`

- **JaxRsInInterceptor**: 요청 인터셉터
- **JaxRsOutInterceptor**: 응답 인터셉터
- **JaxRsFaultOutInterceptor**: REST 예외 처리

**인터셉터 기능**:
- 요청/응답 로깅
- 인증/권한 검증
- 성능 모니터링
- 에러 처리 및 표준화

### 6. JDBC 로깅 프록시

**위치**: `oasis.tools.*`

데이터베이스 연결 및 SQL 실행을 로깅하는 프록시 클래스들:

- **ConnectionLogProxy**: Connection 프록시
- **StatementLogProxy**: Statement 프록시
- **PreparedStatementLogProxy**: PreparedStatement 프록시
- **ResultSetLogProxy**: ResultSet 프록시

**문자 인코딩 변환 매니저**:
- **E2KConnectionLogManager**: EUC-KR → KSC5601
- **K2EConnectionLogManager**: KSC5601 → EUC-KR
- **U2EConnectionLogManager**: UTF-8 → EUC-KR
- **E2UConnectionLogManager**: EUC-KR → UTF-8

### 7. 동적 클래스 생성 (Javassist)

**위치**: `oasis.engine.util.JavassitUtils`

**주요 기능**:
- 런타임에 Java 클래스 동적 생성
- 바이트코드 조작
- 어노테이션 추가
- 메서드 생성

**사용 예**:
```java
// 데이터베이스 테이블 "TB_USER"에 대한 서비스 클래스 자동 생성
// 생성되는 클래스:
// - TbUserService.java (인터페이스)
// - TbUserServiceImpl.java (구현체)
// - TbUser.java (VO)
```

### 8. Expression Language (EL) 지원

**위치**: `oasis.engine.util.el.*`

**주요 기능**:
- SQL 쿼리에 EL 표현식 사용
- 동적 쿼리 생성
- 요청 파라미터 바인딩

**사용 예**:
```sql
SELECT * FROM TB_USER
WHERE 1=1
<#if userName != null>
  AND USER_NAME = #{userName}
</#if>
```

## PUBC 연동

### Spring 설정 통합

**위치**: `WebContent/WEB-INF/web.xml`

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

OASIS는 PUBC 공통연계모듈과 완전히 통합되어 다음 기능을 사용합니다:

1. **인증 모듈**: OpenAPI 서비스 인증
2. **로깅 모듈**: 사용 현황 및 장애 로깅
3. **데이터소스**: PUBC의 데이터베이스 설정 공유
4. **스케줄링**: Quartz 스케줄러 공유

## 기술 스택

### 백엔드

| 기술 | 버전/설명 | 용도 |
|------|-----------|------|
| **Java** | JDK 1.6.0_45 | 개발 언어 |
| **Spring Framework** | 3.x+ | MVC, IoC, DI |
| **Apache CXF** | 2.x+ | 웹 서비스 프레임워크 (JAX-WS, JAX-RS) |
| **Javassist** | 3.x | 동적 클래스 생성 및 바이트코드 조작 |
| **DWR** | 3.x | Direct Web Remoting (Ajax) |
| **SLF4J + Log4j** | - | 로깅 |
| **PUBC** | Ver.2.3 | 공통연계모듈 통합 |

### 프론트엔드

| 기술 | 용도 |
|------|------|
| **Dojo Toolkit** | JavaScript 프레임워크 (UI 위젯, Ajax) |
| **JSP + JSTL** | 서버 사이드 템플릿 |
| **CSS** | 스타일링 |
| **RSA JavaScript** | 클라이언트 암호화 |

### 데이터베이스

- JDBC를 통한 다양한 DBMS 지원
- PUBC 데이터소스 설정 공유
- 동적 SQL 생성 엔진

## 서비스 생성 프로세스

### 1. 데이터베이스 연결

```
사용자 → 관리 UI → 데이터소스 설정
    ↓
JDBC 연결 → 메타데이터 조회
    ↓
테이블/뷰 목록 조회
```

### 2. 서비스 메타데이터 정의

```
사용자 → 서비스 생성 화면
    ↓
- 테이블/뷰 선택
- 서비스 ID 입력
- 엔드포인트 URL 설정
- 서비스 타입 선택 (SOAP/REST)
    ↓
저장 → OASISRepository
```

### 3. 동적 클래스 생성

```
SOAPServiceManager/RESTServiceManager
    ↓
JavassitUtils → 동적 클래스 생성
    ↓
- Service Interface
- Service Implementation
- VO (Value Object)
- Delegate (비즈니스 로직)
    ↓
클래스 로딩 → ClassLoader
```

### 4. CXF 서비스 배포

```
CXF Server Factory
    ↓
- JAX-WS: JaxWsServerFactoryBean
- JAX-RS: JAXRSServerFactoryBean
    ↓
인터셉터 등록
    ↓
엔드포인트 발행
    ↓
서비스 시작 ✓
```

### 5. 서비스 호출

#### SOAP 호출
```
클라이언트 → SOAP 요청
    ↓
CXF Servlet → JaxWsInInterceptor
    ↓
SOAPServiceDelegate → SQL 실행
    ↓
ResultSet → VO 변환
    ↓
JaxWsOutInterceptor → SOAP 응답
```

#### REST 호출
```
클라이언트 → HTTP GET/POST/PUT/DELETE
    ↓
CXF Servlet → JaxRsInInterceptor
    ↓
RESTServiceDelegate → SQL 실행
    ↓
ResultSet → JSON/XML 변환
    ↓
JaxRsOutInterceptor → HTTP 응답
```

## 관리 UI 구조

### Dojo 기반 웹 인터페이스

#### 주요 화면

1. **로그인 화면** (`index.jsp`)
   - RSA 암호화된 로그인
   - IP 제한 확인

2. **대시보드** (`mngmain/`)
   - 서비스 현황
   - 로그 통계

3. **SOAP 서비스 관리**
   - 서비스 목록 조회
   - 서비스 생성/수정/삭제
   - 서비스 시작/중지
   - WSDL 다운로드

4. **REST 서비스 관리**
   - 서비스 목록 조회
   - 서비스 생성/수정/삭제
   - 서비스 시작/중지
   - API 문서 조회

5. **로그 조회**
   - 서비스 호출 로그
   - 에러 로그
   - 성능 로그

6. **관리자 설정**
   - 관리자 계정 관리
   - IP 접근 제어

## 개발 가이드

### 새로운 서비스 타입 추가

#### 1. Service Manager 생성

```java
package oasis.engine.svr.jax.custom;

public class CustomServiceManager {

    private static CustomServiceManager instance;

    public static CustomServiceManager getManager() {
        if (instance == null) {
            instance = new CustomServiceManager();
        }
        return instance;
    }

    public IService createService(CustomServiceInfo serviceInfo) {
        // 서비스 생성 로직
    }

    public void startService(String serviceId) {
        // 서비스 시작
    }

    public void stopService(String serviceId) {
        // 서비스 중지
    }
}
```

#### 2. Service Class Generator

```java
package oasis.engine.svr.jax.custom;

public class CustomServiceClass {

    public Class<?> generateServiceClass(CustomServiceInfo serviceInfo) {
        // Javassist를 사용한 동적 클래스 생성
    }
}
```

#### 3. Service Delegate

```java
package oasis.engine.svr.jax.custom;

public class CustomServiceDelegate {

    public Object invoke(String methodName, Object[] args) {
        // 비즈니스 로직 실행
    }
}
```

#### 4. Controller 및 UI 추가

```java
@Controller
@RequestMapping("/custom")
public class CustomController extends MainController {

    @RequestMapping("/list.json")
    public ModelAndView listServices() {
        // 서비스 목록 조회
    }
}
```

### 커스텀 인터셉터 추가

```java
package oasis.extend.cxf.custom;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class CustomInInterceptor extends AbstractPhaseInterceptor<Message> {

    public CustomInInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        // 요청 처리 로직
    }
}
```

## 주의사항

### 1. 동적 클래스 생성

- **PermGen/Metaspace 메모리**: 동적 클래스 생성이 많으면 메모리 부족 발생 가능
- **ClassLoader 관리**: 서비스 재시작 시 이전 클래스 정리 필요
- **성능**: 첫 번째 서비스 생성은 느릴 수 있음 (클래스 생성 오버헤드)

### 2. 동시성

- **서비스 시작/중지**: 동시에 여러 서비스 조작 시 동기화 필요
- **ClassLoader 충돌**: 같은 서비스 ID로 동시 생성 방지

### 3. 데이터베이스

- **메타데이터 캐싱**: 테이블 구조 변경 시 캐시 무효화 필요
- **Connection Pool**: JDBC 연결 관리 주의
- **SQL Injection**: 동적 쿼리 생성 시 파라미터 바인딩 필수

### 4. 레거시 기술

- **Java 6**: 최신 Java 기능 사용 불가
- **Dojo Toolkit**: 구버전 JavaScript 프레임워크
- **DWR**: Ajax 기술의 구세대 방식

## 빌드 및 배포

### Eclipse에서 빌드

1. Eclipse에서 프로젝트 Import
2. JDK 1.6.0_45 설정
3. Tomcat 6.0 서버 설정
4. PUBC 라이브러리 경로 설정
5. Project → Build Project
6. Export → WAR file

### WAR 파일 배포

**배포 대상**:
- Apache Tomcat 6.0+
- WEB-INF/lib에 186개 JAR 포함
- PUBC 의존성 확인 필요

### 환경 설정

1. **데이터소스**: `WEB-INF/config/context-datasource.xml`
2. **OASIS 엔진**: `WEB-INF/config/context-oasis.xml`
3. **로깅**: `WEB-INF/config/log4j.xml`
4. **초기 스키마**: `classpath:schema.sql`

## PUBC와의 통합 관계

OASIS는 PUBC (공통연계모듈)를 **외부 라이브러리**로 통합하여 인증 및 로깅 기능을 사용합니다.

### 패키지 독립성

```
┌─────────────────────────────────────┐
│  OASIS 프로젝트                     │
│  ├── oasis.* (136 files)           │← OASIS 자체 구현
│  │   ├── engine/                   │
│  │   ├── server/                   │
│  │   ├── tools/                    │
│  │   └── extend/                   │
│  │       └── CommonProc.java       │← PUBC 브리지 클래스
│  │                                  │
│  └── WEB-INF/lib/                  │
│      ├── iros_pubc_1.1.jar  ←──────┼─── PUBC 라이브러리 (74 files)
│      ├── iros_cipher.jar           │
│      └── pubc_common_properties.xml│
└─────────────────────────────────────┘
                 │
                 │ import iros.pubc.*
                 ↓
┌─────────────────────────────────────┐
│  PUBC 모듈 (iros_pubc_1.1.jar)     │
│  ├── iros.pubc.* (62 files)        │← 관리 모듈
│  ├── iros.pubr.* (12 files)        │← 포털 연동
│  └── iros.cmm.* (공통)              │
│      ├── usc/ - 사용자 인증         │
│      ├── usi/ - 사용 현황 로깅      │
│      └── cmm/ - 공통 기능           │
└─────────────────────────────────────┘
```

### 통합 방식

| 구분 | OASIS | PUBC |
|-----|-------|------|
| **패키지** | `oasis.*` | `iros.*` |
| **파일 수** | 136 Java 파일 | 74 Java 파일 |
| **역할** | OpenAPI 자동생성 | 인증/로깅/공통기능 |
| **통합 형태** | 메인 프로젝트 | 외부 JAR 의존 |
| **브리지 클래스** | `oasis.extend.CommonProc` | - |

### OASIS가 사용하는 PUBC 기능

#### 1. 인증 처리 (oasis.extend.CommonProc)

**위치**: `oasis/extend/CommonProc.java`

```java
package oasis.extend;

import iros.pubc.cmm.PubcErrorCodeProperties;
import iros.pubc.cmm.PubcProperties;
import iros.pubc.usc.service.UserCrtfcProcessService;
import iros.pubc.usc.service.vo.UserCrtfcVO;
import iros.pubc.usi.service.UseSttusService;

@Resource(name="UserCrtfcProcessService")
private UserCrtfcProcessService userCrtfcProcessService;

@Resource(name="UseSttusService")
private UseSttusService useSttusService;

/**
 * OpenAPI 인증 처리
 */
public UserCrtfcVO authServiceCall(HttpServletRequest request) {
    // PUBC 인증 모듈 호출
    UserCrtfcVO userCrtfcVO = userCrtfcProcessService.userCrtfcProcess(request);

    // 인증 실패 시 로깅
    if(!"00".equals(userCrtfcVO.getTroblTyCode())) {
        useSttusService.insertUseSttus(userCrtfcVO);
    }

    return userCrtfcVO;
}

/**
 * 사용 현황 로깅
 */
public void useSttusServiceCall(UserCrtfcVO userCrtfcVO) {
    if("00".equals(userCrtfcVO.getTroblTyCode())) {
        useSttusService.insertUseSttus(userCrtfcVO);
    }
}
```

#### 2. CXF 인터셉터 연동

**SOAP 인터셉터**: `oasis.extend.cxf.jaxws.*`

```java
import iros.pubc.usc.service.vo.UserCrtfcVO;

// JaxWsInInterceptor.java
public class JaxWsInInterceptor extends AbstractPhaseInterceptor<Message> {
    @Override
    public void handleMessage(Message message) throws Fault {
        // SOAP 요청 전처리
        // UserCrtfcVO를 통한 인증 정보 전달
    }
}

// JaxWsOutInterceptor.java
public class JaxWsOutInterceptor extends AbstractPhaseInterceptor<Message> {
    @Override
    public void handleMessage(Message message) {
        // SOAP 응답 후처리
        // 사용 현황 로깅
    }
}
```

**REST 인터셉터**: `oasis.extend.cxf.jaxrs.*`

```java
import iros.pubc.usc.service.vo.UserCrtfcVO;

// JaxRsInInterceptor.java
public class JaxRsInInterceptor extends AbstractPhaseInterceptor<Message> {
    @Override
    public void handleMessage(Message message) throws Fault {
        // REST 요청 전처리
        // 서비스키 인증
    }
}

// JaxRsOutInterceptor.java
public class JaxRsOutInterceptor extends AbstractPhaseInterceptor<Message> {
    @Override
    public void handleMessage(Message message) {
        // REST 응답 후처리
        // 사용량 통계
    }
}
```

#### 3. 에러 코드 관리

```java
import iros.pubc.cmm.PubcErrorCodeProperties;

// 표준 에러 코드 사용
String errCode = PubcErrorCodeProperties.APPLICATION_ERROR;          // "01"
String dbError = PubcErrorCodeProperties.DB_ERROR;                   // "02"
String invalidParam = PubcErrorCodeProperties.INVALID_REQUEST_PARAMETER_ERROR; // "10"
String serviceKeyError = PubcErrorCodeProperties.SERVICE_KEY_IS_NOT_REGISTERED_ERROR; // "30"
```

### Spring 빈 통합

**OASIS web.xml**:

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
        <!-- OASIS 자체 설정 -->
        /WEB-INF/config/context-*.xml,

        <!-- PUBC 모듈 Spring 설정 (classpath에서 로드) -->
        classpath:iros/pubc/spring/context-aspect-pubc.xml,
        classpath:iros/pubc/spring/context-common-pubc.xml,
        classpath:iros/pubc/spring/context-datasource-pubc.xml,
        classpath:iros/pubc/spring/context-idgen-pubc.xml,
        classpath:iros/pubc/spring/context-sqlMap-pubc.xml,
        classpath:iros/pubc/spring/context-scheduling-pubc.xml
    </param-value>
</context-param>
```

**통합되는 PUBC Spring 빈**:
- `UserCrtfcProcessService`: 사용자 인증 서비스
- `UseSttusService`: 사용 현황 로깅 서비스
- PUBC 데이터소스 (iBATIS SqlMapClient)
- PUBC 트랜잭션 관리자
- PUBC ID 생성기
- PUBC Quartz 스케줄러

### OpenAPI 호출 흐름

#### SOAP 서비스 호출

```
1. 클라이언트 → SOAP 요청
    ↓
2. OASISCXFServlet
    ↓
3. JaxWsInInterceptor
    ↓ (oasis.extend.cxf.jaxws)
4. CommonProc.authServiceCall()
    ↓ (oasis.extend)
5. UserCrtfcProcessService.userCrtfcProcess()
    ↓ (iros.pubc.usc)
6. 인증 성공 → SOAPServiceDelegate 실행
    ↓
7. SQL 쿼리 실행 → 결과 반환
    ↓
8. JaxWsOutInterceptor
    ↓
9. UseSttusService.insertUseSttus()
    ↓ (iros.pubc.usi)
10. SOAP 응답 반환
```

#### REST 서비스 호출

```
1. 클라이언트 → HTTP GET/POST
    ↓
2. OASISCXFServlet
    ↓
3. JaxRsInInterceptor
    ↓ (oasis.extend.cxf.jaxrs)
4. CommonProc.authServiceCall()
    ↓ (oasis.extend)
5. PUBC 인증 모듈 (서비스키 검증)
    ↓
6. 인증 성공 → RESTServiceDelegate 실행
    ↓
7. SQL 쿼리 실행 → JSON/XML 변환
    ↓
8. JaxRsOutInterceptor
    ↓
9. PUBC 로깅 모듈 (사용량 기록)
    ↓
10. HTTP 응답 반환
```

### 배포 시 고려사항

#### 필수 라이브러리

**OASIS WAR 배포 시 WEB-INF/lib에 포함**:

```
WEB-INF/lib/
├── iros_pubc_1.1.jar              ← PUBC 핵심 모듈 (필수)
├── iros_cipher.jar                 ← PUBC 암호화 (필수)
├── pubc_common_properties.xml      ← PUBC 설정 파일
├── [186개 기타 JAR 파일]
```

#### PUBC 설정 파일

**위치**: `WEB-INF/lib/pubc_common_properties.xml`

PUBC 모듈 동작에 필요한 설정:
- 데이터베이스 연결 정보
- ESB 인터페이스 ID
- 기관 코드, 대표 서비스 코드
- 암호화 설정

#### 데이터베이스 스키마

OASIS와 PUBC는 **동일한 데이터베이스를 공유**:
- OASIS 테이블: `TB_OASIS_*` (서비스 메타데이터, 로그)
- PUBC 테이블: `TN_PUBC_*` (인증 정보, 사용 현황)

### 버전 호환성

| 구성요소 | OASIS | PUBC | 호환성 |
|---------|-------|------|--------|
| **Java** | JDK 1.6.0_45 | JDK 1.6.0_45 | ✓ |
| **컴파일 타겟** | Java 5 | Java 5 | ✓ |
| **Spring** | 2.5.6 | 2.5.6 | ✓ |
| **CXF** | 2.3.2 | 2.2.9 | ✓ (호환) |
| **iBATIS** | - | 2.3.4 | ✓ |

**완전 호환**: 동일한 기술 스택으로 구성되어 통합 문제 없음

### 독립 배포 vs 통합 배포

#### 옵션 1: OASIS 통합 배포 (현재 구조)

```
oasis.war
├── oasis.* 패키지 (자체 구현)
└── WEB-INF/lib/iros_pubc_1.1.jar (PUBC)

장점: 단일 WAR 배포, 관리 간소화
단점: PUBC 관리 UI 없음
```

#### 옵션 2: 독립 배포

```
1. pubc.war (PUBC 관리 콘솔)
2. oasis.war (OpenAPI 자동생성툴 + PUBC JAR)

장점: PUBC 관리 콘솔 사용 가능
단점: 두 개의 WAR 관리 필요
설정: 동일한 데이터베이스 공유
```

### 패키지 명명 규칙

| 패키지 접두사 | 소속 | 역할 |
|-------------|------|------|
| `oasis.*` | OASIS | 자동생성 엔진, 관리 UI |
| `iros.pubc.*` | PUBC | 관리 모듈 (62 files) |
| `iros.pubr.*` | PUBC | 포털 연동 (12 files) |
| `iros.cmm.*` | PUBC | 공통 기능 |
| `oasis.extend.*` | OASIS | PUBC 브리지 (통합 레이어) |

**중요**: OASIS는 `iros.*` 패키지를 직접 구현하지 않으며, JAR 파일을 통해서만 접근합니다.

## 관련 문서

- **PUBC.md**: 공통연계모듈 상세 분석
- **CLAUDE.md**: 전체 저장소 가이드
- `(20171219)자동생성툴_개발환경_구축_가이드.docx`: 개발 환경 구축
- `I.OpenAPI자동생성툴설치가이드.docx`: 설치 가이드
- `II.OpenAPI자동생성툴개발자가이드.docx`: 개발자 가이드

## 참고 기술 문서

- Apache CXF 2.x 문서
- Javassist API 문서
- Dojo Toolkit 문서
- DWR 문서
- **PUBC.md**: PUBC 공통연계모듈 가이드

---

**작성일**: 2025년
**기준 버전**: OASIS (2018-02-08)
**연동 모듈**: PUBC Ver.2.3 (iros_pubc_1.1.jar)
