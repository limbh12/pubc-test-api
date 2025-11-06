package iros.test.user.mock;

import iros.test.user.domain.UserCrtfcVO;

import java.util.Calendar;
import java.util.Date;

/**
 * Mock 사용자 인증 정보 Builder
 *
 * @author PUBC Test API
 * @version 1.0
 */
public class MockUserCrtfcVO {

    /**
     * Mock 사용자 인증 정보 생성
     *
     * @param serviceKey 서비스 키
     * @param userName 사용자명
     * @return 사용자 인증 정보
     */
    public static UserCrtfcVO create(String serviceKey, String userName) {
        UserCrtfcVO user = new UserCrtfcVO();
        user.setServiceKey(serviceKey);
        user.setUserName(userName);
        user.setUserType("DEVELOPER");
        user.setOrganizationName("PUBC Test Organization");
        user.setEmail(userName.toLowerCase().replace(" ", ".") + "@test.com");
        user.setPhoneNumber("010-0000-0000");
        user.setStatus("ACTIVE");
        user.setDailyCallLimit(1000);
        user.setTodayCallCount(0);

        // 발급일: 현재
        user.setIssuedAt(new Date());

        // 만료일: 1년 후
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        user.setExpiredAt(calendar.getTime());

        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        return user;
    }

    /**
     * 만료된 Mock 사용자 인증 정보 생성 (테스트용)
     *
     * @param serviceKey 서비스 키
     * @param userName 사용자명
     * @return 만료된 사용자 인증 정보
     */
    public static UserCrtfcVO createExpired(String serviceKey, String userName) {
        UserCrtfcVO user = create(serviceKey, userName);
        user.setStatus("EXPIRED");

        // 만료일: 과거
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        user.setExpiredAt(calendar.getTime());

        return user;
    }

    /**
     * 정지된 Mock 사용자 인증 정보 생성 (테스트용)
     *
     * @param serviceKey 서비스 키
     * @param userName 사용자명
     * @return 정지된 사용자 인증 정보
     */
    public static UserCrtfcVO createSuspended(String serviceKey, String userName) {
        UserCrtfcVO user = create(serviceKey, userName);
        user.setStatus("SUSPENDED");
        return user;
    }

    /**
     * 호출 제한 초과 Mock 사용자 인증 정보 생성 (테스트용)
     *
     * @param serviceKey 서비스 키
     * @param userName 사용자명
     * @return 호출 제한 초과 사용자 인증 정보
     */
    public static UserCrtfcVO createLimitExceeded(String serviceKey, String userName) {
        UserCrtfcVO user = create(serviceKey, userName);
        user.setDailyCallLimit(100);
        user.setTodayCallCount(100);
        return user;
    }
}
