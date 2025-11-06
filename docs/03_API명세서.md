# API 명세서
## PUBC 연동 테스트 프로젝트

**작성일**: 2025-11-04
**프로젝트명**: PUBC-Test-API
**버전**: 2.0 (JDK 1.8)
**Base URL**: `http://localhost:8080/pubc-test-api`
**API 유형**: 조회 전용 (Read-Only)

> **📌 기술 스택**: 이 문서는 **[JDK 1.8 기술 스택](./00_기술스택_JDK18.md)** 기준으로 작성되었습니다.
> **📌 API 특징**: 이 프로젝트는 **공공데이터 조회 전용 API**입니다. 모든 엔드포인트는 GET 요청만 지원합니다.

---

## 목차

1. [공통 사항](#1-공통-사항)
2. [REST API 명세](#2-rest-api-명세)
3. [SOAP 웹 서비스 명세](#3-soap-웹-서비스-명세)
4. [에러 코드](#4-에러-코드)
5. [테스트 시나리오](#5-테스트-시나리오)

---

## 1. 공통 사항

### 1.1 인증 방식

모든 API는 **서비스키(serviceKey)** 기반 인증을 사용합니다.

**서비스키 전달 방법**:
- **Query Parameter**: `?serviceKey=YOUR_SERVICE_KEY`
- **HTTP Header**: `X-Service-Key: YOUR_SERVICE_KEY` (선택 구현)

**인증 흐름**:
```
1. 클라이언트가 serviceKey를 요청에 포함하여 API 호출
2. PUBC 인증 모듈이 serviceKey 검증
3. 검증 성공 시 비즈니스 로직 실행
4. 검증 실패 시 에러 응답 반환
5. 모든 호출 내역은 자동으로 로깅
```

### 1.2 응답 포맷

#### REST API

**성공 응답** (HTTP 200):
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    // 실제 데이터
  }
}
```

**에러 응답** (HTTP 4xx/5xx):
```json
{
  "code": "030",
  "msg": "서비스키가 등록되지 않았습니다.",
  "data": null
}
```

#### SOAP 웹 서비스

**WSDL 위치**: `http://localhost:8080/pubc-test-api/services/UserService?wsdl`

**SOAP Envelope 예시**:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns:getUserListResponse xmlns:ns="http://service.soap.test.pubc.iros/">
      <code>000</code>
      <msg>OK</msg>
      <users>
        <!-- 사용자 목록 -->
      </users>
    </ns:getUserListResponse>
  </soap:Body>
</soap:Envelope>
```

### 1.3 날짜/시간 포맷

- **날짜**: `YYYY-MM-DD` (예: `2025-11-03`)
- **날짜+시간**: `YYYY-MM-DD HH:mm:ss` (예: `2025-11-03 14:30:00`)
- **타임존**: 서버 로컬 시간 (KST)

### 1.4 페이징

**페이징 파라미터**:
- `pageNo`: 페이지 번호 (1부터 시작, 기본값: 1)
- `numOfRows`: 페이지당 건수 (기본값: 10, 최대값: 100)

**페이징 응답**:
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    "totalCount": 150,
    "pageNo": 1,
    "numOfRows": 10,
    "items": [/* 실제 데이터 */]
  }
}
```

### 1.5 문자 인코딩

- **요청/응답**: UTF-8
- **Content-Type**:
  - REST JSON: `application/json; charset=UTF-8`
  - REST XML: `application/xml; charset=UTF-8`
  - SOAP: `text/xml; charset=UTF-8`

---

## 2. REST API 명세

### 2.1 문화시설 목록 조회

문화시설 목록을 페이징하여 조회합니다. 시설 유형, 지역, 시설명으로 필터링 가능합니다.

#### 기본 정보
- **URL**: `/api/cultureFacilities`
- **Method**: `GET` (조회 전용)
- **인증**: 필수 (serviceKey)
- **Content-Type**: `application/json` 또는 `application/xml`

#### 요청 파라미터

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| serviceKey | String | O | - | 서비스 인증키 |
| facilityType | String | X | - | 시설 유형 (박물관, 미술관, 도서관 등) |
| regionCode | String | X | - | 지역코드 (11:서울, 26:부산, 41:경기 등) |
| facilityName | String | X | - | 시설명 검색 (부분일치) |
| pageNo | Integer | X | 1 | 페이지 번호 (1부터 시작) |
| numOfRows | Integer | X | 10 | 페이지당 건수 (최대 100) |

**지역코드 목록**:
- `11`: 서울특별시
- `26`: 부산광역시
- `27`: 대구광역시
- `28`: 인천광역시
- `29`: 광주광역시
- `30`: 대전광역시
- `31`: 울산광역시
- `36`: 세종특별자치시
- `41`: 경기도
- `42`: 강원도
- `43`: 충청북도
- `44`: 충청남도
- `45`: 전라북도
- `46`: 전라남도
- `47`: 경상북도
- `48`: 경상남도
- `50`: 제주특별자치도

#### 요청 예시

**JSON 요청 (서울 지역 박물관 조회)**:
```
GET /api/cultureFacilities?serviceKey=test123&facilityType=박물관&regionCode=11&pageNo=1&numOfRows=10
Accept: application/json
```

**XML 요청 (시설명 검색)**:
```
GET /api/cultureFacilities?serviceKey=test123&facilityName=국립&pageNo=1&numOfRows=10
Accept: application/xml
```

**전체 조회 (필터 없음)**:
```
GET /api/cultureFacilities?serviceKey=test123&pageNo=1&numOfRows=10
Accept: application/json
```

#### 응답

**성공 응답 (HTTP 200)**:
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    "totalCount": 4,
    "pageNo": 1,
    "numOfRows": 10,
    "items": [
      {
        "facilityId": "F001",
        "facilityName": "국립중앙박물관",
        "facilityType": "박물관",
        "address": "서울특별시 용산구 서빙고로 137",
        "phone": "02-2077-9000",
        "regionCode": "11",
        "updateDate": "2025-01-15"
      },
      {
        "facilityId": "F002",
        "facilityName": "국립현대미술관 서울관",
        "facilityType": "미술관",
        "address": "서울특별시 종로구 삼청로 30",
        "phone": "02-3701-9500",
        "regionCode": "11",
        "updateDate": "2025-01-15"
      },
      {
        "facilityId": "F003",
        "facilityName": "서울시립미술관",
        "facilityType": "미술관",
        "address": "서울특별시 중구 덕수궁길 61",
        "phone": "02-2124-8800",
        "regionCode": "11",
        "updateDate": "2025-01-15"
      },
      {
        "facilityId": "F004",
        "facilityName": "국립중앙도서관",
        "facilityType": "도서관",
        "address": "서울특별시 서초구 반포대로 201",
        "phone": "02-535-4142",
        "regionCode": "11",
        "updateDate": "2025-01-15"
      }
    ]
  }
}
```

**XML 응답**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <code>000</code>
  <msg>OK</msg>
  <data>
    <totalCount>4</totalCount>
    <pageNo>1</pageNo>
    <numOfRows>10</numOfRows>
    <items>
      <item>
        <facilityId>F001</facilityId>
        <facilityName>국립중앙박물관</facilityName>
        <facilityType>박물관</facilityType>
        <address>서울특별시 용산구 서빙고로 137</address>
        <phone>02-2077-9000</phone>
        <regionCode>11</regionCode>
        <updateDate>2025-01-15</updateDate>
      </item>
    </items>
  </data>
</response>
```

**에러 응답**:

*서비스키 미등록 (HTTP 401)*:
```json
{
  "code": "030",
  "msg": "서비스키가 등록되지 않았습니다.",
  "data": null
}
```

*파라미터 오류 (HTTP 400)*:
```json
{
  "code": "010",
  "msg": "요청 파라미터가 올바르지 않습니다.",
  "data": {
    "invalidParam": "regionCode",
    "message": "유효하지 않은 지역코드입니다."
  }
}
```

*데이터 없음 (HTTP 200)*:
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    "totalCount": 0,
    "pageNo": 1,
    "numOfRows": 10,
    "items": []
  }
}
```

---

### 2.2 문화시설 상세 조회

특정 문화시설의 상세 정보를 조회합니다. GPS 좌표, 운영시간, 홈페이지 등 전체 정보를 제공합니다.

#### 기본 정보
- **URL**: `/api/cultureFacilities/{facilityId}`
- **Method**: `GET` (조회 전용)
- **인증**: 필수 (serviceKey)
- **Content-Type**: `application/json` 또는 `application/xml`

#### 경로 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| facilityId | String | O | 조회할 시설 ID (예: F001) |

#### 쿼리 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| serviceKey | String | O | 서비스 인증키 |

#### 요청 예시

```
GET /api/cultureFacilities/F001?serviceKey=test123
Accept: application/json
```

#### 응답

**성공 응답 (HTTP 200)**:
```json
{
  "code": "000",
  "msg": "OK",
  "data": {
    "facilityId": "F001",
    "facilityName": "국립중앙박물관",
    "facilityType": "박물관",
    "address": "서울특별시 용산구 서빙고로 137",
    "phone": "02-2077-9000",
    "latitude": 37.5240123,
    "longitude": 126.9803456,
    "openTime": "10:00-18:00 (월요일 휴관)",
    "homepage": "https://www.museum.go.kr",
    "regionCode": "11",
    "manageAgency": "문화체육관광부",
    "updateDate": "2025-01-15"
  }
}
```

**XML 응답**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<response>
  <code>000</code>
  <msg>OK</msg>
  <data>
    <facilityId>F001</facilityId>
    <facilityName>국립중앙박물관</facilityName>
    <facilityType>박물관</facilityType>
    <address>서울특별시 용산구 서빙고로 137</address>
    <phone>02-2077-9000</phone>
    <latitude>37.5240123</latitude>
    <longitude>126.9803456</longitude>
    <openTime>10:00-18:00 (월요일 휴관)</openTime>
    <homepage>https://www.museum.go.kr</homepage>
    <regionCode>11</regionCode>
    <manageAgency>문화체육관광부</manageAgency>
    <updateDate>2025-01-15</updateDate>
  </data>
</response>
```

