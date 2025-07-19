package com.inventorymanagement.product.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
import com.inventorymanagement.product.model.ProductDto;
import com.inventorymanagement.product.service.ProductService;
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
 * Test class for ProductController
 */
@WebMvcTest(ProductController.class)
@ContextConfiguration(classes = {ProductControllerTest.TestConfig.class, ProductController.class})
@ActiveProfiles("test")
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ProductService productService;
    private ProductDto testProduct;
    private ProductDto testProduct2;

    /**
     * Helper method to convert object to JSON string
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    void setUp() {
        testProduct = new ProductDto();
        testProduct.setId(1L);
        testProduct.setName("Test Product 1");
        testProduct.setBrand("Test Brand");
        testProduct.setCategoryId(1L); // Required field!
        testProduct.setIsActive(true);

        testProduct2 = new ProductDto();
        testProduct2.setId(2L);
        testProduct2.setName("Test Product 2");
        testProduct2.setBrand("Another Brand");
        testProduct2.setCategoryId(1L); // Required field!
        testProduct2.setIsActive(true);
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
        void createProduct_Success() throws Exception {
            when(productService.createProduct(any(ProductDto.class))).thenReturn(testProduct);

            mockMvc
                    .perform(
                            post("/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testProduct)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Product created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Test Product 1"));

            verify(productService).createProduct(any(ProductDto.class));
        }

        @Test
        void createProduct_ValidationError() throws Exception {
            when(productService.createProduct(any(ProductDto.class)))
                    .thenThrow(
                            new ValidationException(
                                    "Validation failed for field 'name': Product name is required"));

            mockMvc
                    .perform(
                            post("/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testProduct)))
                    .andExpect(status().isBadRequest());

            verify(productService).createProduct(any(ProductDto.class));
        }

        @Test
        void createProduct_DuplicateName() throws Exception {
            when(productService.createProduct(any(ProductDto.class)))
                    .thenThrow(
                            new ValidationException(
                                    "Validation failed for field 'name': Product with this name already exists"));

            mockMvc
                    .perform(
                            post("/v1/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testProduct)))
                    .andExpect(status().isBadRequest());

            verify(productService).createProduct(any(ProductDto.class));
        }

        @Test
        void getProductById_Success() throws Exception {
            when(productService.getProductById(1L)).thenReturn(testProduct);

            mockMvc
                    .perform(get("/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("Test Product 1"));

            verify(productService).getProductById(1L);
        }

        @Test
        void getProductById_NotFound() throws Exception {
            when(productService.getProductById(999L))
                    .thenThrow(new EntityNotFoundException("Product", 999L));

            mockMvc.perform(get("/v1/products/999")).andExpect(status().isNotFound());

            verify(productService).getProductById(999L);
        }

        @Test
        void updateProduct_Success() throws Exception {
            ProductDto updatedProduct = new ProductDto();
            updatedProduct.setId(1L);
            updatedProduct.setName("Updated Product");
            updatedProduct.setCategoryId(1L); // Required field!
            updatedProduct.setIsActive(true);

            when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(updatedProduct);

            mockMvc
                    .perform(
                            put("/v1/products/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(updatedProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Product updated successfully"))
                    .andExpect(jsonPath("$.data.name").value("Updated Product"));

            verify(productService).updateProduct(eq(1L), any(ProductDto.class));
        }

        @Test
        void updateProduct_NotFound() throws Exception {
            when(productService.updateProduct(eq(999L), any(ProductDto.class)))
                    .thenThrow(new EntityNotFoundException("Product", 999L));

            mockMvc
                    .perform(
                            put("/v1/products/999")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testProduct)))
                    .andExpect(status().isNotFound());

            verify(productService).updateProduct(eq(999L), any(ProductDto.class));
        }

        @Test
        void updateProduct_DuplicateName() throws Exception {
            when(productService.updateProduct(eq(1L), any(ProductDto.class)))
                    .thenThrow(
                            new ValidationException(
                                    "Validation failed for field 'name': Product with this name already exists"));

            mockMvc
                    .perform(
                            put("/v1/products/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(testProduct)))
                    .andExpect(status().isBadRequest());

            verify(productService).updateProduct(eq(1L), any(ProductDto.class));
        }

        @Test
        void deleteProduct_Success() throws Exception {
            doNothing().when(productService).deleteProduct(1L);

            mockMvc
                    .perform(delete("/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Product deleted successfully"));

            verify(productService).deleteProduct(1L);
        }

        @Test
        void deleteProduct_NotFound() throws Exception {
            doThrow(new EntityNotFoundException("Product", 999L))
                    .when(productService)
                    .deleteProduct(999L);

            mockMvc.perform(delete("/v1/products/999")).andExpect(status().isNotFound());

            verify(productService).deleteProduct(999L);
        }

        @Test
        void deleteProduct_WithActiveSkus() throws Exception {
            doThrow(new BusinessException("Cannot delete product with active SKUs"))
                    .when(productService)
                    .deleteProduct(1L);

            mockMvc
                    .perform(delete("/v1/products/1"))
                    .andExpect(status().isUnprocessableEntity()); // BusinessException returns 422

            verify(productService).deleteProduct(1L);
        }
    }

    @Nested
    @DisplayName("Search and Filtering")
    class SearchAndFiltering {

        @Test
        void getAllProducts_Success() throws Exception {
            Page<ProductDto> productsPage = new PageImpl<>(Arrays.asList(testProduct, testProduct2));
            when(productService.getAllProducts(any(Pageable.class))).thenReturn(productsPage);

            mockMvc
                    .perform(get("/v1/products").param("page", "0").param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2));

            verify(productService).getAllProducts(any(Pageable.class));
        }

        @Test
        void getAllProducts_WithFilters() throws Exception {
            Page<ProductDto> productsPage = new PageImpl<>(Collections.singletonList(testProduct));
            when(productService.searchProducts(
                    any(), // Allow null or any String
                    any(), // Allow null or any Long
                    any(), // Allow null or any String
                    any(), // Allow null or any String
                    any(), // Allow null or any BigDecimal
                    any(), // Allow null or any BigDecimal
                    any(), // Allow null or any Boolean
                    any(Pageable.class)))
                    .thenReturn(productsPage);

            mockMvc
                    .perform(
                            get("/v1/products")
                                    .param("name", "Test")
                                    .param("categoryId", "1")
                                    .param("brand", "Test Brand")
                                    .param("isActive", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            verify(productService)
                    .searchProducts(
                            any(), // Allow null or any String
                            any(), // Allow null or any Long
                            any(), // Allow null or any String
                            any(), // Allow null or any String
                            any(), // Allow null or any BigDecimal
                            any(), // Allow null or any BigDecimal
                            any(), // Allow null or any Boolean
                            any(Pageable.class));
        }

        @Test
        void searchProducts_Success() throws Exception {
            Page<ProductDto> productsPage = new PageImpl<>(Collections.singletonList(testProduct));
            when(productService.searchProductsByNameOrDescription(eq("Test"), any(Pageable.class)))
                    .thenReturn(productsPage);

            mockMvc
                    .perform(get("/v1/products/search").param("query", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Search completed successfully"));

            verify(productService).searchProductsByNameOrDescription(eq("Test"), any(Pageable.class));
        }

        @Test
        void getProductsByCategory_Success() throws Exception {
            Page<ProductDto> productsPage = new PageImpl<>(Collections.singletonList(testProduct));
            when(productService.getProductsByCategory(eq(1L), any(Pageable.class)))
                    .thenReturn(productsPage);

            mockMvc
                    .perform(get("/v1/products/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            verify(productService).getProductsByCategory(eq(1L), any(Pageable.class));
        }

        @Test
        void getProductsByCategory_NotFound() throws Exception {
            when(productService.getProductsByCategory(eq(999L), any(Pageable.class)))
                    .thenThrow(new EntityNotFoundException("Category", 999L));

            mockMvc.perform(get("/v1/products/category/999")).andExpect(status().isNotFound());

            verify(productService).getProductsByCategory(eq(999L), any(Pageable.class));
        }

        @Test
        void getProductsByBrand_Success() throws Exception {
            Page<ProductDto> productsPage = new PageImpl<>(Collections.singletonList(testProduct));
            when(productService.getProductsByBrand(eq("Test Brand"), any(Pageable.class)))
                    .thenReturn(productsPage);

            mockMvc
                    .perform(get("/v1/products/brand/Test Brand"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            verify(productService).getProductsByBrand(eq("Test Brand"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Bulk Operations")
    class BulkOperations {

        @Test
        void bulkCreateProducts_Success() throws Exception {
            List<ProductDto> products = Arrays.asList(testProduct, testProduct2);
            BulkOperationRequest<ProductDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(products);

            when(productService.createProduct(any(ProductDto.class)))
                    .thenReturn(testProduct)
                    .thenReturn(testProduct2);

            mockMvc
                    .perform(
                            post("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService, times(2)).createProduct(any(ProductDto.class));
        }

        @Test
        void bulkCreateProducts_PartialFailure() throws Exception {
            List<ProductDto> products = Arrays.asList(testProduct, testProduct2);
            BulkOperationRequest<ProductDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(products);

            when(productService.createProduct(any(ProductDto.class)))
                    .thenReturn(testProduct)
                    .thenThrow(
                            new ValidationException(
                                    "Validation failed for field 'name': Product name is required"));

            mockMvc
                    .perform(
                            post("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService, times(2)).createProduct(any(ProductDto.class));
        }

        @Test
        void bulkUpdateProducts_Success() throws Exception {
            List<ProductDto> products = Arrays.asList(testProduct, testProduct2);
            BulkOperationRequest<ProductDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(products);

            when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(testProduct);
            when(productService.updateProduct(eq(2L), any(ProductDto.class))).thenReturn(testProduct2);

            mockMvc
                    .perform(
                            put("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService).updateProduct(eq(1L), any(ProductDto.class));
            verify(productService).updateProduct(eq(2L), any(ProductDto.class));
        }

        @Test
        void bulkUpdateProducts_MissingId() throws Exception {
            ProductDto productWithoutId = new ProductDto();
            productWithoutId.setName("Product Without ID");
            productWithoutId.setCategoryId(1L); // Required field!
            productWithoutId.setIsActive(true);
            List<ProductDto> products = List.of(productWithoutId);
            BulkOperationRequest<ProductDto> bulkRequest = new BulkOperationRequest<>();
            bulkRequest.setItems(products);

            mockMvc
                    .perform(
                            put("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(bulkRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService, never()).updateProduct(any(), any());
        }

        @Test
        void bulkDeleteProducts_Success() throws Exception {
            List<Long> ids = Arrays.asList(1L, 2L);

            doNothing().when(productService).deleteProduct(1L);
            doNothing().when(productService).deleteProduct(2L);

            mockMvc
                    .perform(
                            delete("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(ids)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService).deleteProduct(1L);
            verify(productService).deleteProduct(2L);
        }

        @Test
        void bulkDeleteProducts_PartialFailure() throws Exception {
            List<Long> ids = Arrays.asList(1L, 2L);

            doNothing().when(productService).deleteProduct(1L);
            doThrow(new BusinessException("Cannot delete product with active SKUs"))
                    .when(productService)
                    .deleteProduct(2L);

            mockMvc
                    .perform(
                            delete("/v1/products/bulk")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(ids)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bulk operation completed"));

            verify(productService).deleteProduct(1L);
            verify(productService).deleteProduct(2L);
        }
    }
}
