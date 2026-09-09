package com.atmd.backend.domain.facility.controller;

import com.atmd.backend.domain.facility.dto.response.FacilityMarkerResponseDTO;
import com.atmd.backend.domain.facility.service.FacilityService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Facility", description = "운동시설 API")
@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(
            summary = "주변 운동시설 마커 목록 조회",
            description = "내 주소 기준 5km 이내 운동시설 목록을 거리순으로 반환합니다. 결과는 24시간 캐시됩니다. 주변에 시설이 없으면 빈 리스트를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (시설 없으면 빈 리스트)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "주소 정보 미등록"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @GetMapping("/markers")
    public ResponseEntity<ApiResponse<List<FacilityMarkerResponseDTO>>> getMarkers() {
        return ResponseEntity.ok(ApiResponse.success(facilityService.getMarkers(SecurityUtil.getCurrentUserId())));
    }
}
