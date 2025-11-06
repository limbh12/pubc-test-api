package iros.test.facility.dao;

import iros.test.facility.domain.CultureFacilityVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 문화시설 Mapper Interface
 * MyBatis Mapper 및 Mock 구현체에서 공통으로 사용
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Mapper
public interface CultureFacilityMapper {

    /**
     * 문화시설 목록 조회
     *
     * @param params 검색 파라미터 (facilityType, facilityName, pageNum, pageSize)
     * @return 문화시설 목록
     */
    List<CultureFacilityVO> selectFacilityList(Map<String, Object> params);

    /**
     * 문화시설 상세 조회
     *
     * @param facilityId 시설 ID
     * @return 문화시설 정보
     */
    CultureFacilityVO selectFacilityById(String facilityId);

    /**
     * 문화시설 총 개수 조회
     *
     * @param params 검색 파라미터 (facilityType, facilityName)
     * @return 총 개수
     */
    int selectFacilityCount(Map<String, Object> params);

    /**
     * 문화시설 유형 목록 조회
     *
     * @return 시설 유형 목록 (박물관, 미술관, 도서관, 공연장, 문화센터)
     */
    List<String> selectFacilityTypes();

    /**
     * 문화시설 등록
     *
     * @param facility 문화시설 정보
     * @return 등록된 행 수
     */
    int insertFacility(CultureFacilityVO facility);

    /**
     * 문화시설 수정
     *
     * @param facility 문화시설 정보
     * @return 수정된 행 수
     */
    int updateFacility(CultureFacilityVO facility);

    /**
     * 문화시설 삭제
     *
     * @param facilityId 시설 ID
     * @return 삭제된 행 수
     */
    int deleteFacility(String facilityId);
}
