package iros.api.pubc.util;

import iros.api.pubc.domain.UserCrtfcVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PUBC 분산개선형(pubc-1.0.jar) 인증 및 로깅 처리 클래스
 * 
 * <p>개선 사항:</p>
 * <ul>
 *   <li>캐시 기반 빠른 인증 처리</li>
 *   <li>비동기 로깅으로 성능 향상</li>
 *   <li>Circuit Breaker 패턴 적용</li>
 *   <li>최소 데이터만 로깅하여 부하 감소</li>
 * </ul>
 * 
 * <p>사용 예시:</p>
 * <pre>{@code
 * @Autowired
 * private PubcImprovedProcessor pubcProcessor;
 * 
 * public Response getList(@Context HttpServletRequest request) {
 *     UserCrtfcVO userCrtfc = pubcProcessor.authenticate(request);
 *     try {
 *         List<Data> result = service.getList();
 *         return Response.ok(result).build();
 *     } finally {
 *         pubcProcessor.logApiCall(request, userCrtfc, result);
 *     }
 * }
 * }</pre>
 * 
 * @author PUBC Test API
 * @version 2.0 (Improved - pubc-1.0.jar)
 * @see iros.api.pubc.mock.MockCommonProc
 */
@Component
public class PubcImprovedProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PubcImprovedProcessor.class);

    // ================================================================
    // 설정값 (application.properties에서 주입)
    // ================================================================
    
    @Value("${pubc.improved.cache.ttl.minutes:5}")
    private int cacheTtlMinutes;
    
    @Value("${pubc.improved.cache.max.size:10000}")
    private int maxCacheSize;
    
    @Value("${pubc.improved.circuit.failure.threshold:10}")
    private int failureThreshold;
    
    @Value("${pubc.improved.circuit.timeout.seconds:60}")
    private int circuitTimeoutSeconds;
    
    @Value("${pubc.improved.logging.async:true}")
    private boolean asyncLoggingEnabled;
    
    @Value("${pubc.improved.logging.batch.size:100}")
    private int loggingBatchSize;
    
    @Value("${pubc.improved.logging.flush.interval.seconds:10}")
    private int flushIntervalSeconds;

    // ================================================================
    // 캐시 설정
    // ================================================================
    
    /**
     * 인증 캐시 TTL (Time To Live) - 설정값에서 주입
     */
    private long cacheTtlMillis;
    
    /**
     * 인증 캐시 (ServiceKey -> CacheEntry)
     * Thread-safe한 ConcurrentHashMap 사용
     */
    private final Map<String, CacheEntry> authCache = new ConcurrentHashMap<>();
    
    /**
     * 유효한 서비스 키 목록 (Mock 데이터)
     * 실제 환경에서는 DB 또는 외부 인증 서비스 사용
     */
    private static final Map<String, String> VALID_SERVICE_KEYS = new HashMap<>();

    static {
        // 기존 표준형 키
        VALID_SERVICE_KEYS.put("TEST_KEY_001", "테스트 사용자 1");
        VALID_SERVICE_KEYS.put("TEST_KEY_002", "테스트 사용자 2");
        VALID_SERVICE_KEYS.put("DEMO_KEY", "데모 사용자");
        VALID_SERVICE_KEYS.put("DEV_KEY", "개발자 사용자");
        
        // 개선형 전용 키
        VALID_SERVICE_KEYS.put("IMPROVED_KEY_001", "개선형 테스트 사용자 1");
        VALID_SERVICE_KEYS.put("IMPROVED_KEY_002", "개선형 테스트 사용자 2");
        VALID_SERVICE_KEYS.put("IMPROVED_DEV_KEY", "개선형 개발자");
    }
    
    /**
     * 초기화: 설정값을 기반으로 내부 변수 설정
     */
    @PostConstruct
    public void init() {
        this.cacheTtlMillis = cacheTtlMinutes * 60 * 1000L;
        
        logger.info("===== PUBC Improved Processor 초기화 =====");
        logger.info("캐시 TTL: {}분 ({}ms)", cacheTtlMinutes, cacheTtlMillis);
        logger.info("캐시 최대 크기: {}", maxCacheSize);
        logger.info("Circuit Breaker 실패 임계값: {}", failureThreshold);
        logger.info("Circuit Breaker 타임아웃: {}초", circuitTimeoutSeconds);
        logger.info("비동기 로깅: {}", asyncLoggingEnabled);
        logger.info("로깅 배치 크기: {}", loggingBatchSize);
        logger.info("플러시 간격: {}초", flushIntervalSeconds);
        logger.info("=========================================");
    }

    // ================================================================
    // Circuit Breaker 설정
    // ================================================================
    
    /**
     * Circuit Breaker 상태
     */
    private volatile CircuitState circuitState = CircuitState.CLOSED;
    
    /**
     * 연속 실패 횟수
     */
    private volatile int failureCount = 0;
    
    /**
     * 마지막 실패 시각
     */
    private volatile long lastFailureTime = 0;

    // ================================================================
    // Public API - 인증 처리
    // ================================================================
    
    /**
     * PUBC 분산개선형 인증 처리
     * 
     * <p>처리 순서:</p>
     * <ol>
     *   <li>캐시에서 인증 정보 조회 (Cache Hit 시 즉시 반환)</li>
     *   <li>Cache Miss 시 실제 인증 처리 수행</li>
     *   <li>Circuit Breaker 상태 확인 및 처리</li>
     *   <li>인증 성공 시 캐시에 저장</li>
     * </ol>
     * 
     * @param request HTTP 요청 객체
     * @return 사용자 인증 정보
     * @throws IllegalArgumentException 서비스 키가 없거나 유효하지 않은 경우
     * @throws RuntimeException Circuit Breaker OPEN 상태이거나 인증 실패 시
     */
    public UserCrtfcVO authenticate(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 서비스 키 추출
            String serviceKey = extractServiceKey(request);
            String clientIp = request.getRemoteAddr();
            
            logger.debug("[PUBC-Improved] 인증 요청 - ServiceKey: {}, IP: {}", serviceKey, clientIp);
            
            // 2. 캐시 조회
            CacheEntry cached = authCache.get(serviceKey);
            if (cached != null && !cached.isExpired()) {
                long elapsed = System.currentTimeMillis() - startTime;
                logger.info("[PUBC-Improved] 캐시 HIT - ServiceKey: {}, Time: {}ms", serviceKey, elapsed);
                return cached.userCrtfc;
            }
            
            // 3. Circuit Breaker 체크
            if (circuitState == CircuitState.OPEN) {
                long timeSinceFailure = System.currentTimeMillis() - lastFailureTime;
                long timeoutMillis = circuitTimeoutSeconds * 1000L;
                
                if (timeSinceFailure < timeoutMillis) {
                    throw new RuntimeException("Circuit Breaker OPEN - 인증 서비스 일시 중단");
                }
                // Half-Open 상태로 전환
                circuitState = CircuitState.HALF_OPEN;
                logger.info("[Circuit Breaker] HALF_OPEN 상태로 전환");
            }
            
            // 4. 실제 인증 처리
            UserCrtfcVO userCrtfc = performAuthentication(serviceKey, clientIp);
            
            // 5. Circuit Breaker 성공 처리
            if (circuitState == CircuitState.HALF_OPEN) {
                circuitState = CircuitState.CLOSED;
                logger.info("[Circuit Breaker] CLOSED 상태로 전환");
            }
            failureCount = 0;
            
            // 6. 캐시에 저장
            putToCache(serviceKey, userCrtfc);
            
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("[PUBC-Improved] 인증 성공 (DB) - ServiceKey: {}, Time: {}ms", serviceKey, elapsed);
            
            return userCrtfc;
            
        } catch (Exception e) {
            // Circuit Breaker 실패 처리
            handleAuthFailure(e);
            throw e;
        }
    }

    /**
     * API 호출 로깅 (비동기)
     * 
     * <p>최소 정보만 기록하여 성능 영향 최소화:</p>
     * <ul>
     *   <li>서비스 키</li>
     *   <li>요청 URI</li>
     *   <li>HTTP 메서드</li>
     *   <li>클라이언트 IP</li>
     *   <li>응답 코드 (성공/실패만)</li>
     * </ul>
     * 
     * @param request HTTP 요청 객체
     * @param userCrtfc 사용자 인증 정보
     * @param response API 응답 객체
     */
    public void logApiCall(HttpServletRequest request, UserCrtfcVO userCrtfc, Object response) {
        try {
            // 비동기 로깅 (실제로는 별도 스레드 풀 사용)
            String serviceKey = userCrtfc.getServiceKey();
            String requestUri = request.getRequestURI();
            String method = request.getMethod();
            String clientIp = request.getRemoteAddr();
            String responseCode = response != null ? "200" : "500";
            
            logger.info("[PUBC-Improved] API 로깅 - Key: {}, URI: {}, Method: {}, IP: {}, Code: {}",
                serviceKey, requestUri, method, clientIp, responseCode);
            
            // 실제 환경에서는 배치 처리 큐에 추가
            // loggingQueue.offer(new LogEntry(serviceKey, requestUri, method, clientIp, responseCode));
            
        } catch (Exception e) {
            // 로깅 실패는 비즈니스 로직에 영향을 주지 않음
            logger.error("[PUBC-Improved] 로깅 실패 (무시)", e);
        }
    }

    // ================================================================
    // Private Methods - 인증 처리
    // ================================================================
    
    /**
     * 서비스 키 추출
     */
    private String extractServiceKey(HttpServletRequest request) {
        // 1. Query Parameter에서 추출
        String serviceKey = request.getParameter("serviceKey");
        
        // 2. Header에서 추출
        if (serviceKey == null || serviceKey.isEmpty()) {
            serviceKey = request.getHeader("X-Service-Key");
        }
        
        if (serviceKey == null || serviceKey.isEmpty()) {
            throw new IllegalArgumentException("서비스 키가 필요합니다 (Query Parameter 또는 Header)");
        }
        
        return serviceKey;
    }
    
    /**
     * 실제 인증 수행 (Mock 구현)
     * 실제 환경에서는 pubc-1.0.jar의 API 호출
     */
    private UserCrtfcVO performAuthentication(String serviceKey, String clientIp) {
        // Mock: 유효한 서비스 키 확인
        String userName = VALID_SERVICE_KEYS.get(serviceKey);
        
        if (userName == null) {
            throw new IllegalArgumentException("유효하지 않은 서비스 키입니다: " + serviceKey);
        }
        
        // Mock: DB 조회 시뮬레이션 (50ms 지연)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 인증 정보 생성
        UserCrtfcVO userCrtfc = new UserCrtfcVO();
        userCrtfc.setServiceKey(serviceKey);
        userCrtfc.setUserName(userName);
        userCrtfc.setStatus("ACTIVE");
        
        return userCrtfc;
    }
    
    /**
     * 인증 실패 처리
     */
    private void handleAuthFailure(Exception e) {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold && circuitState == CircuitState.CLOSED) {
            circuitState = CircuitState.OPEN;
            logger.error("[Circuit Breaker] OPEN 상태로 전환 - 실패 횟수: {}", failureCount);
        }
        
        logger.error("[PUBC-Improved] 인증 실패", e);
    }

    // ================================================================
    // Private Methods - 캐시 관리
    // ================================================================
    
    /**
     * 캐시에 인증 정보 저장
     */
    private void putToCache(String serviceKey, UserCrtfcVO userCrtfc) {
        // 캐시 크기 제한 체크
        if (authCache.size() >= maxCacheSize) {
            // 간단한 LRU: 오래된 항목 일부 제거
            int removeCount = maxCacheSize / 10;
            authCache.keySet().stream()
                .limit(removeCount)
                .forEach(authCache::remove);
            logger.debug("[Cache] 캐시 정리 - 제거: {}개", removeCount);
        }
        
        CacheEntry entry = new CacheEntry(userCrtfc, System.currentTimeMillis() + cacheTtlMillis);
        authCache.put(serviceKey, entry);
        
        logger.trace("[Cache] 캐시 저장 - Key: {}, TTL: {}초", serviceKey, cacheTtlMillis / 1000);
    }
    
    /**
     * 캐시 통계 조회 (모니터링용)
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", authCache.size());
        stats.put("maxCacheSize", maxCacheSize);
        stats.put("cacheTtlSeconds", cacheTtlMillis / 1000);
        stats.put("circuitState", circuitState.name());
        stats.put("failureCount", failureCount);
        return stats;
    }

    // ================================================================
    // Inner Classes
    // ================================================================
    
    /**
     * 캐시 엔트리
     */
    private static class CacheEntry {
        final UserCrtfcVO userCrtfc;
        final long expireTime;
        
        CacheEntry(UserCrtfcVO userCrtfc, long expireTime) {
            this.userCrtfc = userCrtfc;
            this.expireTime = expireTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    /**
     * Circuit Breaker 상태
     */
    private enum CircuitState {
        CLOSED,    // 정상
        OPEN,      // 차단
        HALF_OPEN  // 반개방 (복구 테스트)
    }
}
