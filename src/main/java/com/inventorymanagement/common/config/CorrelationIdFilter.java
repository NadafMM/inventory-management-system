package com.inventorymanagement.common.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter that adds a correlation ID to each request for better log traceability.
 *
 * <p>This filter generates a unique correlation ID for each incoming request and adds it to: -
 * Mapped Diagnostic Context (MDC) for logging - Response headers for client-side correlation - Request attributes for access by other components
 *
 * <p>The correlation ID helps in: - Tracing requests across logs - Debugging distributed systems -
 * Performance monitoring - Error tracking and analysis
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Gets the correlation ID from the current request context.
     *
     * @return correlation ID from MDC, or null if not available
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String correlationId = getOrGenerateCorrelationId(httpRequest);

            // Add to MDC for logging
            MDC.put(CORRELATION_ID_KEY, correlationId);

            // Add to request attributes for access by other components
            httpRequest.setAttribute(CORRELATION_ID_KEY, correlationId);

            // Add to response headers
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            httpResponse.setHeader(REQUEST_ID_HEADER, correlationId);

            // Continue with the filter chain
            chain.doFilter(request, response);

        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    /**
     * Gets correlation ID from request header or generates a new one.
     *
     * @param request the HTTP request
     * @return correlation ID
     */
    private String getOrGenerateCorrelationId(HttpServletRequest request) {
        // Check if correlation ID is provided in request headers
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = request.getHeader(REQUEST_ID_HEADER);
        }

        // Generate new correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }

        return correlationId;
    }

    /**
     * Generates a new correlation ID.
     *
     * @return new correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