**에러 응답**:

*시설 없음 (HTTP 404)*:
```json
{
  "code": "003",
  "msg": "해당 시설을 찾을 수 없습니다.",
  "data": null
}
```

*잘못된 시설 ID 형식 (HTTP 400)*:
```json
{
  "code": "010",
  "msg": "요청 파라미터가 올바르지 않습니다.",
  "data": {
    "invalidParam": "facilityId",
    "message": "시설 ID 형식이 올바르지 않습니다."
  }
}
```

---

## 3. SOAP 웹 서비스 명세

### 3.1 서비스 정보

- **서비스명**: CultureFacilityService
- **WSDL**: `http://localhost:8080/pubc-test-api/services/CultureFacilityService?wsdl`
- **네임스페이스**: `http://service.soap.test.pubc.iros/`
- **포트**: CultureFacilityServicePort
- **바인딩**: SOAP 1.1
- **서비스 유형**: 조회 전용 (Read-Only)

### 3.2 문화시설 목록 조회 (getCultureFacilityList)

#### 요청 메시지

**SOAP Request**:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.soap.test.pubc.iros/">
  <soapenv:Header/>
  <soapenv:Body>
    <ser:getCultureFacilityList>
      <serviceKey>test123</serviceKey>
      <facilityType>박물관</facilityType>
      <regionCode>11</regionCode>
      <facilityName></facilityName>
      <pageNo>1</pageNo>
      <numOfRows>10</numOfRows>
    </ser:getCultureFacilityList>
  </soapenv:Body>
