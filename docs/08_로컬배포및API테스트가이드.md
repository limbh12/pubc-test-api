# 로컬 배포 및 API 테스트 가이드

**PUBC Test API 프로젝트**

현재 설치된 JBoss 환경에서 애플리케이션을 배포하고 API를 테스트하는 가이드입니다.

---

## 📋 목차

1. [사전 준비 확인](#1-사전-준비-확인)
2. [애플리케이션 빌드](#2-애플리케이션-빌드)
3. [JBoss 배포](#3-jboss-배포)
4. [서버 시작 및 확인](#4-서버-시작-및-확인)
5. [API 테스트](#5-api-테스트)
6. [문제 해결](#6-문제-해결)
7. [서버 종료](#7-서버-종료)

---

## 1. 사전 준비 확인

### 1.1 환경 확인

```bash
# 프로젝트 루트 디렉토리로 이동
cd /Users/byunglim/Prj_Claude/PubcTestApi

# JDK 설치 확인
ls -la env/jdk/

# JBoss 설치 확인
ls -la env/jboss/

# 환경 변수 확인
source bin/setenv.sh
echo "JAVA_HOME: $JAVA_HOME"
echo "JBOSS_HOME: $JBOSS_HOME"
```

**예상 출력**:
```
JAVA_HOME: /Users/byunglim/Prj_Claude/PubcTestApi/env/jdk/zulu8.90.0.19-ca-jdk8.0.472-macosx_aarch64
JBOSS_HOME: /Users/byunglim/Prj_Claude/PubcTestApi/env/jboss/wildfly-26.1.3.Final
```

### 1.2 필수 도구 확인

```bash
# Maven 설치 확인
mvn --version

# curl 확인 (API 테스트용)
curl --version

# jq 설치 (JSON 포맷팅용, 선택사항)
brew install jq  # macOS
```

---

## 2. 애플리케이션 빌드

### 2.1 Maven 빌드

```bash
# 프로젝트 루트에서 실행
cd /Users/byunglim/Prj_Claude/PubcTestApi

# 클린 빌드 (테스트 스킵)
mvn clean package -DskipTests

# 또는 테스트 포함 빌드
mvn clean package
```

**빌드 성공 시**:
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
[INFO] Finished at: 2025-11-13T...
[INFO] ------------------------------------------------------------------------
```

### 2.2 WAR 파일 확인

```bash
# WAR 파일 생성 확인
ls -lh target/pubc-test-api.war

# WAR 파일 내용 확인 (선택사항)
jar -tf target/pubc-test-api.war | head -20
```

---

## 3. JBoss 배포

### 3.1 배포 디렉토리 확인

```bash
# 배포 디렉토리 확인
ls -la env/jboss/wildfly-26.1.3.Final/standalone/deployments/
```

### 3.2 WAR 파일 배포

**방법 1: 자동 배포 (Hot Deployment)**

```bash
# WAR 파일을 deployments 디렉토리로 복사
cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 배포 마커 파일 확인 (서버 실행 중이면 자동 생성)
watch -n 2 'ls -lt env/jboss/wildfly-26.1.3.Final/standalone/deployments/'
```

**방법 2: 수동 배포 (서버 종료 상태)**

```bash
# 기존 배포 파일 삭제 (있다면)
rm -rf env/jboss/wildfly-26.1.3.Final/standalone/deployments/pubc-test-api.war*

# WAR 파일 복사
cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 서버 시작 후 자동 배포됨
```

### 3.3 배포 마커 파일 이해

JBoss는 다음 마커 파일로 배포 상태를 표시합니다:

- **`pubc-test-api.war.isdeploying`**: 배포 진행 중
- **`pubc-test-api.war.deployed`**: 배포 성공
- **`pubc-test-api.war.failed`**: 배포 실패
- **`pubc-test-api.war.undeployed`**: 배포 해제됨

---

## 4. 서버 시작 및 확인

### 4.1 JBoss 서버 시작

```bash
# Standalone 모드로 시작
./bin/jboss-start.sh
```

**서버 시작 로그 확인**:
```
==========================================
JBoss EAP Standalone 모드 시작
==========================================

JBoss 홈: /Users/byunglim/Prj_Claude/PubcTestApi/env/jboss/wildfly-26.1.3.Final

서버 설정:
  Bind Address: 0.0.0.0
  Port Offset: 0
  Config: standalone.xml

서버를 시작합니다...

=========================================================================

  JBoss Bootstrap Environment

  JBOSS_HOME: /Users/byunglim/Prj_Claude/PubcTestApi/env/jboss/wildfly-26.1.3.Final

  JAVA: /Users/byunglim/Prj_Claude/PubcTestApi/env/jdk/zulu8.90.0.19-ca-jdk8.0.472-macosx_aarch64/bin/java

...

14:30:00,123 INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: WildFly Full 26.1.3.Final (WildFly Core 18.1.2.Final) started in 5432ms
```

### 4.2 배포 상태 확인

**터미널에서 로그 확인**:
```bash
# 실시간 로그 모니터링 (별도 터미널)
tail -f logs/server.log

# 또는 env/jboss/wildfly-26.1.3.Final/standalone/log/server.log
tail -f env/jboss/wildfly-26.1.3.Final/standalone/log/server.log
```

**배포 성공 로그 예시**:
```
14:30:15,234 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-1) WFLYSRV0027: Starting deployment of "pubc-test-api.war" (runtime-name: "pubc-test-api.war")
14:30:18,456 INFO  [org.springframework.web.context.ContextLoader] (ServerService Thread Pool -- 78) Root WebApplicationContext: initialization started
14:30:22,789 INFO  [org.springframework.web.context.ContextLoader] (ServerService Thread Pool -- 78) Root WebApplicationContext initialized in 4321 ms
14:30:23,012 INFO  [org.jboss.as.server] (DeploymentScanner-threads - 1) WFLYSRV0010: Deployed "pubc-test-api.war" (runtime-name : "pubc-test-api.war")
```

### 4.3 서버 정상 동작 확인

```bash
# 관리 콘솔 확인 (브라우저)
open http://localhost:9990/

# 애플리케이션 접근 확인
curl -I http://localhost:8080/pubc-test-api/
```

**예상 응답**:
```
HTTP/1.1 200 OK
Content-Type: text/html
...
```

---

## 5. API 테스트

### 5.1 테스트 준비

**유효한 테스트 서비스키** (MockCommonProc.java 참조):
- `TEST_KEY_001`
- `TEST_KEY_002`
- `DEMO_KEY`
- `DEV_KEY`

### 5.2 REST API 테스트

#### 5.2.1 헬스 체크

```bash
# 기본 접근 확인
curl http://localhost:8080/pubc-test-api/

# API 엔드포인트 확인
curl http://localhost:8080/pubc-test-api/api/
```

#### 5.2.2 문화시설 목록 조회

**기본 조회**:
```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001"
```

**시설 유형별 조회 (박물관)**:
```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001&facilityType=박물관"
```

**지역별 조회 (서울)**:
```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001&regionCode=11"
```

**페이징 조회**:
```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001&pageNum=1&pageSize=10"
```

**JSON 포맷팅 (jq 사용)**:
```bash
curl -s -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001&pageSize=5" | jq '.'
```

**예상 응답**:
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
      "phone": "02-2077-9000",
      "latitude": 37.5240,
      "longitude": 126.9802,
      "openTime": "월~일 10:00-18:00 (수,토 10:00-21:00)",
      "homepage": "https://www.museum.go.kr",
      "manageAgency": "문화체육관광부",
      "updateDate": "2025-11-13T14:30:00"
    }
  ]
}
```

#### 5.2.3 문화시설 상세 조회

```bash
# 특정 시설 상세 정보
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities/FAC001?serviceKey=TEST_KEY_001" | jq '.'
```

**예상 응답**:
```json
{
  "facilityId": "FAC001",
  "facilityName": "국립중앙박물관",
  "facilityType": "박물관",
  "regionCode": "11",
  "address": "서울특별시 용산구 서빙고로 137",
  "phone": "02-2077-9000",
  "latitude": 37.5240,
  "longitude": 126.9802,
  "openTime": "월~일 10:00-18:00 (수,토 10:00-21:00)",
  "homepage": "https://www.museum.go.kr",
  "manageAgency": "문화체육관광부",
  "updateDate": "2025-11-13T14:30:00"
}
```

#### 5.2.4 시설 유형 목록 조회

```bash
# 지원되는 시설 유형 목록
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities/types?serviceKey=TEST_KEY_001" | jq '.'
```

**예상 응답**:
```json
{
  "types": [
    {
      "code": "박물관",
      "name": "박물관",
      "count": 20
    },
    {
      "code": "미술관",
      "name": "미술관",
      "count": 15
    },
    {
      "code": "도서관",
      "name": "도서관",
      "count": 15
    }
  ]
}
```

### 5.3 에러 케이스 테스트

#### 5.3.1 인증 실패 (잘못된 서비스키)

```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=INVALID_KEY"
```

**예상 응답** (HTTP 401):
```json
{
  "code": "401",
  "message": "유효하지 않은 서비스 키입니다",
  "timestamp": "2025-11-13T14:30:00"
}
```

#### 5.3.2 서비스키 누락

```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities"
```

**예상 응답** (HTTP 400):
```json
{
  "code": "400",
  "message": "서비스 키가 필요합니다",
  "timestamp": "2025-11-13T14:30:00"
}
```

#### 5.3.3 존재하지 않는 시설

```bash
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities/INVALID_ID?serviceKey=TEST_KEY_001"
```

**예상 응답** (HTTP 404):
```json
{
  "code": "404",
  "message": "시설을 찾을 수 없습니다",
  "timestamp": "2025-11-13T14:30:00"
}
```

### 5.4 통합 테스트 스크립트

**테스트 스크립트** (`scripts/test-api.sh`):

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/pubc-test-api/api"
SERVICE_KEY="TEST_KEY_001"

echo "=========================================="
echo "PUBC Test API - 통합 테스트"
echo "=========================================="
echo ""

# 1. 기본 접근 테스트
echo "1. 기본 접근 테스트..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/pubc-test-api/
echo ""

# 2. 문화시설 목록 조회
echo "2. 문화시설 목록 조회 (전체)..."
curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageSize=5" | jq '.totalCount, .items[0].facilityName'
echo ""

# 3. 시설 유형별 조회
echo "3. 시설 유형별 조회 (박물관)..."
curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&facilityType=박물관&pageSize=3" | jq '.totalCount'
echo ""

# 4. 지역별 조회
echo "4. 지역별 조회 (서울)..."
curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&regionCode=11&pageSize=3" | jq '.totalCount'
echo ""

# 5. 상세 조회
echo "5. 문화시설 상세 조회 (FAC001)..."
curl -s "${BASE_URL}/facilities/FAC001?serviceKey=${SERVICE_KEY}" | jq '.facilityName, .address'
echo ""

# 6. 시설 유형 목록
echo "6. 시설 유형 목록..."
curl -s "${BASE_URL}/facilities/types?serviceKey=${SERVICE_KEY}" | jq '.types[].name'
echo ""

# 7. 인증 실패 테스트
echo "7. 인증 실패 테스트..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" "${BASE_URL}/facilities?serviceKey=INVALID_KEY"
echo ""

echo "=========================================="
echo "테스트 완료"
echo "=========================================="
```

**실행**:
```bash
chmod +x scripts/test-api.sh
./scripts/test-api.sh
```

### 5.5 Postman/Insomnia 테스트

**Postman Collection 예시** (`scripts/pubc-test-api.postman_collection.json`):

```json
{
  "info": {
    "name": "PUBC Test API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "문화시설 목록 조회",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001&pageSize=10",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["pubc-test-api", "api", "facilities"],
          "query": [
            {"key": "serviceKey", "value": "TEST_KEY_001"},
            {"key": "pageSize", "value": "10"}
          ]
        }
      }
    },
    {
      "name": "문화시설 상세 조회",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/pubc-test-api/api/facilities/FAC001?serviceKey=TEST_KEY_001",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["pubc-test-api", "api", "facilities", "FAC001"],
          "query": [
            {"key": "serviceKey", "value": "TEST_KEY_001"}
          ]
        }
      }
    },
    {
      "name": "시설 유형 목록",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "http://localhost:8080/pubc-test-api/api/facilities/types?serviceKey=TEST_KEY_001",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["pubc-test-api", "api", "facilities", "types"],
          "query": [
            {"key": "serviceKey", "value": "TEST_KEY_001"}
          ]
        }
      }
    }
  ]
}
```

---

## 6. 문제 해결

### 6.1 서버 시작 실패

**증상**: JBoss가 시작되지 않음

**해결 방법**:

```bash
# 1. 포트 충돌 확인
lsof -i :8080
lsof -i :9990

