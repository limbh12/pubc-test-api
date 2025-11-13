package iros.test.facility.service;

import iros.test.common.exception.ResourceNotFoundException;
import iros.test.facility.dao.CultureFacilityMapper;
import iros.test.facility.domain.CultureFacilityVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 문화시설 Service 구현체
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class CultureFacilityServiceImpl implements CultureFacilityService {

    private static final Logger logger = LoggerFactory.getLogger(CultureFacilityServiceImpl.class);

    @Autowired
    private CultureFacilityMapper cultureFacilityMapper;

    @Override
    public List<CultureFacilityVO> getFacilityList(Map<String, Object> params) {
        logger.debug("문화시설 목록 조회 - params: {}", params);

        // 기본값 설정
        params.putIfAbsent("pageNum", 1);
        params.putIfAbsent("pageSize", 10);

        List<CultureFacilityVO> facilityList = cultureFacilityMapper.selectFacilityList(params);

        logger.info("문화시설 목록 조회 완료 - count: {}", facilityList.size());

        return facilityList;
    }

    @Override
    public CultureFacilityVO getFacilityById(String facilityId) {
        logger.debug("문화시설 상세 조회 - facilityId: {}", facilityId);

        if (facilityId == null || facilityId.isEmpty()) {
            throw new IllegalArgumentException("시설 ID는 필수입니다");
        }

        CultureFacilityVO facility = cultureFacilityMapper.selectFacilityById(facilityId);

        if (facility == null) {
            logger.warn("문화시설을 찾을 수 없습니다 - facilityId: {}", facilityId);
            throw new ResourceNotFoundException("해당 시설을 찾을 수 없습니다: " + facilityId);
        }

        logger.info("문화시설 상세 조회 완료 - facilityId: {}, name: {}",
                facilityId, facility.getFacilityName());

        return facility;
    }

    @Override
    public int getFacilityCount(Map<String, Object> params) {
        logger.debug("문화시설 총 개수 조회 - params: {}", params);

        int count = cultureFacilityMapper.selectFacilityCount(params);

        logger.info("문화시설 총 개수 조회 완료 - count: {}", count);

        return count;
    }

    @Override
    public List<String> getFacilityTypes() {
        logger.debug("문화시설 유형 목록 조회");

        List<String> types = cultureFacilityMapper.selectFacilityTypes();

        logger.info("문화시설 유형 목록 조회 완료 - count: {}", types.size());

        return types;
    }
}
