package com.projbase.api.global.config;

import com.projbase.api.global.common.exception.CommonErrorCode;
import com.projbase.api.global.common.exception.GeneralException;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@Slf4j
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(3, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            log.error("[External API Error] {} → status={}", methodKey, response.status());
            if (response.status() >= 500) {
                return new GeneralException(CommonErrorCode.EXTERNAL_API_UNAVAILABLE);
            }
            return new GeneralException(CommonErrorCode.EXTERNAL_API_ERROR);
        };
    }
}