# 2. 이미 실행 중인 JBoss 종료
./bin/jboss-stop.sh

# 3. 프로세스 강제 종료 (필요 시)
pkill -9 -f jboss

# 4. 로그 확인
tail -50 env/jboss/wildfly-26.1.3.Final/standalone/log/server.log
```

### 6.2 배포 실패

**증상**: WAR 파일이 배포되지 않음

**해결 방법**:

```bash
# 1. 배포 마커 파일 확인
ls -la env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 2. 실패 로그 확인 (.failed 파일이 있으면)
cat env/jboss/wildfly-26.1.3.Final/standalone/deployments/pubc-test-api.war.failed

# 3. 기존 배포 완전 삭제 후 재배포
rm -rf env/jboss/wildfly-26.1.3.Final/standalone/deployments/pubc-test-api.war*
cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 4. 서버 재시작
./bin/jboss-stop.sh
./bin/jboss-start.sh
```

### 6.3 API 호출 실패

**증상**: 404 Not Found 또는 500 Internal Server Error

**해결 방법**:

```bash
# 1. 배포 상태 확인
curl -I http://localhost:8080/pubc-test-api/

# 2. 로그 확인
tail -100 logs/server.log | grep ERROR

# 3. 애플리케이션 컨텍스트 확인
curl http://localhost:8080/pubc-test-api/api/

