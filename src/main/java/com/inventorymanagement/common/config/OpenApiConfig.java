package com.inventorymanagement.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for the Inventory Management System.
 *
 * <p>This configuration sets up comprehensive API documentation using OpenAPI 3.0 specification
 * with Swagger UI integration. It includes: - API information and metadata - Server configurations for different environments - Contact and license
 * information
 *
 * <p>The configuration follows industry best practices for API documentation and provides a
 * professional, interactive interface for API exploration and testing.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.application.version}")
    private String applicationVersion;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Configures the OpenAPI specification for the Inventory Management System.
     *
     * @return OpenAPI configuration with complete API documentation setup
     */
    @Bean
    public OpenAPI inventoryManagementOpenAPI() {
        return new OpenAPI().info(createApiInfo()).servers(createServers());
    }

    /**
     * Creates API information including title, description, version, and contact details.
     */
    private Info createApiInfo() {
        return new Info()
                .title("Inventory Management System API")
                .description(
                        """
                                # Inventory Management System REST API
                                
                                A comprehensive REST API for managing inventory, products, categories, and SKUs with advanced features including:
                                
                                ## Key Features
                                - **Category Management**: Hierarchical category structure with unlimited nesting
                                - **Product Management**: Complete product lifecycle management
                                - **SKU Management**: Stock Keeping Unit tracking with inventory operations
                                - **Inventory Operations**: Real-time inventory tracking and updates
                                - **Caching**: Advanced caching strategies for optimal performance
                                - **Monitoring**: Comprehensive metrics and observability
                                - **Bulk Operations**: Efficient bulk create, update, and delete operations
                                
                                ## Error Handling
                                All endpoints return standardized error responses with:
                                - HTTP status codes following REST conventions
                                - Detailed error messages and codes
                                - Correlation IDs for request tracking
                                - Validation error details for bad requests
                                
                                ## Pagination
                                List endpoints support pagination with query parameters:
                                - `page`: Page number (0-based, default: 0)
                                - `size`: Page size (1-100, default: 20)
                                - `sort`: Sort field and direction (e.g., `name,asc`)
                                
                                ## Filtering and Search
                                Most list endpoints support filtering and search:
                                - Field-specific filters as query parameters
                                - Full-text search capabilities
                                - Date range filtering
                                - Status-based filtering
                                """)
                .version(applicationVersion)
                .contact(createContact())
                .license(createLicense());
    }

    /**
     * Creates contact information for the API.
     */
    private Contact createContact() {
        return new Contact()
                .name("Inventory Management Team")
                .email("support@inventorymanagement.com")
                .url("https://inventorymanagement.com/support");
    }

    /**
     * Creates license information for the API.
     */
    private License createLicense() {
        return new License().name("MIT License").url("https://opensource.org/licenses/MIT");
    }

    /**
     * Creates server configurations for different environments.
     */
    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort + contextPath)
                        .description("Development server"),
                new Server()
                        .url("https://api.inventorymanagement.com/api")
                        .description("Production server"),
                new Server()
                        .url("https://staging-api.inventorymanagement.com/api")
                        .description("Staging server"));
    }
}