</soapenv:Envelope>
```

**파라미터**:

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| serviceKey | string | O | - | 서비스 인증키 |
| facilityType | string | X | - | 시설 유형 |
| regionCode | string | X | - | 지역 코드 |
| facilityName | string | X | - | 시설명 검색 |
| pageNo | int | X | 1 | 페이지 번호 |
| numOfRows | int | X | 10 | 페이지당 건수 |

#### 응답 메시지

**SOAP Response (성공)**:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns:getCultureFacilityListResponse xmlns:ns="http://service.soap.test.pubc.iros/">
      <code>000</code>
      <msg>OK</msg>
      <totalCount>4</totalCount>
      <pageNo>1</pageNo>
      <numOfRows>10</numOfRows>
      <facilities>
        <facility>
          <facilityId>F001</facilityId>
          <facilityName>국립중앙박물관</facilityName>
          <facilityType>박물관</facilityType>
          <address>서울특별시 용산구 서빙고로 137</address>
          <phone>02-2077-9000</phone>
          <regionCode>11</regionCode>
          <updateDate>2025-01-15T00:00:00</updateDate>
        </facility>
        <facility>
          <facilityId>F004</facilityId>
          <facilityName>국립중앙도서관</facilityName>
          <facilityType>도서관</facilityType>
          <address>서울특별시 서초구 반포대로 201</address>
          <phone>02-535-4142</phone>
          <regionCode>11</regionCode>
          <updateDate>2025-01-15T00:00:00</updateDate>
        </facility>
      </facilities>
    </ns:getCultureFacilityListResponse>
  </soap:Body>
</soap:Envelope>
```

