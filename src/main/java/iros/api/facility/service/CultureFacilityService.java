package iros.api.facility.service;

import iros.api.facility.domain.CultureFacilityVO;

import java.util.List;
import java.util.Map;

/**
 * 문화시설 Service Interface
 *
 * @author PUBC Test API
 * @version 1.0
 */
public interface CultureFacilityService {

    /**
     * 문화시설 목록 조회
     *
     * @param params 검색 파라미터
     * @return 문화시설 목록
     */
    List<CultureFacilityVO> getFacilityList(Map<String, Object> params);

    /**
     * 문화시설 상세 조회
     *
     * @param facilityId 시설 ID
     * @return 문화시설 정보
     */
    CultureFacilityVO getFacilityById(String facilityId);

    /**
     * 문화시설 총 개수 조회
     *
     * @param params 검색 파라미터
     * @return 총 개수
     */
    int getFacilityCount(Map<String, Object> params);

    /**
     * 문화시설 유형 목록 조회
     *
     * @return 시설 유형 목록
     */
    List<String> getFacilityTypes();
}