# 4. Spring 컨텍스트 초기화 확인 (로그에서)
grep "Root WebApplicationContext" env/jboss/wildfly-26.1.3.Final/standalone/log/server.log
```

### 6.4 인증 오류

**증상**: 유효한 서비스키인데 401 에러

**해결 방법**:

```bash
# 1. 서비스키 확인 (대소문자 구분)
# MockCommonProc.java에 정의된 키 확인
grep "TEST_KEY" src/main/java/iros/test/user/mock/MockCommonProc.java

# 2. URL 인코딩 확인
curl -X GET "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001"

# 3. 로그에서 인증 실패 원인 확인
tail -50 logs/server.log | grep -i auth
```

### 6.5 성능 문제

**증상**: API 응답이 느림

**해결 방법**:

```bash
# 1. JVM 힙 메모리 확인
jps -v | grep jboss

# 2. 힙 메모리 조정 (bin/jboss-start.sh 수정)
# JAVA_OPTS="-Xms1024m -Xmx2048m" → "-Xms2048m -Xmx4096m"

# 3. 쓰레드 덤프 확인
jstack <jboss-pid> > thread-dump.txt

# 4. GC 로그 활성화 (bin/jboss-start.sh에 추가)
export JAVA_OPTS="$JAVA_OPTS -verbose:gc -Xloggc:$PROJECT_ROOT/logs/gc.log"
```

### 6.6 로그 확인 방법

```bash
# 애플리케이션 로그
tail -f logs/server.log