**SOAP Fault (에러)**:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <soap:Fault>
      <faultcode>soap:Server</faultcode>
      <faultstring>서비스키가 등록되지 않았습니다.</faultstring>
      <detail>
        <errorCode>030</errorCode>
      </detail>
    </soap:Fault>
  </soap:Body>
</soap:Envelope>
```

---

### 3.3 문화시설 상세 조회 (getCultureFacility)

#### 요청 메시지

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.soap.test.pubc.iros/">
  <soapenv:Header/>
  <soapenv:Body>
    <ser:getCultureFacility>
      <serviceKey>test123</serviceKey>
      <facilityId>F001</facilityId>
    </ser:getCultureFacility>
  </soapenv:Body>
</soapenv:Envelope>
```

**파라미터**:

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| serviceKey | string | O | 서비스 인증키 |
| facilityId | string | O | 시설 ID |

#### 응답 메시지

**SOAP Response (성공)**:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns:getCultureFacilityResponse xmlns:ns="http://service.soap.test.pubc.iros/">
      <code>000</code>
      <msg>OK</msg>
      <facility>
        <facilityId>F001</facilityId>
        <facilityName>국립중앙박물관</facilityName>
        <facilityType>박물관</facilityType>
        <address>서울특별시 용산구 서빙고로 137</address>
        <phone>02-2077-9000</phone>
        <latitude>37.5240123</latitude>
        <longitude>126.9803456</longitude>
        <openTime>10:00-18:00 (월요일 휴관)</openTime>
        <homepage>https://www.museum.go.kr</homepage>
        <regionCode>11</regionCode>
        <manageAgency>문화체육관광부</manageAgency>
        <updateDate>2025-01-15T00:00:00</updateDate>
      </facility>
    </ns:getCultureFacilityResponse>
  </soap:Body>
</soap:Envelope>
```

**SOAP Fault (에러 - 시설 없음)**:
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <soap:Fault>
      <faultcode>soap:Server</faultcode>
      <faultstring>해당 시설을 찾을 수 없습니다.</faultstring>
      <detail>
        <errorCode>003</errorCode>
      </detail>
    </soap:Fault>
  </soap:Body>
</soap:Envelope>
```

---

## 4. 에러 코드

### 4.1 PUBC 표준 에러 코드

| 코드 | HTTP 상태 | 분류 | 메시지 |
|------|-----------|------|--------|
| 00 | 200 OK | 정상 | 정상 처리되었습니다. |
| 01 | 500 | 시스템 오류 | 애플리케이션 오류가 발생했습니다. |
| 02 | 500 | 시스템 오류 | 데이터베이스 오류가 발생했습니다. |
| 03 | 404 | 시스템 오류 | 요청한 데이터가 존재하지 않습니다. |
| 04 | 500 | 시스템 오류 | HTTP 오류가 발생했습니다. |
| 05 | 504 | 시스템 오류 | 서비스 타임아웃이 발생했습니다. |
| 10 | 400 | 요청 오류 | 요청 파라미터가 올바르지 않습니다. |
| 11 | 400 | 요청 오류 | 필수 요청 파라미터가 없습니다. |
| 12 | 404 | 요청 오류 | 해당 오픈API 서비스가 없습니다. |
| 20 | 403 | 접근 제한 | 서비스 접근이 거부되었습니다. |
| 21 | 403 | 접근 제한 | 일시적으로 사용할 수 없는 서비스키입니다. |
| 22 | 429 | 접근 제한 | 서비스 요청 제한 횟수를 초과했습니다. |
| 30 | 401 | 서비스키 오류 | 서비스키가 등록되지 않았습니다. |
| 31 | 401 | 서비스키 오류 | 기한이 만료된 서비스키입니다. |
| 32 | 403 | 서비스키 오류 | 등록되지 않은 IP에서 요청하였습니다. |
| 33 | 401 | 서비스키 오류 | 서명되지 않은 호출입니다. |
| 99 | 500 | 기타 오류 | 기타 오류가 발생했습니다. |

