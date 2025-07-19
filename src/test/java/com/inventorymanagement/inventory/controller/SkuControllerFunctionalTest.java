package com.inventorymanagement.inventory.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventorymanagement.category.service.CategoryService;
import com.inventorymanagement.common.BaseApiTest;
import com.inventorymanagement.common.testdata.TestDataFactory;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.inventory.repository.SkuRepository;
import com.inventorymanagement.inventory.service.SkuService;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.repository.ProductRepository;
import com.inventorymanagement.product.service.ProductService;
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
 * Comprehensive functional tests for SkuController REST API endpoints
 */
@AutoConfigureWebMvc
@DisplayName("SkuController Functional Tests")
class SkuControllerFunctionalTest extends BaseApiTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private SkuRepository skuRepository;

    @Autowired private ProductRepository productRepository;

    @Autowired private SkuService skuService;
    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;

    private Product testProduct;
    private SkuDto testSkuDto;

    @BeforeEach
    void setUp() {
        setupMockMvc();
        clearTestData();

        // Create test product
        testProduct =
                TestDataFactory.product()
                        .withName("Test Product")
                        .withDescription("Test Description")
                        .build();
        testProduct = productRepository.save(testProduct);

        // Create test SKU DTO
        testSkuDto = new SkuDto();
        testSkuDto.setProductId(testProduct.getId());
        testSkuDto.setSkuCode("TEST-SKU-001");
        testSkuDto.setVariantName("Test Variant");
        testSkuDto.setSize("M");
        testSkuDto.setColor("Blue");
        testSkuDto.setPrice(new BigDecimal("99.99"));
        testSkuDto.setCost(new BigDecimal("49.99"));
        testSkuDto.setStockQuantity(100);
        testSkuDto.setReorderPoint(20);
        testSkuDto.setReorderQuantity(50);
        testSkuDto.setBarcode("123456789");
        testSkuDto.setLocation("A1-B2");
        testSkuDto.setIsActive(true);
    }

    @Nested
    @DisplayName("Create SKU API Tests")
    class CreateSkuApiTests {

        @Test
        @DisplayName("POST /v1/skus - Should create SKU successfully")
        void createSkuSuccessfully() throws Exception {
            MvcResult result =
                    mockMvc
                            .perform(
                                    post("/v1/skus")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(testSkuDto)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.success").value(true))
                            .andExpect(jsonPath("$.message").value("SKU created successfully"))
                            .andExpect(jsonPath("$.data.skuCode").value("TEST-SKU-001"))
                            .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
                            .andExpect(jsonPath("$.data.variantName").value("Test Variant"))
                            .andExpect(jsonPath("$.data.size").value("M"))
                            .andExpect(jsonPath("$.data.color").value("Blue"))
                            .andExpect(jsonPath("$.data.price").value(99.99))
                            .andExpect(jsonPath("$.data.cost").value(49.99))
                            .andExpect(jsonPath("$.data.stockQuantity").value(100))
                            .andExpect(jsonPath("$.data.reorderPoint").value(20))
                            .andExpect(jsonPath("$.data.reorderQuantity").value(50))
                            .andExpect(jsonPath("$.data.barcode").value("123456789"))
                            .andExpect(jsonPath("$.data.location").value("A1-B2"))
                            .andExpect(jsonPath("$.data.isActive").value(true))
                            .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response).contains("SKU created successfully");
        }

        @Test
        @DisplayName("POST /v1/skus - Should return 400 for invalid SKU data")
        void createSkuWithInvalidData() throws Exception {
            testSkuDto.setProductId(null); // Invalid data

            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testSkuDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /v1/skus - Should return 409 for duplicate SKU code")
        void createSkuWithDuplicateCode() throws Exception {
            // Create first SKU
            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testSkuDto)))
                    .andExpect(status().isCreated());

            // Try to create duplicate
            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testSkuDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("POST /v1/skus - Should auto-generate SKU code when not provided")
        void createSkuWithAutoGeneratedCode() throws Exception {
            testSkuDto.setSkuCode(null);

            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testSkuDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.skuCode").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Get SKU API Tests")
    class GetSkuApiTests {

        private Long createdSkuId;
        private String createdSkuCode;

        @BeforeEach
        void setUp() throws Exception {
            // Create a SKU for testing
            MvcResult result =
                    mockMvc
                            .perform(
                                    post("/v1/skus")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(testSkuDto)))
                            .andExpect(status().isCreated())
                            .andReturn();

            String response = result.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            createdSkuId = Long.valueOf(data.get("id").toString());
            createdSkuCode = data.get("skuCode").toString();
        }

        @Test
        @DisplayName("GET /v1/skus/{id} - Should retrieve SKU by ID")
        void getSkuById() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/{id}", createdSkuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(createdSkuId))
                    .andExpect(jsonPath("$.data.skuCode").value(createdSkuCode))
                    .andExpect(jsonPath("$.data.productId").value(testProduct.getId()));
        }

        @Test
        @DisplayName("GET /v1/skus/{id} - Should return 404 for non-existent SKU")
        void getSkuByNonExistentId() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("GET /v1/skus/code/{skuCode} - Should retrieve SKU by code")
        void getSkuByCode() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/code/{skuCode}", createdSkuCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.skuCode").value(createdSkuCode))
                    .andExpect(jsonPath("$.data.id").value(createdSkuId));
        }

        @Test
        @DisplayName("GET /v1/skus/code/{skuCode} - Should return 404 for non-existent code")
        void getSkuByNonExistentCode() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/code/{skuCode}", "NON-EXISTENT"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("GET /v1/skus - Should retrieve all SKUs with pagination")
        void getAllSkusWithPagination() throws Exception {
            // Create additional SKUs
            for (int i = 1; i <= 3; i++) {
                SkuDto additionalSku = new SkuDto();
                additionalSku.setProductId(testProduct.getId());
                additionalSku.setSkuCode("ADDITIONAL-SKU-" + i);
                additionalSku.setPrice(new BigDecimal("50.00"));

                mockMvc
                        .perform(
                                post("/v1/skus")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(additionalSku)))
                        .andExpect(status().isCreated());
            }

            mockMvc
                    .perform(
                            get("/v1/skus")
                                    .param("page", "0")
                                    .param("size", "2")
                                    .param("sortBy", "skuCode")
                                    .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(4))
                    .andExpect(jsonPath("$.data.totalPages").value(2))
                    .andExpect(jsonPath("$.data.size").value(2))
                    .andExpect(jsonPath("$.data.number").value(0));
        }

        @Test
        @DisplayName("GET /v1/skus - Should filter SKUs by parameters")
        void getAllSkusWithFilters() throws Exception {
            mockMvc
                    .perform(
                            get("/v1/skus")
                                    .param("skuCode", "TEST")
                                    .param("productId", testProduct.getId().toString())
                                    .param("isActive", "true")
                                    .param("minPrice", "90.00")
                                    .param("maxPrice", "110.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].skuCode").value(createdSkuCode));
        }

        @Test
        @DisplayName("GET /v1/skus/product/{productId} - Should get SKUs by product")
        void getSkusByProduct() throws Exception {
            mockMvc
                    .perform(
                            get("/v1/skus/product/{productId}", testProduct.getId())
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].productId").value(testProduct.getId()));
        }

        @Test
        @DisplayName("GET /v1/skus/product/{productId} - Should return 404 for non-existent product")
        void getSkusByNonExistentProduct() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/product/{productId}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Update SKU API Tests")
    class UpdateSkuApiTests {

        private Long createdSkuId;

        @BeforeEach
        void setUp() throws Exception {
            // Create a SKU for testing
            MvcResult result =
                    mockMvc
                            .perform(
                                    post("/v1/skus")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(testSkuDto)))
                            .andExpect(status().isCreated())
                            .andReturn();

            String response = result.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            createdSkuId = Long.valueOf(data.get("id").toString());
        }

        @Test
        @DisplayName("PUT /v1/skus/{id} - Should update SKU successfully")
        void updateSkuSuccessfully() throws Exception {
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(testProduct.getId());
            updateDto.setSkuCode("TEST-SKU-001");
            updateDto.setVariantName("Updated Variant");
            updateDto.setPrice(new BigDecimal("149.99"));
            updateDto.setCost(new BigDecimal("75.00"));
            updateDto.setReorderPoint(30);
            updateDto.setIsActive(true);

            mockMvc
                    .perform(
                            put("/v1/skus/{id}", createdSkuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU updated successfully"))
                    .andExpect(jsonPath("$.data.variantName").value("Updated Variant"))
                    .andExpect(jsonPath("$.data.price").value(149.99))
                    .andExpect(jsonPath("$.data.cost").value(75.00))
                    .andExpect(jsonPath("$.data.reorderPoint").value(30));
        }

        @Test
        @DisplayName("PUT /v1/skus/{id} - Should return 404 for non-existent SKU")
        void updateNonExistentSku() throws Exception {
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(testProduct.getId());
            updateDto.setPrice(new BigDecimal("100.00"));

            mockMvc
                    .perform(
                            put("/v1/skus/{id}", 99999L)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PUT /v1/skus/{id} - Should return 400 for invalid data")
        void updateSkuWithInvalidData() throws Exception {
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(null); // Invalid data

            mockMvc
                    .perform(
                            put("/v1/skus/{id}", createdSkuId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Delete SKU API Tests")
    class DeleteSkuApiTests {

        private Long createdSkuId;

        @BeforeEach
        void setUp() throws Exception {
            // Create a SKU for testing
            MvcResult result =
                    mockMvc
                            .perform(
                                    post("/v1/skus")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(testSkuDto)))
                            .andExpect(status().isCreated())
                            .andReturn();

            String response = result.getResponse().getContentAsString();
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            createdSkuId = Long.valueOf(data.get("id").toString());
        }

        @Test
        @DisplayName("DELETE /v1/skus/{id} - Should delete SKU successfully")
        void deleteSkuSuccessfully() throws Exception {
            mockMvc
                    .perform(delete("/v1/skus/{id}", createdSkuId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU deleted successfully"));

            // Verify SKU is soft deleted by trying to retrieve it
            mockMvc.perform(get("/v1/skus/{id}", createdSkuId)).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /v1/skus/{id} - Should return 404 for non-existent SKU")
        void deleteNonExistentSku() throws Exception {
            mockMvc
                    .perform(delete("/v1/skus/{id}", 99999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Search SKU API Tests")
    class SearchSkuApiTests {

        @BeforeEach
        void setUp() throws Exception {
            // Create multiple SKUs for search testing
            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testSkuDto)))
                    .andExpect(status().isCreated());

            SkuDto searchSku = new SkuDto();
            searchSku.setProductId(testProduct.getId());
            searchSku.setSkuCode("SEARCH-SKU-001");
            searchSku.setVariantName("Searchable Variant");
            searchSku.setPrice(new BigDecimal("75.00"));

            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(searchSku)))
                    .andExpect(status().isCreated());

            SkuDto anotherSku = new SkuDto();
            anotherSku.setProductId(testProduct.getId());
            anotherSku.setSkuCode("ANOTHER-SKU-002");
            anotherSku.setVariantName("Another Variant");
            anotherSku.setPrice(new BigDecimal("125.00"));

            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(anotherSku)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("GET /v1/skus/search - Should search SKUs by query")
        void searchSkusByQuery() throws Exception {
            mockMvc
                    .perform(
                            get("/v1/skus/search")
                                    .param("query", "SEARCH")
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].skuCode").value("SEARCH-SKU-001"));
        }

        @Test
        @DisplayName("GET /v1/skus/search - Should return 400 for empty query")
        void searchSkusWithEmptyQuery() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/search").param("query", "").param("page", "0").param("size", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("GET /v1/skus/low-stock - Should get low stock SKUs")
        void getLowStockSkus() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/low-stock").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /v1/skus/price-range - Should get SKUs by price range")
        void getSkusByPriceRange() throws Exception {
            mockMvc
                    .perform(
                            get("/v1/skus/price-range")
                                    .param("minPrice", "70.00")
                                    .param("maxPrice", "100.00")
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /v1/skus/price-range - Should return 400 for invalid price range")
        void getSkusByInvalidPriceRange() throws Exception {
            mockMvc
                    .perform(
                            get("/v1/skus/price-range")
                                    .param("minPrice", "100.00")
                                    .param("maxPrice", "50.00")
                                    .param("page", "0")
                                    .param("size", "10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Bulk Operations API Tests")
    class BulkOperationsApiTests {

        @Test
        @DisplayName("POST /v1/skus/bulk - Should create multiple SKUs")
        void createMultipleSKUs() throws Exception {
            // Create multiple SKU DTOs
            SkuDto[] skuDtos = new SkuDto[3];
            for (int i = 0; i < 3; i++) {
                SkuDto skuDto = new SkuDto();
                skuDto.setProductId(testProduct.getId());
                skuDto.setSkuCode("BULK-SKU-" + (i + 1));
                skuDto.setPrice(new BigDecimal("50.00"));
                skuDtos[i] = skuDto;
            }

            mockMvc
                    .perform(
                            post("/v1/skus/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(skuDtos)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.successful").value(3))
                    .andExpect(jsonPath("$.data.failed").value(0));
        }

        @Test
        @DisplayName("PUT /v1/skus/bulk/activate - Should activate multiple SKUs")
        void activateMultipleSKUs() throws Exception {
            // Create and then deactivate some SKUs
            Long[] skuIds = new Long[2];
            for (int i = 0; i < 2; i++) {
                SkuDto skuDto = new SkuDto();
                skuDto.setProductId(testProduct.getId());
                skuDto.setSkuCode("ACTIVATE-SKU-" + (i + 1));
                skuDto.setPrice(new BigDecimal("50.00"));

                MvcResult result =
                        mockMvc
                                .perform(
                                        post("/v1/skus")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(skuDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                skuIds[i] = Long.valueOf(data.get("id").toString());
            }

            mockMvc
                    .perform(
                            put("/v1/skus/bulk/activate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(skuIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.successful").value(2))
                    .andExpect(jsonPath("$.data.failed").value(0));
        }

        @Test
        @DisplayName("PUT /v1/skus/bulk/deactivate - Should deactivate multiple SKUs")
        void deactivateMultipleSKUs() throws Exception {
            // Create some SKUs
            Long[] skuIds = new Long[2];
            for (int i = 0; i < 2; i++) {
                SkuDto skuDto = new SkuDto();
                skuDto.setProductId(testProduct.getId());
                skuDto.setSkuCode("DEACTIVATE-SKU-" + (i + 1));
                skuDto.setPrice(new BigDecimal("50.00"));

                MvcResult result =
                        mockMvc
                                .perform(
                                        post("/v1/skus")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(skuDto)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String response = result.getResponse().getContentAsString();
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                skuIds[i] = Long.valueOf(data.get("id").toString());
            }

            mockMvc
                    .perform(
                            put("/v1/skus/bulk/deactivate")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(skuIds)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.successful").value(2))
                    .andExpect(jsonPath("$.data.failed").value(0));
        }
    }

    @Nested
    @DisplayName("Error Handling API Tests")
    class ErrorHandlingApiTests {

        @Test
        @DisplayName("Should handle validation errors gracefully")
        void handleValidationErrors() throws Exception {
            SkuDto invalidSku = new SkuDto();
            // Missing required fields

            mockMvc
                    .perform(
                            post("/v1/skus")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidSku)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("Should handle invalid path variables")
        void handleInvalidPathVariables() throws Exception {
            mockMvc
                    .perform(get("/v1/skus/{id}", "invalid-id"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should handle invalid request parameters")
        void handleInvalidRequestParameters() throws Exception {
            mockMvc
                    .perform(get("/v1/skus").param("page", "-1").param("size", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
