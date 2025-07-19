package com.inventorymanagement.common;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for unit tests with Mockito support. Provides common setup and utilities for unit testing.
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {

    /**
     * Helper method to create a test exception
     */
    protected Exception createTestException(String message) {
        return new RuntimeException("Test exception: " + message);
    }
}
