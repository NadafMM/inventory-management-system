package com.inventorymanagement.common.config;

import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for performance logging across the application.
 *
 * <p>This aspect automatically logs method execution times for: - Service layer methods -
 * Controller methods - Repository methods (that take longer than threshold)
 *
 * <p>Performance metrics are logged to a separate performance log file for analysis and monitoring
 * purposes.
 */
@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger performanceLogger = LoggerFactory.getLogger("performance");
    private static final long SLOW_EXECUTION_THRESHOLD_MS = 1000; // 1 second
    private static final long WARNING_EXECUTION_THRESHOLD_MS = 5000; // 5 seconds

    /**
     * Logs execution time for all service methods.
     */
    @Around("execution(* com.inventorymanagement.*.service.*.*(..))")
    public Object logServiceMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "SERVICE");
    }

    /**
     * Logs execution time for all controller methods.
     */
    @Around("execution(* com.inventorymanagement.*.controller.*.*(..))")
    public Object logControllerMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "CONTROLLER");
    }

    /**
     * Logs execution time for repository methods that exceed threshold.
     */
    @Around("execution(* com.inventorymanagement.*.repository.*.*(..))")
    public Object logRepositoryMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "REPOSITORY");
    }

    /**
     * Common method to log performance metrics.
     */
    private Object logMethodPerformance(ProceedingJoinPoint joinPoint, String layer)
            throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        Object[] args = joinPoint.getArgs();

        long startTime = System.nanoTime();
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();

        try {
            // Execute the method
            Object result = joinPoint.proceed();

            long endTime = System.nanoTime();
            long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            // Log performance metrics
            logPerformanceMetrics(
                    layer, fullMethodName, executionTimeMs, correlationId, args, true, null);

            return result;

        } catch (Exception e) {
            long endTime = System.nanoTime();
            long executionTimeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            // Log performance metrics for failed execution
            logPerformanceMetrics(layer, fullMethodName, executionTimeMs, correlationId, args, false, e);

            throw e;
        }
    }

    /**
     * Logs performance metrics with detailed information.
     */
    private void logPerformanceMetrics(
            String layer,
            String methodName,
            long executionTimeMs,
            String correlationId,
            Object[] args,
            boolean success,
            Exception exception) {

        String status = success ? "SUCCESS" : "FAILED";
        String logLevel = determineLogLevel(executionTimeMs, success);

        String message =
                String.format(
                        "PERFORMANCE|%s|%s|%s|%dms|%s|args:%d|correlationId:%s%s",
                        layer,
                        methodName,
                        status,
                        executionTimeMs,
                        logLevel,
                        args != null ? args.length : 0,
                        correlationId != null ? correlationId : "N/A",
                        exception != null ? "|error:" + exception.getClass().getSimpleName() : "");

        // Always log to performance logger
        performanceLogger.info(message);

        // Log to main logger based on execution time and success
        Logger mainLogger = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

        if (!success) {
            mainLogger.warn(
                    "Method {} failed after {}ms: {}",
                    methodName,
                    executionTimeMs,
                    exception != null ? exception.getMessage() : "Unknown error");
        } else if (executionTimeMs > WARNING_EXECUTION_THRESHOLD_MS) {
            mainLogger.warn(
                    "Slow method execution: {} took {}ms (threshold: {}ms)",
                    methodName,
                    executionTimeMs,
                    WARNING_EXECUTION_THRESHOLD_MS);
        } else if (executionTimeMs > SLOW_EXECUTION_THRESHOLD_MS) {
            mainLogger.info(
                    "Method {} took {}ms (above normal threshold: {}ms)",
                    methodName,
                    executionTimeMs,
                    SLOW_EXECUTION_THRESHOLD_MS);
        }
    }

    /**
     * Determines the appropriate log level based on execution time and success.
     */
    private String determineLogLevel(long executionTimeMs, boolean success) {
        if (!success) {
            return "ERROR";
        } else if (executionTimeMs > WARNING_EXECUTION_THRESHOLD_MS) {
            return "WARN";
        } else if (executionTimeMs > SLOW_EXECUTION_THRESHOLD_MS) {
            return "SLOW";
        } else {
            return "NORMAL";
        }
    }

    /**
     * Formats method arguments for logging (truncated for readability).
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(args.length, 3); i++) { // Limit to first 3 args
            if (i > 0) {
                sb.append(", ");
            }
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String argStr = arg.toString();
                // Truncate long arguments
                if (argStr.length() > 50) {
                    sb.append(argStr, 0, 47).append("...");
                } else {
                    sb.append(argStr);
                }
            }
        }
        if (args.length > 3) {
            sb.append(", ... +").append(args.length - 3).append(" more");
        }
        sb.append("]");
        return sb.toString();
    }
}
