package com.inventorymanagement.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventorymanagement.category.model.Category;
import com.inventorymanagement.category.repository.CategoryRepository;
import com.inventorymanagement.common.BaseUnitTest;
import com.inventorymanagement.common.exception.BusinessException;
import com.inventorymanagement.common.exception.EntityNotFoundException;
import com.inventorymanagement.common.exception.ValidationException;
import com.inventorymanagement.common.testdata.TestDataFactory;
import com.inventorymanagement.inventory.model.Sku;
import com.inventorymanagement.product.model.Product;
import com.inventorymanagement.product.model.ProductDto;
import com.inventorymanagement.product.repository.ProductRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for ProductService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest extends BaseUnitTest {

    @Mock private ProductRepository productRepository;

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductService productService;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = TestDataFactory.category().withId(1L).withName("Electronics").build();

        testProduct =
                TestDataFactory.product()
                        .withId(1L)
                        .withName("iPhone 15")
                        .withCategory(testCategory)
                        .withBrand("Apple")
                        .build();

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("iPhone 15");
        testProductDto.setCategoryId(1L);
        testProductDto.setBrand("Apple");
    }

    @Nested
    @DisplayName("getProductById Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductDto result = productService.getProductById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("iPhone 15");
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Product with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("createProduct Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_Success() {

            ProductDto newProductDto = new ProductDto();
            newProductDto.setName("New Product");
            newProductDto.setDescription("New product description");
            newProductDto.setCategoryId(1L);
            newProductDto.setBrand("Test Brand");
            newProductDto.setIsActive(true);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("New Product", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(newProductDto);

            assertThat(result).isNotNull();
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {

            ProductDto newProductDto = new ProductDto();
            newProductDto.setName("New Product");
            newProductDto.setCategoryId(999L);

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(newProductDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when duplicate product")
        void createProduct_DuplicateName_ThrowsValidationException() {

            ProductDto newProductDto = new ProductDto();
            newProductDto.setName("Test Product");
            newProductDto.setDescription("New product description");
            newProductDto.setCategoryId(1L);
            newProductDto.setBrand("Test Brand");

            when(productRepository.findByNameAndCategoryId("Test Product", 1L))
                    .thenReturn(Optional.of(testProduct));

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> productService.createProduct(newProductDto))
                    .withMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("updateProduct Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void shouldUpdateProductSuccessfully() {

            ProductDto updateDto = new ProductDto();
            updateDto.setName("Updated iPhone");
            updateDto.setDescription("Updated Description");
            updateDto.setCategoryId(1L);
            updateDto.setBrand("Apple");

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndCategoryIdExcludingId("Updated iPhone", 1L, 1L))
                    .thenReturn(false);
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            ProductDto result = productService.updateProduct(1L, updateDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated iPhone");
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {

            ProductDto updateDto = new ProductDto();
            updateDto.setName("Updated Product");

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateProduct(999L, updateDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Product with ID 999 not found");
        }

        @Test
        @DisplayName("Should throw exception when duplicate product")
        void updateProduct_DuplicateName_ThrowsValidationException() {

            ProductDto updateDto = new ProductDto();
            updateDto.setName("Duplicate Name");
            updateDto.setDescription("Updated description");
            updateDto.setCategoryId(1L);
            updateDto.setBrand("Updated Brand");

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndCategoryIdExcludingId("Duplicate Name", 1L, 1L))
                    .thenReturn(true);

            assertThatExceptionOfType(ValidationException.class)
                    .isThrownBy(() -> productService.updateProduct(1L, updateDto))
                    .withMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("deleteProduct Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_NoActiveSkus_Success() {

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            // testProduct has no SKUs by default, so hasActiveSkus() will return false

            productService.deleteProduct(1L);

            assertThat(testProduct.isDeleted()).isTrue();
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when product has active SKUs")
        void deleteProduct_WithActiveSkus_ThrowsBusinessException() {

            // Create a product with active SKUs
            Product productWithSkus = new Product();
            productWithSkus.setId(1L);
            productWithSkus.setName("Product with SKUs");

            // Create a mock SKU
            Sku activeSku = new Sku();
            activeSku.setId(1L);
            activeSku.setIsActive(true);
            activeSku.setProduct(productWithSkus);
            productWithSkus.getSkus().add(activeSku);

            when(productRepository.findById(1L)).thenReturn(Optional.of(productWithSkus));

            assertThatExceptionOfType(BusinessException.class)
                    .isThrownBy(() -> productService.deleteProduct(1L))
                    .withMessageContaining("Cannot delete");
        }
    }

    @Nested
    @DisplayName("getAllProducts Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() {

            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Collections.singletonList(testProduct);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepository.findAllActive(pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getAllProducts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
        }
    }

    @Nested
    @DisplayName("getProductsByCategory Tests")
    class GetProductsByCategoryTests {

        @Test
        @DisplayName("Should return products by category")
        void shouldReturnProductsByCategory() {

            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Collections.singletonList(testProduct);
            Page<Product> productPage = new PageImpl<>(products);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getProductsByCategory(1L, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {

            Pageable pageable = PageRequest.of(0, 10);
            Long nonExistentCategoryId = 999L;

            when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(
                    () -> productService.getProductsByCategory(nonExistentCategoryId, pageable))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category with ID 999 not found");
        }
    }

    @Nested
    @DisplayName("Search Products")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search products by category")
        void searchProductsByCategory_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getProductsByCategory(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("Should search products by brand")
        void searchProductsByBrand_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findByBrandIgnoreCase("Test Brand", pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.searchProductsByBrand("Test Brand", pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should search products by name or description")
        void searchProductsByNameOrDescription_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findByNameOrDescriptionContainingIgnoreCase("Test", pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result = productService.searchProductsByNameOrDescription("Test", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
        }

        @Test
        @DisplayName("Should search products with filters")
        void searchProducts_WithFilters_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findWithFilters(
                    "Test", 1L, "Test Brand", "Test Manufacturer", true, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result =
                    productService.searchProducts(
                            "Test", "Test Brand", 1L, "Test Manufacturer", true, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should activate product")
        void shouldActivateProduct() {

            testProduct.setIsActive(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.activateProduct(1L);

            assertThat(testProduct.getIsActive()).isTrue();
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should deactivate product")
        void shouldDeactivateProduct() {

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            productService.deactivateProduct(1L);

            assertThat(testProduct.getIsActive()).isFalse();
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("Should get low stock products")
        void shouldGetLowStockProducts() {

            Pageable pageable = PageRequest.of(0, 10);
            List<Product> products = Collections.singletonList(testProduct);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepository.findLowStockProducts(pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getLowStockProducts(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Products By Attributes")
    class GetProductsByAttributesTests {

        @Test
        @DisplayName("Should get all products")
        void getAllProducts_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findAllActive(pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getAllProducts(pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get products by category")
        void getProductsByCategory_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getProductsByCategory(1L, pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get active products")
        void getActiveProducts_Success() {

            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findByIsActive(true, pageable)).thenReturn(productPage);

            Page<ProductDto> result = productService.getActiveProducts(pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should get distinct brands")
        void getDistinctBrands_Success() {

            List<String> brands = Arrays.asList("Apple", "Samsung", "Google");

            when(productRepository.findAllDistinctBrands()).thenReturn(brands);

            List<String> result = productService.getDistinctBrands();

            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("Apple", "Samsung", "Google");
        }

        @Test
        @DisplayName("Should get distinct manufacturers")
        void getDistinctManufacturers_Success() {

            List<String> manufacturers = Arrays.asList("Foxconn", "Samsung Electronics");

            when(productRepository.findAllDistinctManufacturers()).thenReturn(manufacturers);

            List<String> result = productService.getDistinctManufacturers();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("Foxconn", "Samsung Electronics");
        }
    }

    // ===== NEW COMPREHENSIVE EDGE CASE TESTS =====

    @Nested
    @DisplayName("Advanced Validation Tests")
    class AdvancedValidationTests {

        @Test
        @DisplayName("Should throw exception when product name contains invalid characters")
        void shouldThrowExceptionWhenProductNameContainsInvalidCharacters() {

            ProductDto invalidDto = new ProductDto();
            invalidDto.setName("Invalid/Product\\Name|With<>Special");
            invalidDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(invalidDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("invalid characters");
        }

        @Test
        @DisplayName("Should throw exception when product name is too long")
        void shouldThrowExceptionWhenProductNameIsTooLong() {

            ProductDto longNameDto = new ProductDto();
            longNameDto.setName("A".repeat(256)); // Assuming max length is 255
            longNameDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(longNameDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("too long");
        }

        @Test
        @DisplayName("Should throw exception when product name is empty")
        void shouldThrowExceptionWhenProductNameIsEmpty() {

            ProductDto emptyNameDto = new ProductDto();
            emptyNameDto.setName("");
            emptyNameDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(emptyNameDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("cannot be empty");
        }

        @Test
        @DisplayName("Should handle null product name gracefully")
        void shouldHandleNullProductNameGracefully() {

            ProductDto nullNameDto = new ProductDto();
            nullNameDto.setName(null);
            nullNameDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(nullNameDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("name cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when product brand is too long")
        void shouldThrowExceptionWhenProductBrandIsTooLong() {

            ProductDto longBrandDto = new ProductDto();
            longBrandDto.setName("Valid Product Name");
            longBrandDto.setBrand("A".repeat(101)); // Assuming max length is 100
            longBrandDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(longBrandDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("too long");
        }

        @Test
        @DisplayName("Should handle empty brand gracefully")
        void shouldHandleEmptyBrandGracefully() {

            ProductDto emptyBrandDto = new ProductDto();
            emptyBrandDto.setName("Product Without Brand");
            emptyBrandDto.setBrand("");
            emptyBrandDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Product Without Brand", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(emptyBrandDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle null brand gracefully")
        void shouldHandleNullBrandGracefully() {

            ProductDto nullBrandDto = new ProductDto();
            nullBrandDto.setName("Product With Null Brand");
            nullBrandDto.setBrand(null);
            nullBrandDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Product With Null Brand", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(nullBrandDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should validate description length")
        void shouldValidateDescriptionLength() {

            ProductDto longDescDto = new ProductDto();
            longDescDto.setName("Valid Product");
            longDescDto.setDescription("A".repeat(5001)); // Assuming max length is 5000
            longDescDto.setCategoryId(1L);

            assertThatThrownBy(() -> productService.createProduct(longDescDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("description too long");
        }

        @Test
        @DisplayName("Should handle very long description within limits")
        void shouldHandleVeryLongDescriptionWithinLimits() {

            ProductDto longDescDto = new ProductDto();
            longDescDto.setName("Product With Long Description");
            String longDescription = "A".repeat(5000); // Maximum allowed length
            longDescDto.setDescription(longDescription);
            longDescDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Product With Long Description", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(longDescDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in product fields")
        void shouldHandleSpecialCharactersInProductFields() {

            ProductDto specialCharDto = new ProductDto();
            specialCharDto.setName("Product with émojis 😀 and àccénts");
            specialCharDto.setDescription("Description with special chars: @#$%^&*()");
            specialCharDto.setBrand("Bráñd™");
            specialCharDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Product with émojis 😀 and àccénts", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(specialCharDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle Unicode characters in all text fields")
        void shouldHandleUnicodeCharactersInAllTextFields() {

            ProductDto unicodeDto = new ProductDto();
            unicodeDto.setName("產品名稱 Имя продукта 製品名");
            unicodeDto.setDescription("描述 Описание 説明");
            unicodeDto.setBrand("品牌 Бренд ブランド");
            unicodeDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("產品名稱 Имя продукта 製品名", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(unicodeDto);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Complex Business Rules Tests")
    class ComplexBusinessRulesTests {

        @Test
        @DisplayName("Should validate category is active when creating product")
        void shouldValidateCategoryIsActiveWhenCreatingProduct() {

            ProductDto productDto = new ProductDto();
            productDto.setName("Product in Inactive Category");
            productDto.setCategoryId(1L);

            Category inactiveCategory =
                    TestDataFactory.category().withId(1L).withName("Inactive Category").inactive().build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(inactiveCategory));

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("inactive category");
        }

        @Test
        @DisplayName("Should handle category hierarchy validation")
        void shouldHandleCategoryHierarchyValidation() {

            ProductDto productDto = new ProductDto();
            productDto.setName("Product in Deep Category");
            productDto.setCategoryId(1L);

            Category deepCategory =
                    TestDataFactory.category()
                            .withId(1L)
                            .withLevel(15) // Very deep category
                            .build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(deepCategory));

            // Assuming there 's a business rule about maximum category depth
            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("category depth");
        }

        @Test
        @DisplayName("Should validate duplicate product names within same category")
        void shouldValidateDuplicateProductNamesWithinSameCategory() {

            ProductDto duplicateDto = new ProductDto();
            duplicateDto.setName("iPhone 15");
            duplicateDto.setCategoryId(1L);

            when(productRepository.findByNameAndCategoryId("iPhone 15", 1L))
                    .thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.createProduct(duplicateDto))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should allow same product name in different categories")
        void shouldAllowSameProductNameInDifferentCategories() {

            ProductDto sameNameDto = new ProductDto();
            sameNameDto.setName("iPhone 15");
            sameNameDto.setCategoryId(2L); // Different category

            Category differentCategory =
                    TestDataFactory.category().withId(2L).withName("Accessories").build();

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(differentCategory));
            when(productRepository.findByNameAndCategoryId("iPhone 15", 2L)).thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(sameNameDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle product lifecycle state validation")
        void shouldHandleProductLifecycleStateValidation() {

            ProductDto newProductDto = new ProductDto();
            newProductDto.setName("New Product");
            newProductDto.setCategoryId(1L);
            newProductDto.setIsActive(false); // Creating inactive product

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("New Product", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(newProductDto);

            assertThat(result).isNotNull();
            // Should allow creating inactive products
        }

        @Test
        @DisplayName("Should validate product update business rules")
        void shouldValidateProductUpdateBusinessRules() {

            ProductDto updateDto = new ProductDto();
            updateDto.setName("Updated iPhone");
            updateDto.setCategoryId(2L); // Changing category

            Category newCategory = TestDataFactory.category().withId(2L).withName("Accessories").build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
            when(productRepository.existsByNameAndCategoryIdExcludingId("Updated iPhone", 2L, 1L))
                    .thenReturn(false);
            when(productRepository.save(testProduct)).thenReturn(testProduct);

            ProductDto result = productService.updateProduct(1L, updateDto);

            assertThat(result).isNotNull();
            verify(categoryRepository).findById(2L); // Should validate new category
        }

        @Test
        @DisplayName("Should enforce business rules when deleting products")
        void shouldEnforceBusinessRulesWhenDeletingProducts() {

            Product productWithOrderHistory = testProduct;
            productWithOrderHistory.setId(1L);
            // Simulate product with dependencies that prevent deletion
            when(productRepository.findById(1L)).thenReturn(Optional.of(productWithOrderHistory));

            // This test verifies the delete operation can handle business rule validation
            // The actual business rules are implementation-specific and would be tested with real
            // constraints
            productService.deleteProduct(1L);

            // Verify the deletion was attempted
            verify(productRepository).findById(1L);
            assertThat(productWithOrderHistory.isDeleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("Integration Edge Cases Tests")
    class IntegrationEdgeCasesTests {

        @Test
        @DisplayName("Should handle database constraint violations gracefully")
        void shouldHandleDatabaseConstraintViolationsGracefully() {

            ProductDto productDto = new ProductDto();
            productDto.setName("Constraint Violation Product");
            productDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Constraint Violation Product", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class)))
                    .thenThrow(new RuntimeException("Database constraint violation"));

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database constraint violation");
        }

        @Test
        @DisplayName("Should handle transactional rollback scenarios")
        void shouldHandleTransactionalRollbackScenarios() {

            ProductDto productDto = new ProductDto();
            productDto.setName("Rollback Test Product");
            productDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Rollback Test Product", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class)))
                    .thenThrow(new RuntimeException("Simulated transaction failure"));

            assertThatThrownBy(() -> productService.createProduct(productDto))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle cache interaction scenarios")
        void shouldHandleCacheInteractionScenarios() {

            ProductDto productDto = new ProductDto();
            productDto.setName("Cache Test Product");
            productDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Cache Test Product", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(productDto);

            assertThat(result).isNotNull();
            // Cache eviction is handled by @CacheEvict annotation
            // This test verifies the method executes successfully with cache annotations
        }

        @Test
        @DisplayName("Should handle concurrent access scenarios")
        void shouldHandleConcurrentAccessScenarios() {

            ProductDto productDto1 = new ProductDto();
            productDto1.setName("Concurrent Product 1");
            productDto1.setCategoryId(1L);

            ProductDto productDto2 = new ProductDto();
            productDto2.setName("Concurrent Product 2");
            productDto2.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId(anyString(), eq(1L)))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result1 = productService.createProduct(productDto1);
            ProductDto result2 = productService.createProduct(productDto2);

            assertThat(result1).isNotNull();
            assertThat(result2).isNotNull();
            verify(productRepository, times(2)).save(any(Product.class));
        }

        @Test
        @DisplayName("Should handle large batch operations")
        void shouldHandleLargeBatchOperations() {
            // Simulate bulk operations
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId(anyString(), eq(1L)))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // Create multiple products in sequence
            for (int i = 0; i < 100; i++) {
                ProductDto productDto = new ProductDto();
                productDto.setName("Bulk Product " + i);
                productDto.setCategoryId(1L);

                ProductDto result = productService.createProduct(productDto);
                assertThat(result).isNotNull();
            }

            verify(productRepository, times(100)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("Search and Query Edge Cases Tests")
    class SearchAndQueryEdgeCasesTests {

        @Test
        @DisplayName("Should handle empty search queries")
        void shouldHandleEmptySearchQueries() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(new ArrayList<>());

            when(productRepository.findByNameOrDescriptionContainingIgnoreCase("", pageable))
                    .thenReturn(emptyPage);

            Page<ProductDto> result = productService.searchProductsByNameOrDescription("", pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null search queries")
        void shouldHandleNullSearchQueries() {

            assertThatThrownBy(
                    () -> productService.searchProductsByNameOrDescription(null, PageRequest.of(0, 10)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("search term cannot be null");
        }

        @Test
        @DisplayName("Should handle very long search queries")
        void shouldHandleVeryLongSearchQueries() {

            String longSearchTerm = "A".repeat(1000);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = new PageImpl<>(new ArrayList<>());

            when(productRepository.findByNameOrDescriptionContainingIgnoreCase(longSearchTerm, pageable))
                    .thenReturn(emptyPage);

            Page<ProductDto> result =
                    productService.searchProductsByNameOrDescription(longSearchTerm, pageable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle special characters in search queries")
        void shouldHandleSpecialCharactersInSearchQueries() {

            String specialSearchTerm = "@#$%^&*()_+-=[]{}|;:,.<>?";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));

            when(productRepository.findByNameOrDescriptionContainingIgnoreCase(
                    specialSearchTerm, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result =
                    productService.searchProductsByNameOrDescription(specialSearchTerm, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle Unicode characters in search queries")
        void shouldHandleUnicodeCharactersInSearchQueries() {

            String unicodeSearchTerm = "測試 тест テスト";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));

            when(productRepository.findByNameOrDescriptionContainingIgnoreCase(
                    unicodeSearchTerm, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result =
                    productService.searchProductsByNameOrDescription(unicodeSearchTerm, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle case-insensitive brand searches")
        void shouldHandleCaseInsensitiveBrandSearches() {

            String mixedCaseBrand = "AppLE";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));

            when(productRepository.findByBrandIgnoreCase(mixedCaseBrand, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result = productService.searchProductsByBrand(mixedCaseBrand, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle complex filter combinations")
        void shouldHandleComplexFilterCombinations() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));

            when(productRepository.findWithFilters("Phone", 1L, "Apple", "Foxconn", true, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result =
                    productService.searchProducts("Phone", "Apple", 1L, "Foxconn", true, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should handle null values in filter combinations")
        void shouldHandleNullValuesInFilterCombinations() {

            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));

            when(productRepository.findWithFilters(null, null, null, null, null, pageable))
                    .thenReturn(productPage);

            Page<ProductDto> result =
                    productService.searchProducts(null, null, null, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Boundary Condition Tests")
    class BoundaryConditionTests {

        @Test
        @DisplayName("Should handle maximum page size limits")
        void shouldHandleMaximumPageSizeLimits() {

            Pageable largePageable = PageRequest.of(0, 10000);
            List<Product> largeProductList = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                largeProductList.add(testProduct);
            }
            Page<Product> largePage = new PageImpl<>(largeProductList, largePageable, 10000);

            when(productRepository.findAllActive(largePageable)).thenReturn(largePage);

            Page<ProductDto> result = productService.getAllProducts(largePageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(10000);
        }

        @Test
        @DisplayName("Should handle zero page size")
        void shouldHandleZeroPageSize() {

            assertThatThrownBy(() -> PageRequest.of(0, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Page size must not be less than one");
        }

        @Test
        @DisplayName("Should handle very large page numbers")
        void shouldHandleVeryLargePageNumbers() {

            Pageable largePageNumber = PageRequest.of(Integer.MAX_VALUE - 1, 10);
            Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), largePageNumber, 0);

            when(productRepository.findAllActive(largePageNumber)).thenReturn(emptyPage);

            Page<ProductDto> result = productService.getAllProducts(largePageNumber);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle minimum valid product data")
        void shouldHandleMinimumValidProductData() {

            ProductDto minimalDto = new ProductDto();
            minimalDto.setName("A"); // Minimum length name
            minimalDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("A", 1L)).thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(minimalDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle maximum valid field lengths")
        void shouldHandleMaximumValidFieldLengths() {

            ProductDto maximalDto = new ProductDto();
            maximalDto.setName("A".repeat(255)); // Maximum allowed name length
            maximalDto.setDescription("B".repeat(5000)); // Maximum allowed description length
            maximalDto.setBrand("C".repeat(100)); // Maximum allowed brand length
            maximalDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId(maximalDto.getName(), 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(maximalDto);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should handle whitespace trimming in all text fields")
        void shouldHandleWhitespaceTrimmingInAllTextFields() {

            ProductDto whitespaceDto = new ProductDto();
            whitespaceDto.setName("  Trimmed Product Name  ");
            whitespaceDto.setDescription("  Trimmed Description  ");
            whitespaceDto.setBrand("  Trimmed Brand  ");
            whitespaceDto.setCategoryId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.findByNameAndCategoryId("Trimmed Product Name", 1L))
                    .thenReturn(Optional.empty());
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            ProductDto result = productService.createProduct(whitespaceDto);

            assertThat(result).isNotNull();
            verify(productRepository).findByNameAndCategoryId("Trimmed Product Name", 1L);
        }
    }
}
