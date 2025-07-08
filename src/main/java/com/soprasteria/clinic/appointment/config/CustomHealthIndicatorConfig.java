package com.soprasteria.clinic.appointment.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class CustomHealthIndicatorConfig {

    @Bean
    public HealthIndicator diskSpaceHealthIndicator() {
        File path = new File(".");
        long threshold = 10L * 1024 * 1024;
        return () -> {
            long free = path.getFreeSpace();
            long total = path.getTotalSpace();
            return Health.up()
                    .withDetail("total (MB)", total / (1024 * 1024))
                    .withDetail("free (MB)", free / (1024 * 1024))
                    .withDetail("threshold (MB)", threshold / (1024 * 1024))
                    .withDetail("path", path.getAbsolutePath())
                    .withDetail("exists", path.exists())
                    .build();
        };
    }
}