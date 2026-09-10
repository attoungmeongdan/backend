package com.atmd.backend.domain.facility.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("facility-batch")
@RequiredArgsConstructor
public class FacilityBatchExitListener implements ApplicationListener<ApplicationReadyEvent> {

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        SpringApplication.exit(applicationContext, () -> 0);
    }
}
