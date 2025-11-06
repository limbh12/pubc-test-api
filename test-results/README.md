# 테스트 결과

이 디렉토리에는 PUBC Test API의 테스트 결과가 저장됩니다.

## 파일 목록

- `mock-mode-test-2025-11-06.md` - Mock 모드 개발 환경 테스트 결과
- (향후 추가될 테스트 결과들...)

## 테스트 환경별 분류

### Mock 모드 (개발 환경)
- 데이터베이스 없이 인메모리 Mock 데이터 사용
- MockCommonProc를 통한 PUBC 인증/로깅 시뮬레이션
- 빠른 개발 및 테스트를 위한 환경

### Production 모드 (운영 환경)
- 실제 PUBC 모듈 연동
- CUBRID 데이터베이스 사용
- 실제 서비스키 인증

## 테스트 실행 방법

```bash
# Mock 모드 서버 시작
./bin/jboss-start.sh

# 또는 포트 오프셋 지정
cd env/jboss/wildfly-26.1.3.Final
./bin/standalone.sh -Djboss.socket.binding.port-offset=100

# API 테스트
curl "http://localhost:8180/pubc-test-api/api/facilities?serviceKey=TEST_KEY_001"
```

## 유효한 테스트 서비스키

- `TEST_KEY_001` - 테스트 사용자 1
- `TEST_KEY_002` - 테스트 사용자 2
- `DEMO_KEY` - 데모 사용자
- `DEV_KEY` - 개발자 사용자
