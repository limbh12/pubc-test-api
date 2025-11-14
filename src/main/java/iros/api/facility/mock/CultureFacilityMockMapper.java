package iros.api.facility.mock;

import iros.api.facility.dao.CultureFacilityMapper;
import iros.api.facility.domain.CultureFacilityVO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 문화시설 Mock Mapper
 * 모든 프로파일에서 데이터베이스 없이 테스트할 수 있도록 Mock 데이터 제공
 * (표준형/개선형 모두 동일한 데이터 소스 사용)
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Repository("cultureFacilityMockMapper")
@Primary
public class CultureFacilityMockMapper implements CultureFacilityMapper {

    private static final List<CultureFacilityVO> MOCK_DATA = new ArrayList<>();

    static {
        // Mock 데이터 초기화 (8개 샘플 문화시설)
        MOCK_DATA.add(createFacility(
                "F001", "국립중앙박물관", "박물관",
                "서울특별시 용산구 서빙고로 137", "02-2077-9000",
                "월-금 10:00-18:00, 수-토 10:00-21:00", "매주 월요일, 1월 1일",
                "무료 (특별전 별도)", "https://www.museum.go.kr",
                37.5240, 126.9807, "대한민국 대표 국립박물관"
        ));

        MOCK_DATA.add(createFacility(
                "F002", "국립현대미술관 서울관", "미술관",
                "서울특별시 종로구 삼청로 30", "02-3701-9500",
                "월-목 10:00-18:00, 금-토 10:00-21:00", "매주 월요일, 1월 1일",
                "무료 (특별전 별도)", "https://www.mmca.go.kr",
                37.5865, 126.9819, "현대미술 전문 국립미술관"
        ));

        MOCK_DATA.add(createFacility(
                "F003", "국립중앙도서관", "도서관",
                "서울특별시 서초구 반포대로 201", "02-535-4142",
                "월-금 09:00-21:00, 토-일 09:00-18:00", "법정공휴일",
                "무료", "https://www.nl.go.kr",
                37.5029, 127.0365, "대한민국 대표 국립도서관"
        ));

        MOCK_DATA.add(createFacility(
                "F004", "세종문화회관", "공연장",
                "서울특별시 종로구 세종대로 175", "02-399-1000",
                "공연시간에 따라 상이", "없음",
                "공연별 상이", "https://www.sejongpac.or.kr",
                37.5720, 126.9765, "서울 대표 공연문화예술회관"
        ));

        MOCK_DATA.add(createFacility(
                "F005", "광화문 국민소통관", "문화센터",
                "서울특별시 종로구 세종대로 209", "02-720-2028",
                "월-금 09:00-18:00", "주말, 공휴일",
                "무료", "https://www.gwanghwamun1st.go.kr",
                37.5729, 126.9784, "정부 정책 전시 및 홍보 공간"
        ));

        MOCK_DATA.add(createFacility(
                "F006", "국립민속박물관", "박물관",
                "서울특별시 종로구 삼청로 37", "02-3704-3114",
                "월-금 10:00-18:00, 토-일 10:00-19:00", "매주 화요일, 1월 1일",
                "무료", "https://www.nfm.go.kr",
                37.5837, 126.9785, "한국의 전통문화와 생활사 전시"
        ));

        MOCK_DATA.add(createFacility(
                "F007", "예술의전당", "공연장",
                "서울특별시 서초구 남부순환로 2406", "02-580-1300",
                "공연시간에 따라 상이", "없음",
                "공연별 상이", "https://www.sac.or.kr",
                37.4783, 127.0124, "대한민국 최대 복합 문화예술공간"
        ));

        MOCK_DATA.add(createFacility(
                "F008", "서울시립미술관", "미술관",
                "서울특별시 중구 덕수궁길 61", "02-2124-8800",
                "월-금 10:00-20:00, 토-일 10:00-18:00", "매주 월요일, 1월 1일",
                "무료 (특별전 별도)", "https://sema.seoul.go.kr",
                37.5652, 126.9752, "서울시 공립 현대미술관"
        ));
    }

    /**
     * Mock 문화시설 데이터 생성 헬퍼 메서드
     */
    private static CultureFacilityVO createFacility(
            String facilityId, String facilityName, String facilityType,
            String address, String phoneNumber, String operatingHours,
            String closedDays, String admissionFee, String website,
            Double latitude, Double longitude, String description) {

        CultureFacilityVO facility = new CultureFacilityVO();
        facility.setFacilityId(facilityId);
        facility.setFacilityName(facilityName);
        facility.setFacilityType(facilityType);
        facility.setAddress(address);
        facility.setPhoneNumber(phoneNumber);
        facility.setOperatingHours(operatingHours);
        facility.setClosedDays(closedDays);
        facility.setAdmissionFee(admissionFee);
        facility.setWebsite(website);
        facility.setLatitude(latitude);
        facility.setLongitude(longitude);
        facility.setDescription(description);
        facility.setCreatedAt(new Date());
        facility.setUpdatedAt(new Date());

        return facility;
    }

    @Override
    public List<CultureFacilityVO> selectFacilityList(Map<String, Object> params) {
        String facilityType = (String) params.get("facilityType");
        String facilityName = (String) params.get("facilityName");
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);

        // 필터링
        return MOCK_DATA.stream()
                .filter(f -> facilityType == null || facilityType.isEmpty() ||
                        f.getFacilityType().equals(facilityType))
                .filter(f -> facilityName == null || facilityName.isEmpty() ||
                        f.getFacilityName().contains(facilityName))
                .skip((pageNum - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    @Override
    public CultureFacilityVO selectFacilityById(String facilityId) {
        return MOCK_DATA.stream()
                .filter(f -> f.getFacilityId().equals(facilityId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int selectFacilityCount(Map<String, Object> params) {
        String facilityType = (String) params.get("facilityType");
        String facilityName = (String) params.get("facilityName");

        return (int) MOCK_DATA.stream()
                .filter(f -> facilityType == null || facilityType.isEmpty() ||
                        f.getFacilityType().equals(facilityType))
                .filter(f -> facilityName == null || facilityName.isEmpty() ||
                        f.getFacilityName().contains(facilityName))
                .count();
    }

    @Override
    public List<String> selectFacilityTypes() {
        return MOCK_DATA.stream()
                .map(CultureFacilityVO::getFacilityType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public int insertFacility(CultureFacilityVO facility) {
        // Mock 환경에서는 INSERT 지원 안 함
        throw new UnsupportedOperationException("Mock mode does not support INSERT operation");
    }

    @Override
    public int updateFacility(CultureFacilityVO facility) {
        // Mock 환경에서는 UPDATE 지원 안 함
        throw new UnsupportedOperationException("Mock mode does not support UPDATE operation");
    }

    @Override
    public int deleteFacility(String facilityId) {
        // Mock 환경에서는 DELETE 지원 안 함
        throw new UnsupportedOperationException("Mock mode does not support DELETE operation");
    }
}
