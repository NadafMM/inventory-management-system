package com.inventorymanagement.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.GlobalExceptionHandler;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.model.BulkOperationRequest;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.inventory.service.InventoryService;
import com.inventorymanagement.inventory.service.SkuService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for SkuController
 */
@WebMvcTest(SkuController.class)
@ContextConfiguration(classes = {SkuControllerTest.TestConfig.class, SkuController.class})
@ActiveProfiles("test")
@DisplayName("SkuController Tests")
class SkuControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private SkuService skuService;

    @MockBean private InventoryService inventoryService;

    private SkuDto testSku;
    private SkuDto testSku2;

    /**
     * Helper method to convert object to JSON string
     */
    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    void setUp() {
        testSku = new SkuDto();
        testSku.setId(1L);
        testSku.setSkuCode("TEST-SKU-001");
        testSku.setProductId(1L);
        testSku.setPrice(BigDecimal.valueOf(99.99));
        testSku.setIsActive(true);

        testSku2 = new SkuDto();
        testSku2.setId(2L);
        testSku2.setSkuCode("TEST-SKU-002");
        testSku2.setProductId(2L);
        testSku2.setPrice(BigDecimal.valueOf(149.99));
        testSku2.setIsActive(true);
    }

    @SpringBootApplication
    @Import(GlobalExceptionHandler.class)
    static class TestConfig {
        // Minimal configuration for WebMvcTest
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        void createSku_Success() throws Exception {
            when(skuService.createSku(any(SkuDto.class))).thenReturn(testSku);

            mockMvc
                    .perform(
                            post("/v1/skus").contentType(MediaType.APPLICATION_JSON).content(toJson(testSku)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.sku_code").value("TEST-SKU-001"));

            verify(skuService).createSku(any(SkuDto.class));
        }

        @Test
        void createSku_ValidationError() throws Exception {
            when(skuService.createSku(any(SkuDto.class)))
                    .thenThrow(
                            new ValidationException(
                                    "Validation failed for field 'skuCode': SKU code is required"));

            mockMvc
                    .perform(
                            post("/v1/skus").contentType(MediaType.APPLICATION_JSON).content(toJson(testSku)))
                    .andExpect(status().isBadRequest());

            verify(skuService).createSku(any(SkuDto.class));
        }

        @Test
        void getSkuById_Success() throws Exception {
            when(skuService.getSkuById(1L)).thenReturn(testSku);

            mockMvc
                    .perform(get("/v1/skus/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.sku_code").value("TEST-SKU-001"));

            verify(skuService).getSkuById(1L);
        }

        @Test
        void getSkuById_NotFound() throws Exception {
            when(skuService.getSkuById(999L)).thenThrow(new EntityNotFoundException("SKU", 999L));

            mockMvc.perform(get("/v1/skus/999")).andExpect(status().isNotFound());

            verify(skuService).getSkuById(999L);
        }

        @Test
        void getSkuByCode_Success() throws Exception {
            when(skuService.getSkuByCode("TEST-SKU-001")).thenReturn(testSku);

            mockMvc
                    .perform(get("/v1/skus/code/TEST-SKU-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sku_code").value("TEST-SKU-001"));

            verify(skuService).getSkuByCode("TEST-SKU-001");
        }

        @Test
        void getSkuByCode_NotFound() throws Exception {
            when(skuService.getSkuByCode("INVALID-CODE"))
                    .thenThrow(new EntityNotFoundException("SKU", "INVALID-CODE"));

            mockMvc.perform(get("/v1/skus/code/INVALID-CODE")).andExpect(status().isNotFound());

            verify(skuService).getSkuByCode("INVALID-CODE");
        }

        @Test
        void updateSku_Success() throws Exception {
            SkuDto updatedSku = new SkuDto();
            updatedSku.setId(1L);
            updatedSku.setSkuCode("TEST-SKU-001-UPDATED");
            updatedSku.setProductId(1L);
            updatedSku.setPrice(BigDecimal.valueOf(99.99));
            updatedSku.setIsActive(true);

            when(skuService.updateSku(eq(1L), any(SkuDto.class))).thenReturn(updatedSku);

            mockMvc
                    .perform(
                            put("/v1/skus/1").contentType(MediaType.APPLICATION_JSON).content(toJson(updatedSku)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU updated successfully"))
                    .andExpect(jsonPath("$.data.sku_code").value("TEST-SKU-001-UPDATED"));

            verify(skuService).updateSku(eq(1L), any(SkuDto.class));
        }

        @Test
        void updateSku_NotFound() throws Exception {
            when(skuService.updateSku(eq(999L), any(SkuDto.class)))
                    .thenThrow(new EntityNotFoundException("SKU", 999L));

            mockMvc
                    .perform(
                            put("/v1/skus/999").contentType(MediaType.APPLICATION_JSON).content(toJson(testSku)))
                    .andExpect(status().isNotFound());

            verify(skuService).updateSku(eq(999L), any(SkuDto.class));
        }

        @Test
        void deleteSku_Success() throws Exception {
            doNothing().when(skuService).deleteSku(1L);

            mockMvc
                    .perform(delete("/v1/skus/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("SKU deleted successfully"));

            verify(skuService).deleteSku(1L);
        }

        @Test
        void deleteSku_NotFound() throws Exception {
            doThrow(new EntityNotFoundException("SKU", 999L)).when(skuService).deleteSku(999L);

            mockMvc.perform(delete("/v1/skus/999")).andExpect(status().isNotFound());

            verify(skuService).deleteSku(999L);
        }
    }

    @Nested
    @DisplayName("Search and Filtering")
    class SearchAndFiltering {

        @Test
        void getAllSkus_Success() throws Exception {
            Page<SkuDto> skusPage = new PageImpl<>(Arrays.asList(testSku, testSku2));
            when(skuService.getAllSkus(any(Pageable.class))).thenReturn(skusPage);

            mockMvc
                    .perform(get("/v1/skus").param("page", "0").param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2));

            verify(skuService).getAllSkus(any(Pageable.class));
        }

        @Test
        void getAllSkus_WithFilters() throws Exception {
            Page<SkuDto> skusPage = new PageImpl<>(Collections.singletonList(testSku));
            when(skuService.searchSkus(
                    any(), // Allow null or any string
                    any(), // Allow null or any Long
                    any(), // Allow null or any string
                    any(), // Allow null or any BigDecimal
                    any(), // Allow null or any BigDecimal
                    any(), // Allow null or any Boolean
                    any(), // Allow null or any Boolean
                    any(Pageable.class)))
                    .thenReturn(skusPage);

            mockMvc
                    .perform(
                            get("/v1/skus")
                                    .param("skuCode", "TEST")
                                    .param("productId", "1")
                                    .param("isActive", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            verify(skuService)
                    .searchSkus(
                            eq("TEST"),
                            eq(1L),
                            any(), // Allow null for variantName
                            any(), // Allow null for minPrice
                            any(), // Allow null for maxPrice
                            eq(true),
                            any(), // Allow null for isLowStock
                            any(Pageable.class));
        }

        @Test
        void searchSkus_Success() throws Exception {
            Page<SkuDto> skusPage = new PageImpl<>(Collections.singletonList(testSku));
            when(skuService.searchSkusByCodeOrVariant(eq("TEST"), any(Pageable.class)))
                    .thenReturn(skusPage);

            mockMvc
                    .perform(get("/v1/skus/search").param("query", "TEST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Search completed successfully"));

            verify(skuService).searchSkusByCodeOrVariant(eq("TEST"), any(Pageable.class));
        }

        @Test
        void getSkusByProduct_Success() throws Exception {
            Page<SkuDto> skusPage = new PageImpl<>(Collections.singletonList(testSku));
            when(skuService.getSkusByProduct(eq(1L), any(Pageable.class))).thenReturn(skusPage);

            mockMvc
                    .perform(get("/v1/skus/product/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            verify(skuService).getSkusByProduct(eq(1L), any(Pageable.class));
        }

        @Test
        void getLowStockSkus_Success() throws Exception {
            Page<SkuDto> skusPage = new PageImpl<>(Collections.singletonList(testSku));
            when(skuService.getLowStockSkus(any(Pageable.class))).thenReturn(skusPage);

            mockMvc
                    .perform(get("/v1/skus/low-stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Low stock SKUs retrieved successfully"));

            verify(skuService).getLowStockSkus(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Inventory Operations")
    class InventoryOperations {

        @Test
        void addStock_Success() throws Exception {
            when(skuService.addStock(1L, 10)).thenReturn(testSku);

            mockMvc
                    .perform(post("/v1/skus/1/stock/add").param("quantity", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Stock added successfully"));

            verify(skuService).addStock(1L, 10);
        }

        @Test
        void addStock_InvalidQuantity() throws Exception {
            mockMvc
                    .perform(post("/v1/skus/1/stock/add").param("quantity", "0"))
                    .andExpect(status().isBadRequest()); // Validation error causes 500
        }

        @Test
        void removeStock_Success() throws Exception {
            when(skuService.removeStock(1L, 5)).thenReturn(testSku);

            mockMvc
                    .perform(post("/v1/skus/1/stock/remove").param("quantity", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Stock removed successfully"));

            verify(skuService).removeStock(1L, 5);
        }

        @Test
        void removeStock_InsufficientStock() throws Exception {
            when(skuService.removeStock(1L, 100))
                    .thenThrow(new BusinessException("Insufficient stock available"));

            mockMvc
                    .perform(post("/v1/skus/1/stock/remove").param("quantity", "100"))
                    .andExpect(status().isUnprocessableEntity()); // BusinessException returns 422

            verify(skuService).removeStock(1L, 100);
        }

        @Test
        void reserveStock_Success() throws Exception {
            when(skuService.reserveStock(1L, 3)).thenReturn(testSku);

            mockMvc
                    .perform(post("/v1/skus/1/stock/reserve").param("quantity", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Stock reserved successfully"));

            verify(skuService).reserveStock(1L, 3);
        }

        @Test
        void releaseStock_Success() throws Exception {
            when(skuService.releaseStock(1L, 2)).thenReturn(testSku);

            mockMvc
                    .perform(post("/v1/skus/1/stock/release").param("quantity", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Stock released successfully"));

            verify(skuService).releaseStock(1L, 2);
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperations {

        @Test
        void bulkCreateSkus_Success() throws Exception {
            List<SkuDto> skus = Arrays.asList(testSku, testSku2);
            BulkOperationRequest<SkuDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(skus);

            when(skuService.createSku(any(SkuDto.class))).thenReturn(testSku).thenReturn(testSku2);

            mockMvc
                    .perform(
                            post("/v1/skus/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(skuService, times(2)).createSku(any(SkuDto.class));
        }

        @Test
        void bulkUpdateSkus_Success() throws Exception {
            List<SkuDto> skus = Arrays.asList(testSku, testSku2);
            BulkOperationRequest<SkuDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(skus);

            when(skuService.updateSku(eq(1L), any(SkuDto.class))).thenReturn(testSku);
            when(skuService.updateSku(eq(2L), any(SkuDto.class))).thenReturn(testSku2);

            mockMvc
                    .perform(
                            put("/v1/skus/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(skuService).updateSku(eq(1L), any(SkuDto.class));
            verify(skuService).updateSku(eq(2L), any(SkuDto.class));
        }

        @Test
        void bulkDeleteSkus_Success() throws Exception {
            List<Long> ids = Arrays.asList(1L, 2L);

            doNothing().when(skuService).deleteSku(1L);
            doNothing().when(skuService).deleteSku(2L);

            mockMvc
                    .perform(
                            delete("/v1/skus/bulk").contentType(MediaType.APPLICATION_JSON).content(toJson(ids)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(skuService).deleteSku(1L);
            verify(skuService).deleteSku(2L);
        }
    }
}
