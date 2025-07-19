package com.inventorymanagement.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check endpoints for monitoring application status.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/v1/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final BuildProperties buildProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Autowired
    public HealthController(@Autowired(required = false) BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * Simple health check endpoint that returns UP if the application is running.
     */
    @GetMapping
    @Operation(summary = "Health Check", description = "Check if the application is running")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Application is healthy"),
                    @ApiResponse(responseCode = "503", description = "Application is unhealthy")
            })
    public ResponseEntity<Map<String, Object>> health(HttpServletRequest request) {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", System.currentTimeMillis());
        healthStatus.put("application", applicationName);
        healthStatus.put("version", applicationVersion);

        return ResponseEntity.ok(healthStatus);
    }

    /**
     * Detailed health information including version and build details.
     */
    @GetMapping("/info")
    @Operation(summary = "Application Info", description = "Get detailed application information")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Application info")})
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();

        info.put("application", applicationName);
        info.put("version", applicationVersion);
        info.put("timestamp", System.currentTimeMillis());

        if (buildProperties != null) {
            info.put(
                    "build",
                    Map.of(
                            "artifact", buildProperties.getArtifact(),
                            "group", buildProperties.getGroup(),
                            "version", buildProperties.getVersion(),
                            "time", buildProperties.getTime().toString()));
        }

        return ResponseEntity.ok(info);
    }

    /**
     * Detailed health check with system information.
     */
    @GetMapping("/detailed")
    @Operation(
            summary = "Detailed Health Check",
            description = "Get detailed health information including system metrics")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Detailed health information")})
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // System information
        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("totalMemory", runtime.totalMemory());
        system.put("freeMemory", runtime.freeMemory());
        system.put("maxMemory", runtime.maxMemory());
        system.put("availableProcessors", runtime.availableProcessors());
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        health.put("system", system);

        // Database status (simplified - in production, check actual DB connection)
        Map<String, Object> database = new HashMap<>();
        database.put("status", "UP");
        database.put("type", "H2");
        health.put("database", database);

        return ResponseEntity.ok(health);
    }

    /**
     * Readiness probe for Kubernetes deployments.
     */
    @GetMapping("/ready")
    @Operation(
            summary = "Readiness Probe",
            description = "Check if the application is ready to serve requests")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Application is ready"),
                    @ApiResponse(responseCode = "503", description = "Application is not ready")
            })
    public ResponseEntity<Map<String, Object>> ready() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(readiness);
    }

    /**
     * Liveness probe for Kubernetes deployments.
     */
    @GetMapping("/live")
    @Operation(summary = "Liveness Probe", description = "Check if the application is alive")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Application is alive"),
                    @ApiResponse(responseCode = "503", description = "Application is not responding")
            })
    public ResponseEntity<Map<String, Object>> live() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(liveness);
    }
}
