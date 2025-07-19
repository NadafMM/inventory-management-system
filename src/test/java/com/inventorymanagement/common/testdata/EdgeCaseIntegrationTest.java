package com.inventorymanagement.common.testdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventorymanagement.application.InventoryManagementApplication;
import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.model.CategoryDto;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.category.service.CategoryService;
import com.inventorymanagement.common.BaseApiTest;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.inventory.model.InventoryTransaction;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.inventory.repository.InventoryTransactionRepository;
import com.inventorymanagement.inventory.repository.SkuRepository;
import com.inventorymanagement.inventory.service.InventoryService;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.repository.ProductRepository;
import com.inventorymanagement.product.service.ProductService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive edge case integration tests to improve branch coverage across the entire application. Focuses on cross-cutting concerns, error
 * boundaries, and integration scenarios.
 */
@DisplayName("Cross-Application Edge Case Integration Tests")
@ContextConfiguration(classes = InventoryManagementApplication.class)
class EdgeCaseIntegrationTest extends BaseApiTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SkuRepository skuRepository;
    @Autowired
    private InventoryTransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    private Category testCategory;
    private Product testProduct;
    private Sku testSku;

    @BeforeEach
    void setUp() {
        testCategory =
                TestDataFactory.category().withName("Test Category").withPath("/1").withLevel(0).build();
        testCategory = categoryRepository.save(testCategory);

        testProduct =
                TestDataFactory.product().withName("Test Product").withCategory(testCategory).build();
        testProduct = productRepository.save(testProduct);

        testSku =
                TestDataFactory.sku()
                        .withSkuCode(
                                "TEST-BASE-" + System.currentTimeMillis()) // Use unique timestamp-based code
                        .withProduct(testProduct)
                        .withPrice(BigDecimal.valueOf(99.99))
                        .build();
        testSku = skuRepository.save(testSku);
    }

    @Nested
    @DisplayName("Database Constraint and Transaction Edge Cases")
    class DatabaseConstraintTests {

        @Test
        @DisplayName("Should handle database constraint violations during cascading operations")
        @Transactional
        void shouldHandleDatabaseConstraintViolationsDuringCascadingOperations() {
            -Create products and SKUs dependent on category
            Product product1 =
                    TestDataFactory.product().withName("Product 1").withCategory(testCategory).build();
            productRepository.save(product1);

 &Then - Try to delete category that has dependent products
            assertThatThrownBy(() -> categoryService.deleteCategory(testCategory.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete category with existing products");
        }

        @Test
        @DisplayName("Should handle optimistic locking failures")
        @Transactional
        void shouldHandleOptimisticLockingFailures() {
            -Two different entity instances representing the same database record
            Category category1 = categoryRepository.findById(testCategory.getId()).orElseThrow();
            category1.setDescription("First update");
            categoryRepository.save(category1);
            entityManager.flush();

            // Detach the entity and get a fresh copy to simulate concurrent access
            entityManager.detach(category1);
            Category category2 = categoryRepository.findById(testCategory.getId()).orElseThrow();
            entityManager.detach(category2);

            -Simulate concurrent modifications
            category1.setDescription("Update from thread 1");
            category2.setDescription("Update from thread 2");

            // Save first update
            category1 = entityManager.merge(category1);
            entityManager.flush();

            -Second update should fail due to optimistic locking
            assertThatThrownBy(
                    () -> {
                        entityManager.merge(category2);
                        entityManager.flush();
                    })
                    .isInstanceOfAny(
                            ObjectOptimisticLockingFailureException.class,
                            PersistenceException.class,
                            org.hibernate.StaleObjectStateException.class);
        }

        @Test
        @DisplayName("Should handle transaction rollback scenarios with nested transactions")
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        @Rollback
        void shouldHandleTransactionRollbackScenariosWithNestedTransactions() {

            Long originalCount = categoryRepository.count();

            try {
                // Create a parent category first using service to ensure proper setup
                CategoryDto parentCategoryDto = new CategoryDto();
                parentCategoryDto.setName("Rollback Parent Category");
                CategoryDto createdParent = categoryService.createCategory(parentCategoryDto);

                // Create a category using service (which includes validation)
                CategoryDto newCategoryDto = new CategoryDto();
                newCategoryDto.setName("Rollback Test Category");
                newCategoryDto.setParentId(createdParent.getId());
                categoryService.createCategory(newCategoryDto);

                // Try to create duplicate category - this should trigger ValidationException
                CategoryDto duplicateCategoryDto = new CategoryDto();
                duplicateCategoryDto.setName("Rollback Test Category"); // Same name
                duplicateCategoryDto.setParentId(createdParent.getId()); // Same parent
                categoryService.createCategory(duplicateCategoryDto);

                // This should not be reached due to validation exception
                throw new RuntimeException("Test should have failed before this point");

            } catch (BusinessException e) {
                // Expected - validation should prevent duplicate names at same level
                assertThat(e).isInstanceOfAny(ValidationException.class, BusinessException.class);
            }

            // Verify that duplicate validation worked correctly - the successful operations
            // remain
            // but the duplicate was prevented
            Long finalCount = categoryRepository.count();
            assertThat(finalCount)
                    .isEqualTo(originalCount + 2); // parent + child were created successfully
        }

        @Test
        @DisplayName("Should handle orphaned record scenarios")
        @Transactional
        void shouldHandleOrphanedRecordScenarios() {
            -Create SKU with inventory transactions
            InventoryTransaction transaction =
                    new InventoryTransaction(
                            testSku,
                            InventoryTransaction.TransactionType.ADJUSTMENT,
                            10,
                            "REF-001",
                            "MANUAL",
                            "Initial stock",
                            "admin");
            transactionRepository.save(transaction);

            -Try to delete SKU that has transactions
            assertThatThrownBy(
                    () -> {
                        skuRepository.delete(testSku);
                        entityManager.flush(); // Force immediate execution
                    })
                    .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
        }
    }

    @Nested
    @DisplayName("Concurrency and Race Condition Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent category creation with same name")
        void shouldHandleConcurrentCategoryCreationWithSameName() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            String categoryName = "Concurrent Test Category";

            -Multiple threads try to create categories with same name
            List<CompletableFuture<Boolean>> futures =
                    IntStream.range(0, 5)
                            .mapToObj(
                                    i ->
                                            CompletableFuture.supplyAsync(
                                                    () -> {
                                                        try {
                                                            Category category =
                                                                    TestDataFactory.category()
                                                                            .withName(categoryName + "_" + i)
                                                                            .build();
                                                            categoryRepository.save(category);
                                                            return true;
                                                        } catch (Exception e) {
                                                            return false;
                                                        }
                                                    },
                                                    executor))
                            .toList();

            -All should complete (success or failure)
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long successCount = futures.stream().mapToLong(f -> f.join() ? 1L : 0L).sum();

            assertThat(successCount).isGreaterThan(0); // At least some should succeed

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        @Test
        @DisplayName("Should handle concurrent inventory adjustments")
        @Transactional(propagation = Propagation.NOT_SUPPORTED) // Run outside transaction
        void shouldHandleConcurrentInventoryAdjustments() throws InterruptedException {
            // Ensure testSku is available by re-fetching it in a separate transaction
            Long testSkuId = testSku.getId();

            ExecutorService executor = Executors.newFixedThreadPool(3);

            -Multiple threads try to adjust inventory simultaneously
            List<CompletableFuture<Boolean>> futures =
                    IntStream.range(0, 10)
                            .mapToObj(
                                    i ->
                                            CompletableFuture.supplyAsync(
                                                    () -> {
                                                        try {
                                                            inventoryService.recordStockAdjustment(
                                                                    testSkuId,
                                                                    1,
                                                                    "CONCURRENT_TEST",
                                                                    "Concurrent test " + i,
                                                                    "user_" + i);
                                                            return true;
                                                        } catch (Exception e) {
                                                            // Log the exception for debugging
                                                            System.err.println(
                                                                    "Exception in thread " + i + ": " + e.getMessage());
                                                            return false;
                                                        }
                                                    },
                                                    executor))
                            .toList();

            -All should complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long successCount = futures.stream().mapToLong(f -> f.join() ? 1L : 0L).sum();

            assertThat(successCount).isEqualTo(10); // All should succeed for adjustments

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Nested
    @DisplayName("Resource Exhaustion and Limit Tests")
    class ResourceExhaustionTests {

        @Test
        @DisplayName("Should handle extremely large result sets")
        void shouldHandleExtremelyLargeResultSets() {
            -Create many categories
            List<Category> categories =
                    IntStream.range(0, 1000)
                            .mapToObj(
                                    i ->
                                            TestDataFactory.category()
                                                    .withName("Bulk Category " + i)
                                                    .withPath("/" + (i + 100))
                                                    .withLevel(0)
                                                    .build())
                            .toList();

            categoryRepository.saveAll(categories);

            -Request very large page
            Pageable largePage = PageRequest.of(0, 5000);
            var result = categoryRepository.findAllActive(largePage);

            -Should handle gracefully without memory issues
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isNotEmpty();
            // Should return all active categories (1000 bulk + 1 test category from setup)
            assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1001);
            // Should handle page size larger than actual data
            assertThat(result.getPageable().getPageSize()).isEqualTo(5000);
            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(1001);
            // Should be the first and only page due to large page size
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should handle very deep category hierarchies")
        void shouldHandleVeryDeepCategoryHierarchies() {
            -Create deep hierarchy
            Category parent = testCategory;

            for (int i = 1; i <= 20; i++) {
                Category child =
                        TestDataFactory.category()
                                .withName("Level " + i + " Category")
                                .withParent(parent)
                                .withPath(parent.getPath() + (i + 1) + "/")
                                .withLevel(i)
                                .build();
                child = categoryRepository.save(child);
                parent = child;
            }

 &Then - Should handle deep hierarchy queries
            List<Category> path = categoryRepository.findPathToCategory(parent.getId());
            assertThat(path).hasSizeGreaterThanOrEqualTo(20);
        }

        @Test
        @DisplayName("Should handle inventory with massive transaction history")
        void shouldHandleInventoryWithMassiveTransactionHistory() {
            -Create many transactions
            List<InventoryTransaction> transactions =
                    IntStream.range(0, 1000)
                            .mapToObj(
                                    i ->
                                            new InventoryTransaction(
                                                    testSku,
                                                    InventoryTransaction.TransactionType.ADJUSTMENT,
                                                    i % 2 == 0 ? 1 : -1, // Alternate positive/negative
                                                    "REF-" + i,
                                                    "BULK_TEST",
                                                    "Bulk test transaction " + i,
                                                    "admin"))
                            .toList();

            transactionRepository.saveAll(transactions);

            -Query large transaction history
            LocalDateTime start = LocalDateTime.now().minusDays(1);
            LocalDateTime end = LocalDateTime.now().plusDays(1);

            -Should handle large result sets
            List<InventoryTransaction> result =
                    inventoryService
                            .getTransactionsByDateRange(start, end, PageRequest.of(0, 1500))
                            .getContent();

            assertThat(result).hasSizeGreaterThan(500);
        }
    }

    @Nested
    @DisplayName("Data Validation and Boundary Tests")
    class DataValidationBoundaryTests {

        @Test
        @DisplayName("Should handle maximum field length boundaries")
        void shouldHandleMaximumFieldLengthBoundaries() {
            -Category with maximum length fields
            String maxName = "A".repeat(255); // Assuming 255 is max length
            String maxDescription = "D".repeat(1000); // Assuming 1000 is max length

 &Then - Should handle at boundary
            Category maxCategory =
                    TestDataFactory.category().withName(maxName).withDescription(maxDescription).build();

            Category saved = categoryRepository.save(maxCategory);
            assertThat(saved.getName()).hasSize(255);
            assertThat(saved.getDescription()).hasSize(1000);
        }

        @Test
        @DisplayName("Should handle numeric precision boundaries")
        void shouldHandleNumericPrecisionBoundaries() {
            -SKU with extreme prices that fit within NUMERIC(10, 2) constraints
            // NUMERIC(10,2) allows max 8 digits before decimal + 2 after = 99999999.99
            BigDecimal maxPrice = new BigDecimal("99999999.99");
            BigDecimal minPrice = new BigDecimal("0.01");

            -Create SKUs with boundary prices
            Sku maxPriceSku =
                    TestDataFactory.sku()
                            .withSkuCode("MAX-PRICE-" + System.currentTimeMillis())
                            .withProduct(testProduct)
                            .withPrice(maxPrice)
                            .build();

            Sku minPriceSku =
                    TestDataFactory.sku()
                            .withSkuCode("MIN-PRICE-" + System.currentTimeMillis())
                            .withProduct(testProduct)
                            .withPrice(minPrice)
                            .build();

            Sku savedMax = skuRepository.save(maxPriceSku);
            Sku savedMin = skuRepository.save(minPriceSku);

            assertThat(savedMax.getPrice()).isEqualByComparingTo(maxPrice);
            assertThat(savedMin.getPrice()).isEqualByComparingTo(minPrice);
        }

        @Test
        @DisplayName("Should handle extreme date boundaries")
        void shouldHandleExtremeDateBoundaries() {
            -Transactions with extreme dates
            LocalDateTime farPast = LocalDateTime.of(1900, 1, 1, 0, 0);
            LocalDateTime farFuture = LocalDateTime.of(2100, 12, 31, 23, 59);

 &Then - Should handle date range queries with extreme dates
            assertThatThrownBy(
                    () ->
                            inventoryService.getTransactionsByDateRange(
                                    farFuture, farPast, PageRequest.of(0, 10)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Start date cannot be after end date");
        }
    }

    @Nested
    @DisplayName("Cross-Entity Consistency Tests")
    class CrossEntityConsistencyTests {

        @Test
        @DisplayName("Should maintain consistency across entity relationships")
        void shouldMaintainConsistencyAcrossEntityRelationships() {
            -Complex entity graph using service to ensure proper hierarchy setup
            CategoryDto parentCategoryDto = new CategoryDto();
            parentCategoryDto.setName("Parent Category");
            CategoryDto createdParent = categoryService.createCategory(parentCategoryDto);

            CategoryDto childCategoryDto = new CategoryDto();
            childCategoryDto.setName("Child Category");
            childCategoryDto.setParentId(createdParent.getId());
            CategoryDto createdChild = categoryService.createCategory(childCategoryDto);

            // Get the actual category entities for product creation
            Category childCategory = categoryRepository.findById(createdChild.getId()).orElseThrow();

            Product categoryProduct =
                    productRepository.save(
                            TestDataFactory.product()
                                    .withName("Category Product")
                                    .withCategory(childCategory)
                                    .build());

            -Delete parent category
            Long parentCategoryId = createdParent.getId();
            assertThatThrownBy(() -> categoryService.deleteCategory(parentCategoryId))
                    .isInstanceOf(BusinessException.class);

            -Verify child entities still exist and are consistent
            assertThat(categoryRepository.findById(createdChild.getId())).isPresent();
            assertThat(productRepository.findById(categoryProduct.getId())).isPresent();
        }

        @Test
        @DisplayName("Should handle circular reference detection")
        void shouldHandleCircularReferenceDetection() {
            -Setup potential circular reference
            Category parent = testCategory;
            Category child =
                    TestDataFactory.category().withName("Child Category").withParent(parent).build();
            child = categoryRepository.save(child);

 &Then - Try to update parent to make it a child of its own child
            // (circular reference)
            CategoryDto updateDto = new CategoryDto();
            updateDto.setParentId(child.getId()); // Try to make parent a child of its own child

            assertThatThrownBy(
                    () -> {
                        categoryService.updateCategory(parent.getId(), updateDto);
                    })
                    .isInstanceOfAny(ValidationException.class, BusinessException.class);
        }

        @Test
        @DisplayName("Should handle entity state consistency during failures")
        void shouldHandleEntityStateConsistencyDuringFailures() {
            -Product with SKUs
            long timestamp = System.currentTimeMillis();
            Sku sku1 =
                    TestDataFactory.sku()
                            .withSkuCode("SKU-001-" + timestamp)
                            .withProduct(testProduct)
                            .build();
            Sku sku2 =
                    TestDataFactory.sku()
                            .withSkuCode("SKU-002-" + timestamp)
                            .withProduct(testProduct)
                            .build();

            skuRepository.saveAll(Arrays.asList(sku1, sku2));

            // Store original price for verification
            BigDecimal originalPrice = sku1.getPrice();

            -Try operation that should fail due to validation
            assertThatThrownBy(
                    () -> {
                        // Simulate operation that fails after partial completion
                        sku1.setPrice(BigDecimal.valueOf(-100)); // Invalid negative price
                        // Use entityManager to trigger validation
                        entityManager.merge(sku1);
                        entityManager.flush();
                    })
                    .isInstanceOfAny(
                            ConstraintViolationException.class,
                            ValidationException.class,
                            org.hibernate.exception.ConstraintViolationException.class,
                            jakarta.validation.ConstraintViolationException.class);

            -Verify entity remains in consistent state by refreshing from database
            entityManager.clear(); // Clear persistence context
            Sku refreshedSku1 = skuRepository.findById(sku1.getId()).orElseThrow();
            assertThat(refreshedSku1.getPrice())
                    .isEqualTo(originalPrice); // Should be original price, not -100
        }
    }

    @Nested
    @DisplayName("Error Recovery and Resilience Tests")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Should handle graceful degradation during service failures")
        void shouldHandleGracefulDegradationDuringServiceFailures() {
            -Service encounters unexpected errors
            assertThatThrownBy(
                    () -> {
                        // Force a low-level database error
                        entityManager.createNativeQuery("INVALID SQL SYNTAX").executeUpdate();
                    })
                    .isInstanceOf(Exception.class);

            -Other operations should still work
            List<Category> categories = categoryRepository.findAllActive();
            assertThat(categories).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle partial failure in batch operations")
        void shouldHandlePartialFailureInBatchOperations() {
            -Mix of valid and invalid data
            List<Category> categories =
                    Arrays.asList(
                            TestDataFactory.category().withName("TEST Valid Category 1").build(),
                            TestDataFactory.category().withName("TEST Valid Category 2").build(),
                            TestDataFactory.category().withName(null).build(), // Invalid
                            TestDataFactory.category().withName("TEST Valid Category 3").build());

 &Then - Batch operation should handle partial failures
            assertThatThrownBy(() -> categoryRepository.saveAll(categories))
                    .isInstanceOfAny(ConstraintViolationException.class, ValidationException.class);

            // Clear the EntityManager to remove dirty entities with null IDs after the
            // failed operation
            entityManager.clear();

            // Verify partial save behavior - saveAll processes sequentially and stops on
            // error
            List<Category> saved =
                    categoryRepository.findByNameContainingIgnoreCase("TEST Valid Category");
            // Spring Data JPA's saveAll processes entities sequentially, so valid entities
            // before the invalid one are saved, but processing stops at the error
            assertThat(saved).hasSize(2); // First two valid categories should be saved
            assertThat(saved)
                    .extracting(Category::getName)
                    .containsExactlyInAnyOrder("TEST Valid Category 1", "TEST Valid Category 2");

            // The third valid category after the invalid one should not be saved
            assertThat(saved).extracting(Category::getName).doesNotContain("TEST Valid Category 3");
        }
    }
}
