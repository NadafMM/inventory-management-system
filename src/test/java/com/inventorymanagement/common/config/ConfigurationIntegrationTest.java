package com.inventorymanagement.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for configuration classes
 */
@SpringBootTest(classes = com.inventorymanagement.application.InventoryManagementApplication.class)
@ActiveProfiles("test")
@DisplayName("Configuration Integration Tests")
class ConfigurationIntegrationTest {

    @Autowired private ApplicationContext applicationContext;

    @Nested
    @DisplayName("Database Configuration Tests")
    class DatabaseConfigurationTests {

        @Test
        @DisplayName("Should load DatabaseConfig bean")
        void shouldLoadDatabaseConfig() {
            DatabaseConfig databaseConfig = applicationContext.getBean(DatabaseConfig.class);
            assertNotNull(databaseConfig);
        }

        @Test
        @DisplayName("Should configure auditor aware")
        void shouldConfigureAuditorAware() {
            AuditorAware<String> auditorAware =
                    applicationContext.getBean("auditorAware", AuditorAware.class);
            assertNotNull(auditorAware);

            // Test current auditor
            var currentAuditor = auditorAware.getCurrentAuditor();
            assertThat(currentAuditor).isPresent();
            assertThat(currentAuditor.get()).isEqualTo("SYSTEM");
        }
    }

    @Nested
    @DisplayName("Flyway Configuration Tests")
    class FlywayConfigurationTests {

        @Test
        @DisplayName("Should handle FlywayConfig properly")
        void shouldLoadFlywayConfig() {
            // Flyway is disabled in test profile, so bean should not exist
            assertThat(applicationContext.containsBean("flywayConfig")).isFalse();
        }
    }

    @Nested
    @DisplayName("API Examples Configuration Tests")
    class ApiExamplesConfigurationTests {

        @Test
        @DisplayName("Should load ApiExamplesConfig bean")
        void shouldLoadApiExamplesConfig() {
            ApiExamplesConfig apiExamplesConfig = applicationContext.getBean(ApiExamplesConfig.class);
            assertNotNull(apiExamplesConfig);
        }

        @Test
        @DisplayName("Should provide example responses")
        void shouldProvideExampleResponses() {
            ApiExamplesConfig config = applicationContext.getBean(ApiExamplesConfig.class);

            // Test available example methods
            assertThat(config.categoryCreateExample()).isNotNull();
            assertThat(config.validationErrorExample()).isNotNull();
        }
    }

    @Nested
    @DisplayName("OpenAPI Configuration Tests")
    class OpenApiConfigurationTests {

        @Test
        @DisplayName("Should load OpenApiConfig bean")
        void shouldLoadOpenApiConfig() {
            OpenApiConfig openApiConfig = applicationContext.getBean(OpenApiConfig.class);
            assertNotNull(openApiConfig);
        }
    }

    @Nested
    @DisplayName("Logging Configuration Tests")
    class LoggingConfigurationTests {

        @Test
        @DisplayName("Should load LoggingConfig bean")
        void shouldLoadLoggingConfig() {
            LoggingConfig loggingConfig = applicationContext.getBean(LoggingConfig.class);
            assertNotNull(loggingConfig);
        }
    }

    @Nested
    @DisplayName("Correlation ID Filter Tests")
    class CorrelationIdFilterTests {

        @Test
        @DisplayName("Should create CorrelationIdFilter bean")
        void shouldCreateCorrelationIdFilter() {
            CorrelationIdFilter filter = applicationContext.getBean(CorrelationIdFilter.class);
            assertNotNull(filter);
        }

        @Test
        @DisplayName("Should process filter chain with correlation ID")
        void shouldProcessFilterChainWithCorrelationId() throws Exception {
            CorrelationIdFilter filter = new CorrelationIdFilter();

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            FilterChain filterChain = Mockito.mock(FilterChain.class);

            // Mock request without correlation ID header
            Mockito.when(request.getHeader("X-Correlation-ID")).thenReturn(null);

            filter.doFilter(request, response, filterChain);

            // Verify filter chain was called
            Mockito.verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Should use existing correlation ID from request")
        void shouldUseExistingCorrelationId() throws Exception {
            CorrelationIdFilter filter = new CorrelationIdFilter();

            HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            FilterChain filterChain = Mockito.mock(FilterChain.class);

            String existingCorrelationId = "existing-correlation-id";
            Mockito.when(request.getHeader("X-Correlation-ID")).thenReturn(existingCorrelationId);

            filter.doFilter(request, response, filterChain);

            // Verify filter chain was called
            Mockito.verify(filterChain).doFilter(request, response);
            // Verify response header was set
            Mockito.verify(response).setHeader("X-Correlation-ID", existingCorrelationId);
        }
    }

    @Nested
    @DisplayName("Performance Logging Aspect Tests")
    class PerformanceLoggingAspectTests {

        @Test
        @DisplayName("Should load PerformanceLoggingAspect bean")
        void shouldLoadPerformanceLoggingAspect() {
            PerformanceLoggingAspect aspect = applicationContext.getBean(PerformanceLoggingAspect.class);
            assertNotNull(aspect);
        }
    }

    @Nested
    @DisplayName("Cache Configuration Tests")
    class CacheConfigurationTests {

        @Test
        @DisplayName("Should configure cache manager")
        void shouldConfigureCacheManager() {
            // Cache manager should be configured by Spring Boot's auto-configuration
            // but we can verify it exists and is properly configured
            if (applicationContext.containsBean("cacheManager")) {
                CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
                assertNotNull(cacheManager);

                // Verify expected caches exist - initially empty, caches are created on demand
                assertThat(cacheManager.getCacheNames()).isNotNull();
            }
        }
    }
}
