package com.atmd.backend.global.external.kakao.client;

import com.atmd.backend.global.external.kakao.config.KakaoLocalConfig;
import com.atmd.backend.global.external.kakao.dto.response.KakaoLocalAddressResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "kakao-local",
        url = "${kakao.local.base-url}",
        configuration = KakaoLocalConfig.class
)
public interface KakaoLocalClient {

    @GetMapping("/v2/local/search/address.json")
    KakaoLocalAddressResponseDTO searchAddress(@RequestParam("query") String query);
}