### 4.2 커스�om 에러 코드 (애플리케이션)

| 코드 | HTTP 상태 | 분류 | 메시지 |
|------|-----------|------|--------|
| 000 | 200 | 성공 | 정상 처리되었습니다. |
| 099 | 409 | 비즈니스 로직 | 이미 존재하는 데이터입니다. |

---

## 5. 테스트 시나리오

### 5.1 REST API 테스트

#### 시나리오 1: 정상 흐름 테스트 (조회 전용)

```bash
# 1. 문화시설 전체 목록 조회
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&pageNo=1&numOfRows=10" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, code="000", totalCount >= 1

# 2. 서울 지역 박물관 필터링 조회
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&facilityType=박물관&regionCode=11&pageNo=1&numOfRows=10" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, items 배열에 서울 지역 박물관만 포함

# 3. 시설명 검색 ("국립" 포함)
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&facilityName=국립&pageNo=1&numOfRows=10" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, facilityName에 "국립" 포함된 시설만

# 4. 특정 시설 상세 조회
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities/F001?serviceKey=test123" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, facilityId="F001", 상세 정보 포함 (위도/경도/홈페이지 등)

# 5. XML 형식 조회
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&pageNo=1&numOfRows=5" \
  -H "Accept: application/xml"

# 예상 응답: HTTP 200, XML 형식 응답
```

#### 시나리오 2: 인증 실패 테스트

```bash
# 잘못된 서비스키로 요청
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=invalid_key" \
  -H "Accept: application/json"

# 예상 응답: HTTP 401, code="030", msg="서비스키가 등록되지 않았습니다."
```

#### 시나리오 3: 파라미터 검증 테스트

```bash
# 잘못된 지역코드
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&regionCode=99" \
  -H "Accept: application/json"

# 예상 응답: HTTP 400, code="010", msg="유효하지 않은 지역코드입니다."

# 잘못된 페이지 번호 (0 또는 음수)
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&pageNo=0" \
  -H "Accept: application/json"

# 예상 응답: HTTP 400, code="010", msg="페이지 번호는 1 이상이어야 합니다."
```

#### 시나리오 4: 데이터 없음 테스트

```bash
# 존재하지 않는 시설 ID로 상세 조회
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities/F999?serviceKey=test123" \
  -H "Accept: application/json"

# 예상 응답: HTTP 404, code="003", msg="해당 시설을 찾을 수 없습니다."

# 검색 결과 없음
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&facilityName=존재하지않는시설" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, code="000", totalCount=0, items=[]
```

#### 시나리오 5: 페이징 테스트

```bash
# 1페이지 조회 (5건)
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&pageNo=1&numOfRows=5" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, pageNo=1, numOfRows=5, items 최대 5건

# 2페이지 조회 (5건)
curl -X GET "http://localhost:8080/pubc-test-api/api/cultureFacilities?serviceKey=test123&pageNo=2&numOfRows=5" \
  -H "Accept: application/json"

# 예상 응답: HTTP 200, pageNo=2, items는 6~10번째 데이터
```

### 5.2 SOAP 웹 서비스 테스트

#### SoapUI 테스트 프로젝트

1. **WSDL 로드**: `http://localhost:8080/pubc-test-api/services/CultureFacilityService?wsdl`
2. **테스트 케이스 생성** (조회 전용):
   - getCultureFacilityList: 목록 조회 (필터링)
   - getCultureFacility: 특정 시설 상세 조회

#### cURL을 사용한 SOAP 테스트

