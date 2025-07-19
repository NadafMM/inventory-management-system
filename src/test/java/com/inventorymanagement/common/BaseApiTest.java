package com.inventorymanagement.common;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventorymanagement.application.InventoryManagementApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Base class for full Spring Boot integration tests without DataJpaTest conflicts
 */
@SpringBootTest(
        classes = InventoryManagementApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseApiTest {

    protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired private WebApplicationContext context;

    /**
     * Setup method to initialize MockMvc
     */
    protected void setupMockMvc() {
        if (mockMvc == null) {
            mockMvc = webAppContextSetup(context).build();
        }
    }

    /**
     * Clears all relevant data between tests
     */
    protected void clearTestData() {
        // Override in subclasses if needed to clear specific test data
    }

    /**
     * Helper method to convert object to JSON string
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Helper method to parse JSON response
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    /**
     * Clears all caches before each test
     */
    protected void clearAllCaches() {
        // Implementation would clear any caches
    }
}
