#!/bin/bash

###############################################################################
# PUBC Test API - 통합 테스트 스크립트
# 사용법: ./test-api.sh
###############################################################################

BASE_URL="http://localhost:8080/pubc-test-api/api"
SERVICE_KEY="TEST_KEY_001"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo "=========================================="
echo "  PUBC Test API - 통합 테스트"
echo "=========================================="
echo ""

# jq 설치 확인
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}경고: jq가 설치되지 않았습니다. JSON 포맷팅이 제한됩니다.${NC}"
    echo "설치: brew install jq"
    echo ""
    USE_JQ=false
else
    USE_JQ=true
fi

# 서버 상태 확인
echo -e "${BLUE}0. 서버 상태 확인...${NC}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/pubc-test-api/)
if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✓ 서버 정상 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "${RED}✗ 서버 접근 실패 (HTTP $HTTP_CODE)${NC}"
    echo "서버가 실행 중인지 확인하세요: ./bin/jboss-start.sh"
    exit 1
fi
echo ""

# 1. 기본 접근 테스트
echo -e "${BLUE}1. 기본 접근 테스트${NC}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/pubc-test-api/)
echo "   애플리케이션 루트: HTTP $HTTP_CODE"

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/pubc-test-api/api/)
echo "   API 엔드포인트: HTTP $HTTP_CODE"
echo ""