# JBoss 서버 로그
tail -f env/jboss/wildfly-26.1.3.Final/standalone/log/server.log

# 특정 패턴 검색
grep -i "error\|exception" logs/server.log

# 최근 30분 로그만
find logs/ -name "*.log" -mmin -30 -exec tail -100 {} \;
```

---

## 7. 서버 종료

### 7.1 정상 종료

```bash
# 프로젝트 루트에서 실행
./bin/jboss-stop.sh
```

**종료 로그**:
```
==========================================
JBoss EAP 종료
==========================================

JBoss PID: 12345 종료 시도...
JBoss가 정상적으로 종료되었습니다.
```

### 7.2 강제 종료 (응답 없을 시)

```bash
# JBoss 프로세스 찾기
ps aux | grep jboss

# 강제 종료
pkill -9 -f jboss

# 또는 PID로 종료
kill -9 <jboss-pid>
```

### 7.3 재배포 (Hot Deployment)

서버를 종료하지 않고 재배포:

```bash
# 1. WAR 파일 다시 빌드
mvn clean package -DskipTests

# 2. 기존 배포 삭제
rm env/jboss/wildfly-26.1.3.Final/standalone/deployments/pubc-test-api.war.deployed

# 3. 새 WAR 복사 (자동 재배포)
cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/

