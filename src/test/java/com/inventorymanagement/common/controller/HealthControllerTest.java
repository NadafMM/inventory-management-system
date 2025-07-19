package com.inventorymanagement.common.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for the HealthController.
 *
 * <p>These tests verify that the health check endpoints return the expected responses and status
 * codes.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@SpringBootTest(classes = com.inventorymanagement.application.InventoryManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired private MockMvc mockMvc;

    /**
     * Test the basic health check endpoint.
     */
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc
                .perform(get("/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Test the detailed health check endpoint.
     */
    @Test
    void testDetailedHealthEndpoint() throws Exception {
        mockMvc
                .perform(get("/v1/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.system.totalMemory").exists())
                .andExpect(jsonPath("$.system.javaVersion").exists())
                .andExpect(jsonPath("$.database").exists())
                .andExpect(jsonPath("$.database.status").value("UP"));
    }

    /**
     * Test the readiness probe endpoint.
     */
    @Test
    void testReadyEndpoint() throws Exception {
        mockMvc
                .perform(get("/v1/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Test the liveness probe endpoint.
     */
    @Test
    void testLiveEndpoint() throws Exception {
        mockMvc
                .perform(get("/v1/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALIVE"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