# 2. 문화시설 목록 조회 (전체)
echo -e "${BLUE}2. 문화시설 목록 조회 (전체, 페이지 크기: 5)${NC}"
RESPONSE=$(curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageSize=5")
if [ "$USE_JQ" = true ]; then
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalCount // "N/A"')
    FIRST_NAME=$(echo "$RESPONSE" | jq -r '.data[0].facilityName // "N/A"')
    CODE=$(echo "$RESPONSE" | jq -r '.resultCode // "N/A"')
    echo "   응답 코드: $CODE"
    echo "   총 시설 수: $TOTAL"
    echo "   첫 번째 시설: $FIRST_NAME"
else
    echo "$RESPONSE" | head -20
fi
echo ""

# 3. 시설 유형별 조회 (박물관)
echo -e "${BLUE}3. 시설 유형별 조회 (박물관, 페이지 크기: 3)${NC}"
RESPONSE=$(curl -s -G "${BASE_URL}/facilities" \
  --data-urlencode "serviceKey=${SERVICE_KEY}" \
  --data-urlencode "facilityType=박물관" \
  --data-urlencode "pageSize=3")
if [ "$USE_JQ" = true ]; then
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalCount // "N/A"')
    COUNT=$(echo "$RESPONSE" | jq -r '.data | length')
    echo "   박물관 총 수: $TOTAL"
    echo "   조회된 수: $COUNT"
    echo "   시설 목록:"
    echo "$RESPONSE" | jq -r '.data[].facilityName' | while read name; do
        echo "     - $name"
    done
else
    echo "$RESPONSE" | head -20
fi
echo ""

# 4. 지역별 조회 (서울)
echo -e "${BLUE}4. 지역별 조회 (서울, 지역코드: 11)${NC}"
RESPONSE=$(curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&regionCode=11&pageSize=3")
if [ "$USE_JQ" = true ]; then
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalCount // "N/A"')
    COUNT=$(echo "$RESPONSE" | jq -r '.data | length')
    echo "   서울 시설 총 수: $TOTAL"
    echo "   조회된 수: $COUNT"
    echo "   시설 목록:"
    echo "$RESPONSE" | jq -r '.data[] | "     - \(.facilityName) (\(.facilityType))"'
else
    echo "$RESPONSE" | head -20
fi
echo ""

# 5. 복합 조건 조회 (서울 + 박물관)
echo -e "${BLUE}5. 복합 조건 조회 (서울 + 박물관)${NC}"
RESPONSE=$(curl -s -G "${BASE_URL}/facilities" \
  --data-urlencode "serviceKey=${SERVICE_KEY}" \
  --data-urlencode "regionCode=11" \
  --data-urlencode "facilityType=박물관" \
  --data-urlencode "pageSize=3")
if [ "$USE_JQ" = true ]; then
    TOTAL=$(echo "$RESPONSE" | jq -r '.totalCount // "N/A"')
    echo "   서울 박물관 수: $TOTAL"
    echo "   시설 목록:"
    echo "$RESPONSE" | jq -r '.data[] | "     - \(.facilityName) (\(.address))"'
else
    echo "$RESPONSE" | head -20
fi
echo ""

# 6. 문화시설 상세 조회
echo -e "${BLUE}6. 문화시설 상세 조회 (F001)${NC}"
RESPONSE=$(curl -s "${BASE_URL}/facilities/F001?serviceKey=${SERVICE_KEY}")
if [ "$USE_JQ" = true ]; then
    NAME=$(echo "$RESPONSE" | jq -r '.data.facilityName // "N/A"')
    TYPE=$(echo "$RESPONSE" | jq -r '.data.facilityType // "N/A"')
    ADDRESS=$(echo "$RESPONSE" | jq -r '.data.address // "N/A"')
    PHONE=$(echo "$RESPONSE" | jq -r '.data.phoneNumber // "N/A"')
    HOMEPAGE=$(echo "$RESPONSE" | jq -r '.data.website // "N/A"')
    echo "   시설명: $NAME"
    echo "   유형: $TYPE"
    echo "   주소: $ADDRESS"
    echo "   전화: $PHONE"
    echo "   홈페이지: $HOMEPAGE"
else
    echo "$RESPONSE" | head -20
fi
echo ""

# 7. 시설 유형 목록 조회
echo -e "${BLUE}7. 시설 유형 목록 조회${NC}"
RESPONSE=$(curl -s "${BASE_URL}/facilities/types?serviceKey=${SERVICE_KEY}")
if [ "$USE_JQ" = true ]; then
    echo "   지원 시설 유형:"
    echo "$RESPONSE" | jq -r '.data[] | "     - \(.)"'
else
    echo "$RESPONSE"
fi
echo ""

# 8. 페이징 테스트
echo -e "${BLUE}8. 페이징 테스트${NC}"
echo "   페이지 1 (크기: 5):"
RESPONSE=$(curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageNum=1&pageSize=5")
if [ "$USE_JQ" = true ]; then
    echo "$RESPONSE" | jq -r '.data[] | "     \(.facilityId): \(.facilityName)"'
else
    echo "$RESPONSE" | head -10
fi

echo "   페이지 2 (크기: 5):"
RESPONSE=$(curl -s "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageNum=2&pageSize=5")
if [ "$USE_JQ" = true ]; then
    echo "$RESPONSE" | jq -r '.data[] | "     \(.facilityId): \(.facilityName)"'
else
    echo "$RESPONSE" | head -10
fi
echo ""

# 9. 에러 케이스 테스트
echo -e "${BLUE}9. 에러 케이스 테스트${NC}"

# 9.1 잘못된 서비스키
echo "   9.1 잘못된 서비스키:"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/facilities?serviceKey=INVALID_KEY")
if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "      ${GREEN}✓ 예상대로 거부됨 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "      ${RED}✗ 예상치 못한 응답 (HTTP $HTTP_CODE)${NC}"
fi

# 9.2 서비스키 누락
echo "   9.2 서비스키 누락:"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/facilities")
if [ "$HTTP_CODE" -eq 401 ] || [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "      ${GREEN}✓ 예상대로 거부됨 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "      ${RED}✗ 예상치 못한 응답 (HTTP $HTTP_CODE)${NC}"
fi

# 9.3 존재하지 않는 시설
echo "   9.3 존재하지 않는 시설 ID:"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/facilities/INVALID_ID?serviceKey=${SERVICE_KEY}")
if [ "$HTTP_CODE" -eq 404 ]; then
    echo -e "      ${GREEN}✓ 예상대로 404 반환 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "      ${YELLOW}⚠ 예상과 다른 응답 (HTTP $HTTP_CODE)${NC}"
fi

# 9.4 잘못된 파라미터
echo "   9.4 잘못된 페이지 번호:"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageNum=-1")
if [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "      ${GREEN}✓ 예상대로 400 반환 (HTTP $HTTP_CODE)${NC}"
else
    echo -e "      ${YELLOW}⚠ 예상과 다른 응답 (HTTP $HTTP_CODE)${NC}"
fi
echo ""

# 10. 다른 서비스키 테스트
echo -e "${BLUE}10. 다른 서비스키 테스트${NC}"
for KEY in "TEST_KEY_002" "DEMO_KEY" "DEV_KEY"; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/facilities?serviceKey=${KEY}&pageSize=1")
    if [ "$HTTP_CODE" -eq 200 ]; then
        echo -e "   ${GREEN}✓ $KEY: 정상 (HTTP $HTTP_CODE)${NC}"
    else
        echo -e "   ${RED}✗ $KEY: 실패 (HTTP $HTTP_CODE)${NC}"
    fi
done
echo ""

# 11. 성능 테스트 (간단)
echo -e "${BLUE}11. 성능 테스트 (10회 반복)${NC}"
TOTAL_TIME=0
for i in {1..10}; do
    START=$(date +%s%N)
    curl -s -o /dev/null "${BASE_URL}/facilities?serviceKey=${SERVICE_KEY}&pageSize=10"
    END=$(date +%s%N)
    ELAPSED=$((($END - $START) / 1000000))  # ms로 변환
    TOTAL_TIME=$(($TOTAL_TIME + $ELAPSED))
done
AVG_TIME=$(($TOTAL_TIME / 10))
echo "   평균 응답 시간: ${AVG_TIME}ms"
if [ $AVG_TIME -lt 100 ]; then
    echo -e "   ${GREEN}✓ 성능 우수${NC}"
elif [ $AVG_TIME -lt 500 ]; then
    echo -e "   ${GREEN}✓ 성능 양호${NC}"
elif [ $AVG_TIME -lt 1000 ]; then
    echo -e "   ${YELLOW}⚠ 성능 보통${NC}"
else
    echo -e "   ${RED}✗ 성능 개선 필요${NC}"
fi
echo ""

# 최종 결과
echo "=========================================="
echo -e "${GREEN}테스트 완료${NC}"
echo "=========================================="
echo ""
echo "추가 테스트:"
echo "  - SOAP 웹 서비스: docs/03_API명세서.md 참조"
echo "  - Postman Collection: scripts/pubc-test-api.postman_collection.json"
echo "  - 상세 로그: tail -f logs/server.log"
echo ""