# 4. 재배포 확인
watch -n 1 'ls -lt env/jboss/wildfly-26.1.3.Final/standalone/deployments/'
```

---

## 8. 빠른 참조

### 8.1 주요 명령어

| 작업 | 명령어 |
|------|--------|
| 빌드 | `mvn clean package -DskipTests` |
| 배포 | `cp target/pubc-test-api.war env/jboss/wildfly-26.1.3.Final/standalone/deployments/` |
| 서버 시작 | `./bin/jboss-start.sh` |
| 서버 종료 | `./bin/jboss-stop.sh` |
| 로그 확인 | `tail -f logs/server.log` |
| 통합 테스트 | `./scripts/test-api.sh` |
| API 테스트 | `curl "http://localhost:8080/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001"` |

### 8.2 주요 URL

| 항목 | URL |
|------|-----|
| 애플리케이션 | http://localhost:8080/pubc-test-api/ |
| REST API | http://localhost:8080/pubc-test-api/api/facilities |
| 관리 콘솔 | http://localhost:9990/ |

### 8.3 유효한 서비스키

- `TEST_KEY_001`
- `TEST_KEY_002`
- `DEMO_KEY`
- `DEV_KEY`

### 8.4 지역 코드

| 코드 | 지역 |
|------|------|
| 11 | 서울특별시 |
| 26 | 부산광역시 |
| 27 | 대구광역시 |
| 28 | 인천광역시 |
| 29 | 광주광역시 |
| 30 | 대전광역시 |
| 31 | 울산광역시 |
| 41 | 경기도 |
| 42 | 강원도 |
| 43 | 충청북도 |
| 44 | 충청남도 |
| 45 | 전라북도 |
| 46 | 전라남도 |
| 47 | 경상북도 |
| 48 | 경상남도 |
| 50 | 제주특별자치도 |

### 8.5 시설 유형

- `박물관` (museum)
- `미술관` (art gallery)
- `도서관` (library)

---

## 9. 다음 단계

✅ **완료한 작업**:
- [x] 로컬 환경 구축
- [x] 애플리케이션 빌드
- [x] JBoss 배포
- [x] API 테스트

🔜 **추가 작업**:
- [ ] SOAP 웹 서비스 테스트 (3번 섹션 참고)
- [ ] 실제 PUBC 모듈 연동 (운영 환경)
- [ ] 실제 CUBRID 데이터베이스 연동
- [ ] 성능 테스트 및 튜닝
- [ ] 보안 설정 강화

---

## 10. 참고 문서

- **API 명세서**: `docs/03_API명세서.md`
- **시스템 아키텍처**: `docs/02_시스템아키텍처설계서.md`
- **환경 설치 가이드**: `docs/99_로컬환경설치가이드.md`
- **기술 스택**: `docs/00_기술스택_JDK18.md`
- **Copilot 지침**: `.github/copilot-instructions.md`

---

**문서 버전**: 1.0  
**작성일**: 2025-11-13  
**최종 수정**: 2025-11-13
