package com.inventorymanagement.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Inventory Management System.
 *
 * <p>This Spring Boot application provides comprehensive inventory management capabilities
 * including hierarchical category management, product management, and SKU tracking.
 *
 * <p>Key Features: - RESTful API for inventory management - Hierarchical category structure using
 * path enumeration - Multi-level caching with Caffeine - Structured JSON logging - Comprehensive error handling
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@SpringBootApplication(scanBasePackages = "com.inventorymanagement")
@EnableJpaRepositories(basePackages = "com.inventorymanagement.*.repository")
@EntityScan(basePackages = "com.inventorymanagement.*.model")
@EnableCaching
@EnableTransactionManagement
@EnableAsync
public class InventoryManagementApplication {

    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryManagementApplication.class, args);
    }
}
