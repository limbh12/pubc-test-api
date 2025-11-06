package iros.test.facility.controller;

import iros.test.facility.domain.CultureFacilityVO;
import iros.test.facility.service.CultureFacilityService;
import iros.test.user.domain.UserCrtfcVO;
import iros.test.user.mock.MockCommonProc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 문화시설 REST API Service (JAX-RS)
 *
 * @author PUBC Test API
 * @version 1.0
 */
@Component("cultureFacilityRestService")
@Path("/facilities")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class CultureFacilityRestService {

    private static final Logger logger = LoggerFactory.getLogger(CultureFacilityRestService.class);

    @Autowired
    private CultureFacilityService cultureFacilityService;

    @Autowired
    private MockCommonProc mockCommonProc;

    /**
     * 문화시설 목록 조회
     *
     * GET /api/facilities?serviceKey={key}&facilityType={type}&pageNum={num}&pageSize={size}
     *
     * @param request HTTP 요청
     * @param facilityType 시설 유형 (선택)
     * @param facilityName 시설명 검색 (선택)
     * @param pageNum 페이지 번호 (기본: 1)
     * @param pageSize 페이지 크기 (기본: 10)
     * @return 문화시설 목록
     */
    @GET
    @Path("")
    public Response getFacilityList(
            @Context HttpServletRequest request,
            @QueryParam("facilityType") String facilityType,
            @QueryParam("facilityName") String facilityName,
            @QueryParam("pageNum") @DefaultValue("1") int pageNum,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        try {
            // PUBC 인증
            UserCrtfcVO userCrtfc = mockCommonProc.authServiceCall(request);

            // 파라미터 설정
            Map<String, Object> params = new HashMap<>();
            params.put("facilityType", facilityType);
            params.put("facilityName", facilityName);
            params.put("pageNum", pageNum);
            params.put("pageSize", pageSize);

            // 목록 조회
            List<CultureFacilityVO> facilityList = cultureFacilityService.getFacilityList(params);
            int totalCount = cultureFacilityService.getFacilityCount(params);

            // 응답 데이터
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("resultCode", "00");
            responseData.put("resultMessage", "정상 처리되었습니다");
            responseData.put("totalCount", totalCount);
            responseData.put("pageNum", pageNum);
            responseData.put("pageSize", pageSize);
            responseData.put("data", facilityList);

            // PUBC 로깅
            mockCommonProc.insertPubcLog(request, userCrtfc, responseData);

            return Response.ok(responseData).build();

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 요청: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "01");
            errorResponse.put("resultMessage", e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();

        } catch (Exception e) {
            logger.error("서버 오류: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "99");
            errorResponse.put("resultMessage", "서버 오류가 발생했습니다");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * 문화시설 상세 조회
     *
     * GET /api/facilities/{facilityId}?serviceKey={key}
     *
     * @param request HTTP 요청
     * @param facilityId 시설 ID
     * @return 문화시설 상세 정보
     */
    @GET
    @Path("/{facilityId}")
    public Response getFacilityById(
            @Context HttpServletRequest request,
            @PathParam("facilityId") String facilityId) {

        try {
            // PUBC 인증
            UserCrtfcVO userCrtfc = mockCommonProc.authServiceCall(request);

            // 상세 조회
            CultureFacilityVO facility = cultureFacilityService.getFacilityById(facilityId);

            // 응답 데이터
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("resultCode", "00");
            responseData.put("resultMessage", "정상 처리되었습니다");
            responseData.put("data", facility);

            // PUBC 로깅
            mockCommonProc.insertPubcLog(request, userCrtfc, responseData);

            return Response.ok(responseData).build();

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 요청: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "01");
            errorResponse.put("resultMessage", e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();

        } catch (Exception e) {
            logger.error("서버 오류: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "99");
            errorResponse.put("resultMessage", "서버 오류가 발생했습니다");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * 문화시설 유형 목록 조회
     *
     * GET /api/facilities/types?serviceKey={key}
     *
     * @param request HTTP 요청
     * @return 시설 유형 목록
     */
    @GET
    @Path("/types")
    public Response getFacilityTypes(@Context HttpServletRequest request) {

        try {
            // PUBC 인증
            UserCrtfcVO userCrtfc = mockCommonProc.authServiceCall(request);

            // 유형 목록 조회
            List<String> types = cultureFacilityService.getFacilityTypes();

            // 응답 데이터
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("resultCode", "00");
            responseData.put("resultMessage", "정상 처리되었습니다");
            responseData.put("data", types);

            // PUBC 로깅
            mockCommonProc.insertPubcLog(request, userCrtfc, responseData);

            return Response.ok(responseData).build();

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 요청: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "01");
            errorResponse.put("resultMessage", e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();

        } catch (Exception e) {
            logger.error("서버 오류: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", "99");
            errorResponse.put("resultMessage", "서버 오류가 발생했습니다");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }
}
