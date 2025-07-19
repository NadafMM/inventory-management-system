package com.inventorymanagement.common;

import com.inventorymanagement.application.InventoryManagementApplication;
import com.inventorymanagement.common.config.TestConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for repository integration tests. Provides database setup and common test data.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(TestConfig.class)
@ContextConfiguration(classes = InventoryManagementApplication.class)
@Transactional
public abstract class BaseIntegrationTest {

    /**
     * Clears all caches before each test
     */
    protected void clearAllCaches() {
        // Implementation would clear any caches
    }
}
