package com.inventorymanagement.common.config;

import io.swagger.v3.oas.models.examples.Example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI examples.
 *
 * <p>This class provides comprehensive examples for API documentation including: - Request body
 * examples for different operations - Response body examples for success and error cases - Common parameter examples for the API
 *
 * <p>Examples are used to enhance the Swagger UI documentation and provide clear guidance for API
 * consumers.
 *
 * @version 1.0.0
 * @since 2025-01-15
 */
@Configuration
public class ApiExamplesConfig {

    /**
     * Category creation request example.
     */
    @Bean
    public Example categoryCreateExample() {
        return new Example()
                .summary("Create Category")
                .description("Example request to create a new category")
                .value(
                        """
                                {
                                  "name": "Electronics",
                                  "description": "Electronic devices and accessories",
                                  "parent_id": null,
                                  "sort_order": 1,
                                  "is_active": true,
                                  "metadata": "{\\"color\\": \\"blue\\", \\"icon\\": \\"electronics\\"}"
                                }
                                """);
    }

    /**
     * Validation error response example.
     */
    @Bean
    public Example validationErrorExample() {
        return new Example()
                .summary("Validation Error")
                .description("Example validation error response")
                .value(
                        """
                                {
                                  "status": "error",
                                  "message": "Validation failed for one or more fields",
                                  "errorCode": "VALIDATION_ERROR",
                                  "timestamp": "2025-01-15T15:00:00",
                                  "path": "/api/v1/categories",
                                  "method": "POST",
                                  "status_code": 400,
                                  "correlationId": "abc123-def456-ghi789",
                                  "traceId": "trace123456",
                                  "spanId": "span789012",
                                  "validationErrors": [
                                    {
                                      "field": "name",
                                      "rejectedValue": "",
                                      "message": "Category name is required"
                                    }
                                  ]
                                }
                                """);
    }
}