**시나리오 1: 문화시설 목록 조회**
```bash
# getCultureFacilityList 호출
curl -X POST "http://localhost:8080/pubc-test-api/services/CultureFacilityService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: getCultureFacilityList" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.soap.test.pubc.iros/">
    <soapenv:Header/>
    <soapenv:Body>
      <ser:getCultureFacilityList>
        <serviceKey>test123</serviceKey>
        <facilityType>박물관</facilityType>
        <regionCode>11</regionCode>
        <facilityName></facilityName>
        <pageNo>1</pageNo>
        <numOfRows>10</numOfRows>
      </ser:getCultureFacilityList>
    </soapenv:Body>
  </soapenv:Envelope>'

# 예상 응답: SOAP Envelope with code="000", facilities 배열
```

**시나리오 2: 문화시설 상세 조회**
```bash
# getCultureFacility 호출
curl -X POST "http://localhost:8080/pubc-test-api/services/CultureFacilityService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: getCultureFacility" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.soap.test.pubc.iros/">
    <soapenv:Header/>
    <soapenv:Body>
      <ser:getCultureFacility>
        <serviceKey>test123</serviceKey>
        <facilityId>F001</facilityId>
      </ser:getCultureFacility>
    </soapenv:Body>
  </soapenv:Envelope>'

# 예상 응답: SOAP Envelope with code="000", facility 상세 정보
```

**시나리오 3: 인증 실패 테스트**
```bash
# 잘못된 서비스키
curl -X POST "http://localhost:8080/pubc-test-api/services/CultureFacilityService" \
  -H "Content-Type: text/xml; charset=utf-8" \
  -H "SOAPAction: getCultureFacilityList" \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.soap.test.pubc.iros/">
    <soapenv:Header/>
    <soapenv:Body>
      <ser:getCultureFacilityList>
        <serviceKey>invalid_key</serviceKey>
        <facilityType></facilityType>
        <regionCode></regionCode>
        <facilityName></facilityName>
        <pageNo>1</pageNo>
        <numOfRows>10</numOfRows>
      </ser:getCultureFacilityList>
    </soapenv:Body>
  </soapenv:Envelope>'

# 예상 응답: SOAP Fault, errorCode="030"
```

### 5.3 성능 테스트

#### Apache JMeter 테스트 플랜

**테스트 1: 목록 조회 성능**
```
Thread Group:
  - Number of Threads: 50
  - Ramp-Up Period: 10초
  - Loop Count: 10

HTTP Request:
  - URL: /api/cultureFacilities?serviceKey=test123&pageNo=1&numOfRows=10
  - Method: GET

Assertions:
  - Response Code: 200
  - Response Time: < 1000ms
  - JSON Path: $.code equals "000"
```

**테스트 2: 필터링 조회 성능**
```
HTTP Request:
  - URL: /api/cultureFacilities?serviceKey=test123&facilityType=박물관&regionCode=11
  - Method: GET

Assertions:
  - Response Time: < 1000ms
```

**테스트 3: 상세 조회 성능**
```
HTTP Request:
  - URL: /api/cultureFacilities/F001?serviceKey=test123
  - Method: GET

Assertions:
  - Response Time: < 500ms (단일 레코드 조회)
```

#### 예상 결과
- **평균 응답 시간**: 200~500ms (목록), 100~300ms (상세)
- **처리량 (Throughput)**: 50 TPS 이상
- **에러율**: < 1%
- **동시 사용자**: 50명 안정 처리

### 5.4 로깅 확인

#### PUBC 사용 현황 조회

```sql
-- 최근 100건의 API 호출 내역 조회
SELECT
    SERVICE_KEY,
    SERVICE_ID,
    REQUEST_TIME,
    TROBL_TY_CODE,
    TROBL_CN,
    CLIENT_IP
FROM TN_PUBC_USE_STTUS_INFO
ORDER BY REQUEST_TIME DESC
LIMIT 100;

-- 서비스키별 호출 통계
SELECT
    SERVICE_KEY,
    COUNT(*) AS TOTAL_CALLS,
    SUM(CASE WHEN TROBL_TY_CODE = '00' THEN 1 ELSE 0 END) AS SUCCESS_CALLS,
    SUM(CASE WHEN TROBL_TY_CODE != '00' THEN 1 ELSE 0 END) AS FAIL_CALLS
FROM TN_PUBC_USE_STTUS_INFO
WHERE REQUEST_TIME >= CURRENT_DATE
GROUP BY SERVICE_KEY;
```

