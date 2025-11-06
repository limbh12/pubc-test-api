package iros.test.user.mock;

import iros.test.user.domain.UserCrtfcVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock PUBC 인증 및 로깅 처리
 * Mock 프로파일에서 PUBC 모듈 없이 테스트할 수 있도록 Mock 처리 제공
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Component
@Profile("mock")
public class MockCommonProc {

    private static final Logger logger = LoggerFactory.getLogger(MockCommonProc.class);

    /**
     * 유효한 서비스 키 목록 (Mock 데이터)
     */
    private static final Map<String, String> VALID_SERVICE_KEYS = new HashMap<>();

    static {
        VALID_SERVICE_KEYS.put("TEST_KEY_001", "테스트 사용자 1");
        VALID_SERVICE_KEYS.put("TEST_KEY_002", "테스트 사용자 2");
        VALID_SERVICE_KEYS.put("DEMO_KEY", "데모 사용자");
        VALID_SERVICE_KEYS.put("DEV_KEY", "개발자 사용자");
    }

    /**
     * Mock 인증 처리 (PUBC authServiceCall 대체)
     *
     * @param request HTTP 요청
     * @return 사용자 인증 정보
     */
    public UserCrtfcVO authServiceCall(HttpServletRequest request) {
        String serviceKey = request.getParameter("serviceKey");

        logger.debug("[Mock PUBC Auth] Service Key: {}", serviceKey);

        // 서비스 키 검증
        if (serviceKey == null || serviceKey.isEmpty()) {
            logger.warn("[Mock PUBC Auth] Service Key is missing");
            throw new IllegalArgumentException("서비스 키가 필요합니다");
        }

        // 유효한 서비스 키인지 확인
        String userName = VALID_SERVICE_KEYS.get(serviceKey);
        if (userName == null) {
            logger.warn("[Mock PUBC Auth] Invalid Service Key: {}", serviceKey);
            throw new IllegalArgumentException("유효하지 않은 서비스 키입니다");
        }

        // Mock 사용자 인증 정보 생성
        UserCrtfcVO userCrtfc = MockUserCrtfcVO.create(serviceKey, userName);

        logger.info("[Mock PUBC Auth] 인증 성공 - Service Key: {}, User: {}",
                serviceKey, userName);

        return userCrtfc;
    }

    /**
     * Mock 로깅 처리 (PUBC insertPubcLog 대체)
     *
     * @param request HTTP 요청
     * @param userCrtfc 사용자 인증 정보
     * @param response API 응답 데이터
     */
    public void insertPubcLog(HttpServletRequest request, UserCrtfcVO userCrtfc, Object response) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        logger.info("[Mock PUBC Log] API 호출 기록");
        logger.info("  - Request URI: {}", requestUri);
        logger.info("  - Query String: {}", queryString);
        logger.info("  - Method: {}", method);
        logger.info("  - Remote Address: {}", remoteAddr);
        logger.info("  - Service Key: {}", userCrtfc.getServiceKey());
        logger.info("  - User Name: {}", userCrtfc.getUserName());
        logger.info("  - Response: {}", response != null ? "Success" : "Null");
    }

    /**
     * Mock 로깅 처리 (오류 발생 시)
     *
     * @param request HTTP 요청
     * @param userCrtfc 사용자 인증 정보
     * @param exception 예외
     */
    public void insertPubcErrorLog(HttpServletRequest request, UserCrtfcVO userCrtfc, Exception exception) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String method = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        logger.error("[Mock PUBC Error Log] API 호출 오류");
        logger.error("  - Request URI: {}", requestUri);
        logger.error("  - Query String: {}", queryString);
        logger.error("  - Method: {}", method);
        logger.error("  - Remote Address: {}", remoteAddr);

        if (userCrtfc != null) {
            logger.error("  - Service Key: {}", userCrtfc.getServiceKey());
            logger.error("  - User Name: {}", userCrtfc.getUserName());
        } else {
            logger.error("  - Service Key: N/A");
            logger.error("  - User Name: N/A");
        }

        logger.error("  - Exception: {}", exception.getMessage(), exception);
    }

    /**
     * 유효한 서비스 키 목록 조회
     *
     * @return 서비스 키 Map
     */
    public Map<String, String> getValidServiceKeys() {
        return new HashMap<>(VALID_SERVICE_KEYS);
    }

    /**
     * 서비스 키 유효성 검증
     *
     * @param serviceKey 서비스 키
     * @return 유효 여부
     */
    public boolean isValidServiceKey(String serviceKey) {
        return VALID_SERVICE_KEYS.containsKey(serviceKey);
    }
}
