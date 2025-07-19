package com.inventorymanagement.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.InsufficientStockException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.testdata.TestDataFactory;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.inventory.model.SkuDto;
import com.inventorymanagement.inventory.repository.SkuRepository;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive integration tests for SkuService to improve coverage
 */
@SpringBootTest(classes = com.inventorymanagement.application.InventoryManagementApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("SkuService Integration Tests")
class SkuServiceIntegrationTest {

    @Autowired private SkuService skuService;

    @Autowired private SkuRepository skuRepository;

    @Autowired private ProductRepository productRepository;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private InventoryService inventoryService;

    private Category testCategory;
    private Product testProduct;
    private SkuDto testSkuDto;

    @BeforeEach
    void setUp() {
        // Clean up any existing data in proper order (due to foreign key constraints)
        skuRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category first
        testCategory =
                TestDataFactory.category()
                        .withName("Test Category")
                        .withDescription("Test category for SKU tests")
                        .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product with valid category
        testProduct =
                TestDataFactory.product().withName("Test Product").withCategory(testCategory).build();
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
    @DisplayName("SKU Creation Tests")
    class SkuCreationTests {

        @Test
        @DisplayName("Should create SKU with all fields")
        void createSkuWithAllFields() {
            SkuDto result = skuService.createSku(testSkuDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull();
            assertThat(result.getSkuCode()).isEqualTo("TEST-SKU-001");
            assertThat(result.getProductId()).isEqualTo(testProduct.getId());
            assertThat(result.getVariantName()).isEqualTo("Test Variant");
            assertThat(result.getSize()).isEqualTo("M");
            assertThat(result.getColor()).isEqualTo("Blue");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("99.99"));
            assertThat(result.getCost()).isEqualTo(new BigDecimal("49.99"));
            assertThat(result.getStockQuantity()).isEqualTo(100);
            assertThat(result.getReorderPoint()).isEqualTo(20);
            assertThat(result.getReorderQuantity()).isEqualTo(50);
            assertThat(result.getBarcode()).isEqualTo("123456789");
            assertThat(result.getLocation()).isEqualTo("A1-B2");
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should auto-generate SKU code when not provided")
        void createSkuWithAutoGeneratedCode() {
            testSkuDto.setSkuCode(null);

            SkuDto result = skuService.createSku(testSkuDto);

            assertThat(result.getSkuCode()).isNotNull();
            assertThat(result.getSkuCode()).startsWith("SKU");
        }

        @Test
        @DisplayName("Should create initial stock transaction when stock quantity provided")
        void createSkuWithInitialStock() {
            testSkuDto.setStockQuantity(50);

            SkuDto result = skuService.createSku(testSkuDto);

            assertThat(result.getStockQuantity()).isEqualTo(50);
            // Verify inventory transaction was created
            assertThat(result.getId()).isNotNull();
        }

        @Test
        @DisplayName("Should throw validation exception when product ID is null")
        void createSkuWithNullProductId() {
            testSkuDto.setProductId(null);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Product is required");
        }

        @Test
        @DisplayName("Should throw validation exception when price is null")
        void createSkuWithNullPrice() {
            testSkuDto.setPrice(null);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Price is required");
        }

        @Test
        @DisplayName("Should throw validation exception when price is negative")
        void createSkuWithNegativePrice() {
            testSkuDto.setPrice(new BigDecimal("-10.00"));

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Price is required and must be non-negative");
        }

        @Test
        @DisplayName("Should throw validation exception when SKU code already exists")
        void createSkuWithDuplicateSkuCode() {
            // Create first SKU
            skuService.createSku(testSkuDto);

            // Try to create second SKU with same code
            SkuDto duplicateSkuDto = new SkuDto();
            duplicateSkuDto.setProductId(testProduct.getId());
            duplicateSkuDto.setSkuCode("TEST-SKU-001");
            duplicateSkuDto.setPrice(new BigDecimal("50.00"));

            assertThatThrownBy(() -> skuService.createSku(duplicateSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("SKU code already exists");
        }

        @Test
        @DisplayName("Should throw entity not found exception when product doesn't exist")
        void createSkuWithNonExistentProduct() {
            testSkuDto.setProductId(99999L);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Product");
        }

        @Test
        @DisplayName("Should throw business exception when product is inactive")
        void createSkuWithInactiveProduct() {
            testProduct.setIsActive(false);
            productRepository.save(testProduct);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot create SKU for inactive product");
        }

        @Test
        @DisplayName("Should validate negative stock quantities")
        void createSkuWithNegativeStockQuantity() {
            testSkuDto.setStockQuantity(-10);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Stock quantity cannot be negative");
        }

        @Test
        @DisplayName("Should validate negative reorder point")
        void createSkuWithNegativeReorderPoint() {
            testSkuDto.setReorderPoint(-5);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reorder point cannot be negative");
        }

        @Test
        @DisplayName("Should validate negative reorder quantity")
        void createSkuWithNegativeReorderQuantity() {
            testSkuDto.setReorderQuantity(-5);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reorder quantity cannot be negative");
        }
    }

    @Nested
    @DisplayName("SKU Retrieval Tests")
    class SkuRetrievalTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should retrieve SKU by ID")
        void getSkuById() {
            SkuDto result = skuService.getSkuById(createdSku.getId());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(createdSku.getId());
            assertThat(result.getSkuCode()).isEqualTo(createdSku.getSkuCode());
        }

        @Test
        @DisplayName("Should throw entity not found when SKU ID doesn't exist")
        void getSkuByNonExistentId() {
            assertThatThrownBy(() -> skuService.getSkuById(99999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        @DisplayName("Should retrieve SKU by code")
        void getSkuByCode() {
            SkuDto result = skuService.getSkuByCode(createdSku.getSkuCode());

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(createdSku.getId());
            assertThat(result.getSkuCode()).isEqualTo(createdSku.getSkuCode());
        }

        @Test
        @DisplayName("Should throw entity not found when SKU code doesn't exist")
        void getSkuByNonExistentCode() {
            assertThatThrownBy(() -> skuService.getSkuByCode("NON-EXISTENT"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        @DisplayName("Should retrieve all SKUs with pagination")
        void getAllSkusWithPagination() {
            // Create additional SKUs
            for (int i = 0; i < 5; i++) {
                SkuDto skuDto = new SkuDto();
                skuDto.setProductId(testProduct.getId());
                skuDto.setSkuCode("TEST-SKU-" + (i + 2));
                skuDto.setPrice(new BigDecimal("50.00"));
                skuService.createSku(skuDto);
            }

            Pageable pageable = PageRequest.of(0, 3, Sort.by("skuCode"));
            Page<SkuDto> result = skuService.getAllSkus(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getTotalElements()).isEqualTo(6);
            assertThat(result.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should retrieve SKUs by product")
        void getSkusByProduct() {
            // Create additional product and SKU
            Product anotherProduct = TestDataFactory.product().withName("Another Product").build();
            anotherProduct = productRepository.save(anotherProduct);

            SkuDto anotherSkuDto = new SkuDto();
            anotherSkuDto.setProductId(anotherProduct.getId());
            anotherSkuDto.setSkuCode("ANOTHER-SKU");
            anotherSkuDto.setPrice(new BigDecimal("75.00"));
            skuService.createSku(anotherSkuDto);

            Pageable pageable = PageRequest.of(0, 10);
            Page<SkuDto> result = skuService.getSkusByProduct(testProduct.getId(), pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getProductId()).isEqualTo(testProduct.getId());
        }

        @Test
        @DisplayName("Should throw entity not found when getting SKUs for non-existent product")
        void getSkusByNonExistentProduct() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> skuService.getSkusByProduct(99999L, pageable))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Product");
        }

        @Test
        @DisplayName("Should retrieve active SKUs by product")
        void getActiveSkusByProduct() {
            List<SkuDto> result = skuService.getActiveSkusByProduct(testProduct.getId());

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("SKU Update Tests")
    class SkuUpdateTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should update SKU successfully")
        void updateSku() {
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(testProduct.getId());
            updateDto.setSkuCode(createdSku.getSkuCode());
            updateDto.setVariantName("Updated Variant");
            updateDto.setPrice(new BigDecimal("149.99"));
            updateDto.setCost(new BigDecimal("75.00"));
            updateDto.setReorderPoint(30);
            updateDto.setIsActive(true);

            SkuDto result = skuService.updateSku(createdSku.getId(), updateDto);

            assertThat(result.getVariantName()).isEqualTo("Updated Variant");
            assertThat(result.getPrice()).isEqualTo(new BigDecimal("149.99"));
            assertThat(result.getCost()).isEqualTo(new BigDecimal("75.00"));
            assertThat(result.getReorderPoint()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should throw entity not found when updating non-existent SKU")
        void updateNonExistentSku() {
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(testProduct.getId());
            updateDto.setPrice(new BigDecimal("100.00"));

            assertThatThrownBy(() -> skuService.updateSku(99999L, updateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        @DisplayName("Should throw validation exception when updating with duplicate SKU code")
        void updateSkuWithDuplicateCode() {
            // Create another SKU
            SkuDto anotherSkuDto = new SkuDto();
            anotherSkuDto.setProductId(testProduct.getId());
            anotherSkuDto.setSkuCode("ANOTHER-SKU");
            anotherSkuDto.setPrice(new BigDecimal("50.00"));
            SkuDto anotherSku = skuService.createSku(anotherSkuDto);

            // Try to update with existing code
            SkuDto updateDto = new SkuDto();
            updateDto.setProductId(testProduct.getId());
            updateDto.setSkuCode("ANOTHER-SKU");
            updateDto.setPrice(new BigDecimal("100.00"));

            assertThatThrownBy(() -> skuService.updateSku(createdSku.getId(), updateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("SKU code already exists");
        }
    }

    @Nested
    @DisplayName("SKU Deletion Tests")
    class SkuDeletionTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should delete SKU successfully")
        void deleteSku() {
            skuService.deleteSku(createdSku.getId());

            // Verify SKU is soft deleted
            Optional<Sku> sku = skuRepository.findById(createdSku.getId());
            assertThat(sku).isPresent();
            assertThat(sku.get().getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw entity not found when deleting non-existent SKU")
        void deleteNonExistentSku() {
            assertThatThrownBy(() -> skuService.deleteSku(99999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        @DisplayName("Should release reserved stock before deletion")
        void deleteSkuWithReservedStock() {
            // Reserve some stock
            skuService.reserveStock(
                    createdSku.getId(), 10, "ORDER-123", "ORDER", "Customer order", "SYSTEM");

            skuService.deleteSku(createdSku.getId());

            // Verify deletion completed successfully
            Optional<Sku> sku = skuRepository.findById(createdSku.getId());
            assertThat(sku).isPresent();
            assertThat(sku.get().getDeletedAt()).isNotNull();
            assertThat(sku.get().getReservedQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Stock Reservation Tests")
    class StockReservationTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            testSkuDto.setStockQuantity(100);
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should reserve stock successfully")
        void reserveStock() {
            skuService.reserveStock(
                    createdSku.getId(), 25, "ORDER-123", "ORDER", "Customer order", "SYSTEM");

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getReservedQuantity()).isEqualTo(25);
            assertThat(updatedSku.getAvailableQuantity()).isEqualTo(75);
        }

        @Test
        @DisplayName("Should throw validation exception for zero quantity reservation")
        void reserveZeroQuantity() {
            assertThatThrownBy(
                    () ->
                            skuService.reserveStock(
                                    createdSku.getId(), 0, "ORDER-123", "ORDER", "Test", "SYSTEM"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw validation exception for negative quantity reservation")
        void reserveNegativeQuantity() {
            assertThatThrownBy(
                    () ->
                            skuService.reserveStock(
                                    createdSku.getId(), -5, "ORDER-123", "ORDER", "Test", "SYSTEM"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw insufficient stock exception when reserving more than available")
        void reserveMoreThanAvailable() {
            assertThatThrownBy(
                    () ->
                            skuService.reserveStock(
                                    createdSku.getId(), 150, "ORDER-123", "ORDER", "Test", "SYSTEM"))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("Should throw business exception when reserving for inactive SKU")
        void reserveForInactiveSku() {
            skuService.deactivateSku(createdSku.getId());

            assertThatThrownBy(
                    () ->
                            skuService.reserveStock(
                                    createdSku.getId(), 10, "ORDER-123", "ORDER", "Test", "SYSTEM"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot reserve stock for inactive SKU");
        }
    }

    @Nested
    @DisplayName("Stock Release Tests")
    class StockReleaseTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            testSkuDto.setStockQuantity(100);
            createdSku = skuService.createSku(testSkuDto);
            // Reserve some stock
            skuService.reserveStock(
                    createdSku.getId(), 30, "ORDER-123", "ORDER", "Customer order", "SYSTEM");
        }

        @Test
        @DisplayName("Should release reserved stock successfully")
        void releaseReservedStock() {
            skuService.releaseStock(createdSku.getId(), 15);

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getReservedQuantity()).isEqualTo(15);
            assertThat(updatedSku.getAvailableQuantity()).isEqualTo(85);
        }

        @Test
        @DisplayName("Should throw validation exception for zero quantity release")
        void releaseZeroQuantity() {
            assertThatThrownBy(() -> skuService.releaseStock(createdSku.getId(), 0))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("Should throw validation exception for negative quantity release")
        void releaseNegativeQuantity() {
            assertThatThrownBy(() -> skuService.releaseStock(createdSku.getId(), -5))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Quantity must be positive");
        }
    }

    @Nested
    @DisplayName("Stock Adjustment Tests")
    class StockAdjustmentTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            testSkuDto.setStockQuantity(100);
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should adjust stock upward")
        void adjustStockUpward() {
            skuService.adjustStock(createdSku.getId(), 25, "Inventory count adjustment", "ADMIN");

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getStockQuantity()).isEqualTo(125);
        }

        @Test
        @DisplayName("Should adjust stock downward")
        void adjustStockDownward() {
            skuService.adjustStock(createdSku.getId(), -15, "Damaged inventory", "ADMIN");

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getStockQuantity()).isEqualTo(85);
        }

        @Test
        @DisplayName("Should throw validation exception for zero adjustment")
        void adjustStockByZero() {
            assertThatThrownBy(() -> skuService.adjustStock(createdSku.getId(), 0, "Test", "ADMIN"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Adjustment cannot be zero");
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @BeforeEach
        void setUp() {
            // Create multiple SKUs for search testing
            skuService.createSku(testSkuDto);

            SkuDto sku2 = new SkuDto();
            sku2.setProductId(testProduct.getId());
            sku2.setSkuCode("SEARCH-SKU-001");
            sku2.setVariantName("Search Variant");
            sku2.setPrice(new BigDecimal("75.00"));
            skuService.createSku(sku2);

            SkuDto sku3 = new SkuDto();
            sku3.setProductId(testProduct.getId());
            sku3.setSkuCode("ANOTHER-SKU-002");
            sku3.setVariantName("Another Variant");
            sku3.setPrice(new BigDecimal("125.00"));
            skuService.createSku(sku3);
        }

        @Test
        @DisplayName("Should search SKUs by code or variant")
        void searchSkusByCodeOrVariant() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SkuDto> result = skuService.searchSkusByCodeOrVariant("SEARCH", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getSkuCode()).contains("SEARCH");
        }

        @Test
        @DisplayName("Should throw validation exception for empty search query")
        void searchWithEmptyQuery() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(() -> skuService.searchSkusByCodeOrVariant("", pageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Search query cannot be empty");
        }

        @Test
        @DisplayName("Should search SKUs with extended filters")
        void searchSkusWithExtendedFilters() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SkuDto> result =
                    skuService.searchSkus(
                            null,
                            testProduct.getId(),
                            null,
                            new BigDecimal("70.00"),
                            new BigDecimal("100.00"),
                            true,
                            null,
                            pageable);

            assertThat(result.getContent()).hasSize(2); // Two SKUs in price range 70-100
        }

        @Test
        @DisplayName("Should get SKUs by price range")
        void getSkusByPriceRange() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SkuDto> result =
                    skuService.getSkusByPriceRange(
                            new BigDecimal("70.00"), new BigDecimal("100.00"), pageable);

            assertThat(result.getContent()).hasSizeGreaterThan(0);
            result
                    .getContent()
                    .forEach(
                            sku -> {
                                assertThat(sku.getPrice())
                                        .isBetween(new BigDecimal("70.00"), new BigDecimal("100.00"));
                            });
        }

        @Test
        @DisplayName("Should throw validation exception when min price exceeds max price")
        void getSkusByInvalidPriceRange() {
            Pageable pageable = PageRequest.of(0, 10);

            assertThatThrownBy(
                    () ->
                            skuService.getSkusByPriceRange(
                                    new BigDecimal("100.00"), new BigDecimal("50.00"), pageable))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Minimum price cannot be greater than maximum price");
        }

        @Test
        @DisplayName("Should get low stock SKUs")
        void getLowStockSkus() {
            // Adjust one SKU to low stock
            Sku sku = skuRepository.findAll().get(0);
            sku.setStockQuantity(5);
            sku.setReorderPoint(10);
            skuRepository.save(sku);

            Pageable pageable = PageRequest.of(0, 10);
            Page<SkuDto> result = skuService.getLowStockSkus(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStockQuantity())
                    .isLessThanOrEqualTo(result.getContent().get(0).getReorderPoint());
        }
    }

    @Nested
    @DisplayName("Activation and Deactivation Tests")
    class ActivationDeactivationTests {

        private SkuDto createdSku;

        @BeforeEach
        void setUp() {
            createdSku = skuService.createSku(testSkuDto);
        }

        @Test
        @DisplayName("Should activate SKU")
        void activateSku() {
            // First deactivate it
            skuService.deactivateSku(createdSku.getId());

            // Then activate it
            skuService.activateSku(createdSku.getId());

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate SKU")
        void deactivateSku() {
            skuService.deactivateSku(createdSku.getId());

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should release reserved stock when deactivating")
        void deactivateSkuWithReservedStock() {
            // Reserve some stock
            skuService.reserveStock(
                    createdSku.getId(), 20, "ORDER-123", "ORDER", "Customer order", "SYSTEM");

            skuService.deactivateSku(createdSku.getId());

            SkuDto updatedSku = skuService.getSkuById(createdSku.getId());
            assertThat(updatedSku.getIsActive()).isFalse();
            assertThat(updatedSku.getReservedQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should throw business exception when activating SKU for inactive product")
        void activateSkuForInactiveProduct() {
            // Deactivate product
            testProduct.setIsActive(false);
            productRepository.save(testProduct);

            // Deactivate SKU first
            skuService.deactivateSku(createdSku.getId());

            // Try to activate SKU
            assertThatThrownBy(() -> skuService.activateSku(createdSku.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot activate SKU for inactive product");
        }
    }

    @Nested
    @DisplayName("Business Rule Tests")
    class BusinessRuleTests {

        @Test
        @DisplayName("Should generate unique SKU codes for same product")
        void generateUniqueSkuCodes() {
            SkuDto sku1 = new SkuDto();
            sku1.setProductId(testProduct.getId());
            sku1.setPrice(new BigDecimal("50.00"));
            SkuDto result1 = skuService.createSku(sku1);

            SkuDto sku2 = new SkuDto();
            sku2.setProductId(testProduct.getId());
            sku2.setPrice(new BigDecimal("75.00"));
            SkuDto result2 = skuService.createSku(sku2);

            assertThat(result1.getSkuCode()).isNotEqualTo(result2.getSkuCode());
            assertThat(result1.getSkuCode()).startsWith("SKU");
            assertThat(result2.getSkuCode()).startsWith("SKU");
        }

        @Test
        @DisplayName("Should validate stock quantities on creation")
        void validateStockQuantitiesOnCreation() {
            testSkuDto.setReservedQuantity(150); // More than stock quantity

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reserved quantity cannot exceed stock quantity");
        }

        @Test
        @DisplayName("Should validate reorder point logic")
        void validateReorderPointLogic() {
            testSkuDto.setReorderPoint(200); // More than stock quantity
            testSkuDto.setStockQuantity(100);

            assertThatThrownBy(() -> skuService.createSku(testSkuDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Reorder point should not exceed stock quantity");
        }
    }
}
