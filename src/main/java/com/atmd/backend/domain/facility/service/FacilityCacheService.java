package com.atmd.backend.domain.facility.service;

import com.atmd.backend.domain.facility.dto.response.FacilityMarkerResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityCacheService {

    private static final String PREFIX = "FACILITY_MARKER:";
    private static final long TTL_HOURS = 24;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<List<FacilityMarkerResponseDTO>> get(Long userId) {
        String json = redisTemplate.opsForValue().get(key(userId));
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception e) {
            log.warn("시설 마커 캐시 역직렬화 실패: userId={}", userId);
            return Optional.empty();
        }
    }

    public void save(Long userId, List<FacilityMarkerResponseDTO> markers) {
        try {
            String json = objectMapper.writeValueAsString(markers);
            redisTemplate.opsForValue().set(key(userId), json, TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("시설 마커 캐시 저장 실패: userId={}", userId);
        }
    }

    public void delete(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return PREFIX + userId;
    }
}
