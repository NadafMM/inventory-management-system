package com.inventorymanagement.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration providing test-specific beans.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a fixed clock for consistent time-based tests.
     */
    @Bean
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneId.of("UTC"));
    }

    /**
     * Provides a configured ObjectMapper for tests.
     */
    @Bean
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper();
    }
}
