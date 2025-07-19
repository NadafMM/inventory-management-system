package com.inventorymanagement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.common.BaseApiTest;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.product.model.ProductDto;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * End-to-end workflow tests that test complete business scenarios
 */
@AutoConfigureWebMvc
@DisplayName("End-to-End Workflow Tests")
class EndToEndWorkflowTest extends BaseApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        setupMockMvc();
        clearTestData();
    }

    // Helper methods
    private Long extractIdFromResponse(MvcResult result) throws Exception {
        String response = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
        return Long.valueOf(data.get("id").toString());
    }

    private Long createCompleteProductSetup() throws Exception {
        // Create Category
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Test Category");
        categoryDto.setDescription("Test Description");
        categoryDto.setIsActive(true);

        MvcResult categoryResult =
                mockMvc
                        .perform(
                                post("/v1/categories")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(categoryDto)))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long categoryId = extractIdFromResponse(categoryResult);

        // Create Product
        ProductDto productDto = new ProductDto();
        productDto.setName("Test Product");
        productDto.setDescription("Test Description");
        productDto.setCategoryId(categoryId);
        productDto.setBrand("TestBrand");

        productDto.setIsActive(true);

        MvcResult productResult =
                mockMvc
                        .perform(
                                post("/v1/products")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(productDto)))
                        .andExpect(status().isCreated())
                        .andReturn();

        Long productId = extractIdFromResponse(productResult);

        // Create SKU
        SkuDto skuDto = new SkuDto();
        skuDto.setProductId(productId);
        skuDto.setSkuCode("TEST-SKU-001");
        skuDto.setVariantName("Test Variant");
        skuDto.setPrice(new BigDecimal("99.99"));
        skuDto.setCost(new BigDecimal("49.99"));
        skuDto.setStockQuantity(50);
        skuDto.setReorderPoint(10);
        skuDto.setReorderQuantity(30);
        skuDto.setIsActive(true);

        MvcResult skuResult =
                mockMvc
                        .perform(
                                post("/v1/skus")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(skuDto)))
                        .andExpect(status().isCreated())
                        .andReturn();

        return extractIdFromResponse(skuResult);
    }

    private void setupComplexProductCatalog() throws Exception {
        // Create multiple categories
        String[] categoryNames = {"Electronics", "Clothing", "Books", "Home & Garden"};
        Long[] categoryIds = new Long[categoryNames.length];

        for (int i = 0; i < categoryNames.length; i++) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(categoryNames[i]);
            categoryDto.setDescription(categoryNames[i] + " category");
            categoryDto.setIsActive(true);

            MvcResult result =
                    mockMvc
                            .perform(
                                    post("/v1/categories")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(categoryDto)))
                            .andExpect(status().isCreated())
                            .andReturn();

            categoryIds[i] = extractIdFromResponse(result);
        }

        // Create products for each category
        String[][] productData = {
                {"Smartphone", "Laptop", "Tablet"},
                {"T-Shirt", "Jeans", "Sneakers"},
                {"Novel", "Cookbook", "Biography"},
                {"Plant Pot", "Garden Tool", "Outdoor Chair"}
        };

        for (int i = 0; i < categoryIds.length; i++) {
            for (String productName : productData[i]) {
                ProductDto productDto = new ProductDto();
                productDto.setName(productName);
                productDto.setDescription(productName + " description");
                productDto.setCategoryId(categoryIds[i]);
                productDto.setBrand("Brand" + i);

                productDto.setIsActive(true);

                MvcResult productResult =
                        mockMvc
                                .perform(
                                        post("/v1/products")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(productDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long productId = extractIdFromResponse(productResult);

                // Create SKU for each product
                SkuDto skuDto = new SkuDto();
                skuDto.setProductId(productId);
                skuDto.setSkuCode(productName.toUpperCase().replace(" ", "-") + "-001");
                skuDto.setVariantName("Standard");
                skuDto.setPrice(BigDecimal.valueOf((i + 1) * 100 + Math.random() * 200));
                skuDto.setCost(skuDto.getPrice().multiply(new BigDecimal("0.6")));
                skuDto.setStockQuantity((int) (Math.random() * 100) + 10);
                skuDto.setReorderPoint(5);
                skuDto.setReorderQuantity(20);
                skuDto.setIsActive(true);

                mockMvc
                        .perform(
                                post("/v1/skus")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(skuDto)))
                        .andExpect(status().isCreated());
            }
        }
    }

    @Nested
    @DisplayName("Complete Product Creation Workflow")
    class CompleteProductCreationWorkflow {

        @Test
        @DisplayName("Should create complete product hierarchy: Category -> Product -> SKU")
        void createCompleteProductHierarchy() throws Exception {
            // Step 1: Create Category
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName("Electronics");
            categoryDto.setDescription("Electronic devices and accessories");
            categoryDto.setIsActive(true);

            MvcResult categoryResult =
                    mockMvc
                            .perform(
                                    post("/v1/categories")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(categoryDto)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.data.name").value("Electronics"))
                            .andReturn();

            Long categoryId = extractIdFromResponse(categoryResult);

            // Step 2: Create Product
            ProductDto productDto = new ProductDto();
            productDto.setName("Smartphone");
            productDto.setDescription("Latest smartphone with advanced features");
            productDto.setCategoryId(categoryId);
            productDto.setBrand("TechBrand");

            productDto.setIsActive(true);

            MvcResult productResult =
                    mockMvc
                            .perform(
                                    post("/v1/products")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(productDto)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.data.name").value("Smartphone"))
                            .andExpect(jsonPath("$.data.categoryId").value(categoryId))
                            .andReturn();

            Long productId = extractIdFromResponse(productResult);

            // Step 3: Create Multiple SKUs for the Product
            String[] colors = {"Black", "White", "Blue"};
            String[] sizes = {"64GB", "128GB", "256GB"};
            BigDecimal basePrice = new BigDecimal("699.99");

            for (int i = 0; i < colors.length; i++) {
                for (int j = 0; j < sizes.length; j++) {
                    SkuDto skuDto = new SkuDto();
                    skuDto.setProductId(productId);
                    skuDto.setSkuCode(String.format("PHONE-%s-%s", colors[i].toUpperCase(), sizes[j]));
                    skuDto.setVariantName(String.format("%s %s", colors[i], sizes[j]));
                    skuDto.setColor(colors[i]);
                    skuDto.setSize(sizes[j]);
                    skuDto.setPrice(basePrice.add(new BigDecimal(j * 100))); // Price increases with storage
                    skuDto.setCost(basePrice.multiply(new BigDecimal("0.6")));
                    skuDto.setStockQuantity(50 + (i * 10) + (j * 5));
                    skuDto.setReorderPoint(10);
                    skuDto.setReorderQuantity(30);
                    skuDto.setIsActive(true);

                    mockMvc
                            .perform(
                                    post("/v1/skus")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(skuDto)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.data.skuCode").value(skuDto.getSkuCode()))
                            .andExpect(jsonPath("$.data.productId").value(productId));
                }
            }

            // Step 4: Verify Product has SKUs
            mockMvc
                    .perform(get("/v1/skus/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(9)) // 3 colors × 3 sizes
                    .andExpect(jsonPath("$.data.content").isArray());

            // Step 5: Verify Category has Products
            mockMvc
                    .perform(get("/v1/products").param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].categoryId").value(categoryId));
        }
    }

    @Nested
    @DisplayName("Complete Inventory Management Workflow")
    class CompleteInventoryManagementWorkflow {

        @Test
        @DisplayName(
                "Should handle complete inventory lifecycle: Stock In -> Reserve -> Release -> Adjust -> Stock Out")
        void handleCompleteInventoryLifecycle() throws Exception {
            // Setup: Create Category, Product, and SKU
            Long skuId = createCompleteProductSetup();

            // Step 1: Stock In - Add more inventory
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/stock-in", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "quantity": 100,
                                                        "referenceId": "PO-001",
                                                        "reason": "Purchase order received",
                                                        "performedBy": "WAREHOUSE_MANAGER"
                                                    }
                                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("Stock added successfully"));

            // Verify stock was added
            mockMvc
                    .perform(get("/v1/skus/{id}", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stockQuantity").value(150)); // 50 initial + 100 added

            // Step 2: Reserve Stock for an order
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/reserve", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "quantity": 25,
                                                        "referenceId": "ORDER-12345",
                                                        "referenceType": "SALES_ORDER",
                                                        "reason": "Customer order",
                                                        "performedBy": "SALES_SYSTEM"
                                                    }
                                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("Stock reserved successfully"));

            // Verify reservation
            mockMvc
                    .perform(get("/v1/skus/{id}", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stockQuantity").value(150))
                    .andExpect(jsonPath("$.data.reservedQuantity").value(25))
                    .andExpect(jsonPath("$.data.availableQuantity").value(125));

            // Step 3: Release some reserved stock (partial order cancellation)
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/release", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "quantity": 10,
                                                        "referenceId": "ORDER-12345",
                                                        "referenceType": "SALES_ORDER",
                                                        "reason": "Partial order cancellation",
                                                        "performedBy": "CUSTOMER_SERVICE"
                                                    }
                                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("Stock released successfully"));

            // Verify release
            mockMvc
                    .perform(get("/v1/skus/{id}", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reservedQuantity").value(15))
                    .andExpect(jsonPath("$.data.availableQuantity").value(135));

            // Step 4: Adjust stock (inventory count correction)
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/adjust", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "adjustment": -5,
                                                        "reason": "Inventory count correction - damaged items found",
                                                        "performedBy": "WAREHOUSE_SUPERVISOR"
                                                    }
                                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("Stock adjusted successfully"));

            // Verify adjustment
            mockMvc
                    .perform(get("/v1/skus/{id}", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stockQuantity").value(145)); // 150 - 5

            // Step 5: Stock Out (fulfill remaining reserved stock)
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/stock-out", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "quantity": 15,
                                                        "referenceId": "ORDER-12345",
                                                        "referenceType": "SALES_ORDER",
                                                        "reason": "Order fulfillment",
                                                        "performedBy": "FULFILLMENT_CENTER"
                                                    }
                                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("Stock removed successfully"));

            // Verify final state
            mockMvc
                    .perform(get("/v1/skus/{id}", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stockQuantity").value(130)) // 145 - 15
                    .andExpect(jsonPath("$.data.reservedQuantity").value(0)) // All reserved stock fulfilled
                    .andExpect(jsonPath("$.data.availableQuantity").value(130));

            // Step 6: Verify transaction history
            mockMvc
                    .perform(get("/v1/inventory/{skuId}/transactions", skuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(6)); // Initial + 5 transactions
        }
    }

    @Nested
    @DisplayName("Complete Search and Filter Workflow")
    class CompleteSearchAndFilterWorkflow {

        @Test
        @DisplayName("Should handle complex search and filtering scenarios")
        void handleComplexSearchAndFiltering() throws Exception {
            // Setup: Create multiple categories, products, and SKUs with different attributes
            setupComplexProductCatalog();

            // Test 1: Search products by category
            mockMvc
                    .perform(
                            get("/v1/products")
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param("sortBy", "name")
                                    .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            // Test 2: Search SKUs by price range
            mockMvc
                    .perform(
                            get("/v1/skus/price-range")
                                    .param("minPrice", "100.00")
                                    .param("maxPrice", "500.00")
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            // Test 3: Search SKUs with multiple filters
            mockMvc
                    .perform(
                            get("/v1/skus")
                                    .param("isActive", "true")
                                    .param("minPrice", "200.00")
                                    .param("maxPrice", "800.00")
                                    .param("page", "0")
                                    .param("size", "5")
                                    .param("sortBy", "price")
                                    .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.size").value(5));

            // Test 4: Search SKUs by text query
            mockMvc
                    .perform(
                            get("/v1/skus/search").param("query", "Phone").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());

            // Test 5: Get low stock SKUs
            mockMvc
                    .perform(get("/v1/skus/low-stock").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("Complete Error Handling Workflow")
    class CompleteErrorHandlingWorkflow {

        @Test
        @DisplayName("Should handle various error scenarios gracefully")
        void handleVariousErrorScenariosGracefully() throws Exception {
            // Test 1: Entity not found errors
            mockMvc
                    .perform(get("/v1/categories/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());

            mockMvc
                    .perform(get("/v1/products/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            mockMvc
                    .perform(get("/v1/skus/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));

            // Test 2: Validation errors
            CategoryDto invalidCategory = new CategoryDto();
            // Missing required name field

            mockMvc
                    .perform(
                            post("/v1/categories")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidCategory)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors").exists());

            // Test 3: Business rule violations
            Long skuId = createCompleteProductSetup();

            // Try to reserve more stock than available
            mockMvc
                    .perform(
                            post("/v1/inventory/{skuId}/reserve", skuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            """
                                                    {
                                                        "quantity": 1000,
                                                        "referenceId": "ORDER-99999",
                                                        "referenceType": "SALES_ORDER",
                                                        "reason": "Large order",
                                                        "performedBy": "SALES_SYSTEM"
                                                    }
                                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));

            // Test 4: Invalid request parameters
            mockMvc
                    .perform(get("/v1/skus").param("page", "-1").param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            // Test 5: Invalid path variables
            mockMvc
                    .perform(get("/v1/categories/{id}", "invalid-id"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