---

## 6. API 클라이언트 샘플 코드

### 6.1 Java REST 클라이언트

```java
import java.io.*;
import java.net.*;
import org.json.*;

public class RestApiClient {

    public static void main(String[] args) throws Exception {
        String serviceKey = "test123";
        String baseUrl = "http://localhost:8080/pubc-test-api/api";

        // 사용자 목록 조회
        String response = getUserList(baseUrl, serviceKey, null, 1, 10);
        System.out.println("Response: " + response);
    }

    public static String getUserList(String baseUrl, String serviceKey,
                                      String userName, int pageNo, int numOfRows)
        throws Exception {

        StringBuilder url = new StringBuilder(baseUrl + "/users?serviceKey=" + serviceKey);
        if (userName != null && !userName.isEmpty()) {
            url.append("&userName=").append(URLEncoder.encode(userName, "UTF-8"));
        }
        url.append("&pageNo=").append(pageNo);
        url.append("&numOfRows=").append(numOfRows);

        HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        return response.toString();
    }
}
```

### 6.2 Java SOAP 클라이언트 (JAX-WS)

```java
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.net.URL;

public class SoapClient {

    public static void main(String[] args) throws Exception {
        String wsdlUrl = "http://localhost:8080/pubc-test-api/services/UserService?wsdl";
        String namespace = "http://service.soap.test.pubc.iros/";
        String serviceName = "UserService";

        URL url = new URL(wsdlUrl);
        QName qname = new QName(namespace, serviceName);

        Service service = Service.create(url, qname);
        UserService userService = service.getPort(UserService.class);

        // getUserList 호출
        UserListResponse response = userService.getUserList("test123", null, 1, 10);

        System.out.println("Code: " + response.getCode());
        System.out.println("Message: " + response.getMsg());
        System.out.println("Total Count: " + response.getTotalCount());

        for (User user : response.getUsers()) {
            System.out.println("User: " + user.getUserId() + " - " + user.getUserName());
        }
    }
}
```

### 6.3 Python REST 클라이언트

```python
import requests
import json

BASE_URL = "http://localhost:8080/pubc-test-api/api"
SERVICE_KEY = "test123"

def get_user_list(user_name=None, page_no=1, num_of_rows=10):
    """사용자 목록 조회"""
    params = {
        "serviceKey": SERVICE_KEY,
        "pageNo": page_no,
        "numOfRows": num_of_rows
    }
    if user_name:
        params["userName"] = user_name

    response = requests.get(f"{BASE_URL}/users", params=params)
    return response.json()

def create_user(user_id, user_name, email):
    """사용자 등록"""
    params = {"serviceKey": SERVICE_KEY}
    data = {
        "userId": user_id,
        "userName": user_name,
        "email": email
    }

    response = requests.post(
        f"{BASE_URL}/users",
        params=params,
        json=data,
        headers={"Content-Type": "application/json"}
    )
    return response.json()

# 테스트 실행
if __name__ == "__main__":
    # 사용자 목록 조회
    result = get_user_list()
    print(json.dumps(result, indent=2, ensure_ascii=False))

    # 사용자 등록
    result = create_user("pyuser001", "파이썬사용자", "python@example.com")
    print(json.dumps(result, indent=2, ensure_ascii=False))
```

---

## 부록 A: Postman Collection

```json
{
  "info": {
    "name": "PUBC Test API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "사용자 목록 조회",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Accept",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "http://localhost:8080/pubc-test-api/api/users?serviceKey=test123&pageNo=1&numOfRows=10",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["pubc-test-api", "api", "users"],
          "query": [
            {"key": "serviceKey", "value": "test123"},
            {"key": "pageNo", "value": "1"},
            {"key": "numOfRows", "value": "10"}
          ]
        }
      }
    },
    {
      "name": "사용자 등록",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"userId\": \"testuser001\",\n  \"userName\": \"테스트사용자\",\n  \"email\": \"test@example.com\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/pubc-test-api/api/users?serviceKey=test123",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["pubc-test-api", "api", "users"],
          "query": [
            {"key": "serviceKey", "value": "test123"}
          ]
        }
      }
    }
  ]
}
```

---

**작성자**: Claude
**버전**: 1.0
**최종 수정일**: 2025-11-03
