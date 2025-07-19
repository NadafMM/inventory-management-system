package com.inventorymanagement.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Comprehensive tests for PagedResponse and its inner classes to achieve maximum code coverage
 */
@DisplayName("PagedResponse Tests")
class PagedResponseTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create PagedResponse with default constructor")
        void shouldCreateWithDefaultConstructor() {
            PagedResponse<String> response = new PagedResponse<>();

            assertThat(response.getContent()).isNull();
            assertThat(response.getMeta()).isNull();
            assertThat(response.getLinks()).isNull();
        }

        @Test
        @DisplayName("Should create PagedResponse with content and meta")
        void shouldCreateWithContentAndMeta() {
            List<String> content = List.of("item1", "item2");
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();
            PagedResponse<String> response = new PagedResponse<>(content, meta);

            assertThat(response.getContent()).isEqualTo(content);
            assertThat(response.getMeta()).isEqualTo(meta);
            assertThat(response.getLinks()).isNull();
        }

        @Test
        @DisplayName("Should create PagedResponse with content, meta, and links")
        void shouldCreateWithContentMetaAndLinks() {
            List<String> content = List.of("item1", "item2");
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();
            PagedResponse<String> response = new PagedResponse<>(content, meta, links);

            assertThat(response.getContent()).isEqualTo(content);
            assertThat(response.getMeta()).isEqualTo(meta);
            assertThat(response.getLinks()).isEqualTo(links);
        }
    }

    @Nested
    @DisplayName("Static Factory Methods Tests")
    class StaticFactoryMethodsTests {

        @Test
        @DisplayName("Should create PagedResponse from Spring Page without baseUrl")
        void shouldCreateFromSpringPageWithoutBaseUrl() {
            List<String> content = List.of("item1", "item2", "item3");
            Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 5);

            PagedResponse<String> response = PagedResponse.of(page);

            assertThat(response.getContent()).isEqualTo(content);
            assertThat(response.getMeta()).isNotNull();
            assertThat(response.getMeta().getTotalElements()).isEqualTo(5);
            assertThat(response.getMeta().getPageNumber()).isEqualTo(1);
            assertThat(response.getMeta().getPageSize()).isEqualTo(2);
            assertThat(response.getMeta().getTotalPages()).isEqualTo(3);
            assertThat(response.getMeta().isFirst()).isFalse();
            assertThat(response.getMeta().isLast()).isFalse();
            assertThat(response.getMeta().isHasNext()).isTrue();
            assertThat(response.getMeta().isHasPrevious()).isTrue();
            assertThat(response.getLinks()).isNull();
        }

        @Test
        @DisplayName("Should create PagedResponse from Spring Page with baseUrl")
        void shouldCreateFromSpringPageWithBaseUrl() {
            List<String> content = List.of("item1", "item2");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 2), 5);
            String baseUrl = "http://localhost:8080/api/items";

            PagedResponse<String> response = PagedResponse.of(page, baseUrl);

            assertThat(response.getContent()).isEqualTo(content);
            assertThat(response.getMeta()).isNotNull();
            assertThat(response.getLinks()).isNotNull();
            assertThat(response.getLinks().getSelf())
                    .isEqualTo("http://localhost:8080/api/items?page=0&size=2");
            assertThat(response.getLinks().getFirst())
                    .isEqualTo("http://localhost:8080/api/items?page=0&size=2");
            assertThat(response.getLinks().getLast())
                    .isEqualTo("http://localhost:8080/api/items?page=2&size=2");
            assertThat(response.getLinks().getNext())
                    .isEqualTo("http://localhost:8080/api/items?page=1&size=2");
            assertThat(response.getLinks().getPrevious()).isNull();
        }

        @Test
        @DisplayName("Should create PagedResponse from first page")
        void shouldCreateFromFirstPage() {
            List<String> content = List.of("item1", "item2");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 2), 4);

            PagedResponse<String> response = PagedResponse.of(page);

            assertThat(response.getMeta().isFirst()).isTrue();
            assertThat(response.getMeta().isLast()).isFalse();
            assertThat(response.getMeta().isHasNext()).isTrue();
            assertThat(response.getMeta().isHasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Should create PagedResponse from last page")
        void shouldCreateFromLastPage() {
            List<String> content = List.of("item3", "item4");
            Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 4);

            PagedResponse<String> response = PagedResponse.of(page);

            assertThat(response.getMeta().isFirst()).isFalse();
            assertThat(response.getMeta().isLast()).isTrue();
            assertThat(response.getMeta().isHasNext()).isFalse();
            assertThat(response.getMeta().isHasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should create PagedResponse from single page")
        void shouldCreateFromSinglePage() {
            List<String> content = List.of("item1", "item2");
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 2);

            PagedResponse<String> response = PagedResponse.of(page);

            assertThat(response.getMeta().isFirst()).isTrue();
            assertThat(response.getMeta().isLast()).isTrue();
            assertThat(response.getMeta().isHasNext()).isFalse();
            assertThat(response.getMeta().isHasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Should create PagedResponse from empty page")
        void shouldCreateFromEmptyPage() {
            List<String> content = List.of();
            Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 0);

            PagedResponse<String> response = PagedResponse.of(page);

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getMeta().getTotalElements()).isEqualTo(0);
            assertThat(response.getMeta().getTotalPages()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Should set and get content")
        void shouldSetAndGetContent() {
            PagedResponse<String> response = new PagedResponse<>();
            List<String> content = List.of("item1", "item2");

            response.setContent(content);

            assertThat(response.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("Should set and get meta")
        void shouldSetAndGetMeta() {
            PagedResponse<String> response = new PagedResponse<>();
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();

            response.setMeta(meta);

            assertThat(response.getMeta()).isEqualTo(meta);
        }

        @Test
        @DisplayName("Should set and get links")
        void shouldSetAndGetLinks() {
            PagedResponse<String> response = new PagedResponse<>();
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();

            response.setLinks(links);

            assertThat(response.getLinks()).isEqualTo(links);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            PagedResponse<String> response =
                    new PagedResponse<>(List.of("item1"), new PagedResponse.PageMetadata());

            assertThat(response).isEqualTo(response);
            assertThat(response.hashCode()).isEqualTo(response.hashCode());
        }

        @Test
        @DisplayName("Should be equal to another instance with same values")
        void shouldBeEqualToAnotherInstanceWithSameValues() {
            List<String> content = List.of("item1", "item2");
            PagedResponse.PageMetadata meta =
                    new PagedResponse.PageMetadata(5, 0, 2, 3, true, false, true, false);
            PagedResponse<String> response1 = new PagedResponse<>(content, meta);
            PagedResponse<String> response2 = new PagedResponse<>(content, meta);

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            PagedResponse<String> response = new PagedResponse<>();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            PagedResponse<String> response = new PagedResponse<>();
            String differentObject = "different";

            assertThat(response).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("Should not be equal when content differs")
        void shouldNotBeEqualWhenContentDiffers() {
            PagedResponse<String> response1 =
                    new PagedResponse<>(List.of("item1"), new PagedResponse.PageMetadata());
            PagedResponse<String> response2 =
                    new PagedResponse<>(List.of("item2"), new PagedResponse.PageMetadata());

            assertThat(response1).isNotEqualTo(response2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString")
        void shouldGenerateToString() {
            List<String> content = List.of("item1", "item2");
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();
            PagedResponse<String> response = new PagedResponse<>(content, meta);
            String toString = response.toString();

            assertThat(toString).contains("PagedResponse{");
            assertThat(toString).contains("content=[item1, item2]");
            assertThat(toString).contains("meta=");
        }
    }

    @Nested
    @DisplayName("PageMetadata Tests")
    class PageMetadataTests {

        @Test
        @DisplayName("Should create PageMetadata with default constructor")
        void shouldCreatePageMetadataWithDefaultConstructor() {
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();

            assertThat(meta.getTotalElements()).isEqualTo(0);
            assertThat(meta.getPageNumber()).isEqualTo(0);
            assertThat(meta.getPageSize()).isEqualTo(0);
            assertThat(meta.getTotalPages()).isEqualTo(0);
            assertThat(meta.isFirst()).isFalse();
            assertThat(meta.isLast()).isFalse();
            assertThat(meta.isHasNext()).isFalse();
            assertThat(meta.isHasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Should create PageMetadata with all parameters")
        void shouldCreatePageMetadataWithAllParameters() {
            PagedResponse.PageMetadata meta =
                    new PagedResponse.PageMetadata(100, 2, 10, 10, false, false, true, true);

            assertThat(meta.getTotalElements()).isEqualTo(100);
            assertThat(meta.getPageNumber()).isEqualTo(2);
            assertThat(meta.getPageSize()).isEqualTo(10);
            assertThat(meta.getTotalPages()).isEqualTo(10);
            assertThat(meta.isFirst()).isFalse();
            assertThat(meta.isLast()).isFalse();
            assertThat(meta.isHasNext()).isTrue();
            assertThat(meta.isHasPrevious()).isTrue();
        }

        @Test
        @DisplayName("Should set and get all PageMetadata properties")
        void shouldSetAndGetAllPageMetadataProperties() {
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();

            meta.setTotalElements(50);
            meta.setPageNumber(1);
            meta.setPageSize(5);
            meta.setTotalPages(10);
            meta.setFirst(false);
            meta.setLast(false);
            meta.setHasNext(true);
            meta.setHasPrevious(true);

            assertThat(meta.getTotalElements()).isEqualTo(50);
            assertThat(meta.getPageNumber()).isEqualTo(1);
            assertThat(meta.getPageSize()).isEqualTo(5);
            assertThat(meta.getTotalPages()).isEqualTo(10);
            assertThat(meta.isFirst()).isFalse();
            assertThat(meta.isLast()).isFalse();
            assertThat(meta.isHasNext()).isTrue();
            assertThat(meta.isHasPrevious()).isTrue();
        }

        @Test
        @DisplayName("PageMetadata should be equal to itself")
        void pageMetadataShouldBeEqualToItself() {
            PagedResponse.PageMetadata meta =
                    new PagedResponse.PageMetadata(10, 0, 5, 2, true, false, true, false);

            assertThat(meta).isEqualTo(meta);
            assertThat(meta.hashCode()).isEqualTo(meta.hashCode());
        }

        @Test
        @DisplayName("PageMetadata should be equal to another with same values")
        void pageMetadataShouldBeEqualToAnotherWithSameValues() {
            PagedResponse.PageMetadata meta1 =
                    new PagedResponse.PageMetadata(10, 0, 5, 2, true, false, true, false);
            PagedResponse.PageMetadata meta2 =
                    new PagedResponse.PageMetadata(10, 0, 5, 2, false, true, false, true);

            assertThat(meta1).isEqualTo(meta2);
            assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode());
        }

        @Test
        @DisplayName("PageMetadata should not be equal to null")
        void pageMetadataShouldNotBeEqualToNull() {
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();

            assertThat(meta).isNotEqualTo(null);
        }

        @Test
        @DisplayName("PageMetadata should not be equal to different class")
        void pageMetadataShouldNotBeEqualToDifferentClass() {
            PagedResponse.PageMetadata meta = new PagedResponse.PageMetadata();
            String differentObject = "different";

            assertThat(meta).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("PageMetadata should not be equal when totalElements differs")
        void pageMetadataShouldNotBeEqualWhenTotalElementsDiffers() {
            PagedResponse.PageMetadata meta1 =
                    new PagedResponse.PageMetadata(10, 0, 5, 2, true, false, true, false);
            PagedResponse.PageMetadata meta2 =
                    new PagedResponse.PageMetadata(20, 0, 5, 2, true, false, true, false);

            assertThat(meta1).isNotEqualTo(meta2);
        }

        @Test
        @DisplayName("PageMetadata should generate toString")
        void pageMetadataShouldGenerateToString() {
            PagedResponse.PageMetadata meta =
                    new PagedResponse.PageMetadata(25, 1, 5, 5, false, false, true, true);
            String toString = meta.toString();

            assertThat(toString).contains("PageMetadata{");
            assertThat(toString).contains("totalElements=25");
            assertThat(toString).contains("pageNumber=1");
            assertThat(toString).contains("pageSize=5");
            assertThat(toString).contains("totalPages=5");
            assertThat(toString).contains("isFirst=false");
            assertThat(toString).contains("isLast=false");
            assertThat(toString).contains("hasNext=true");
            assertThat(toString).contains("hasPrevious=true");
        }
    }

    @Nested
    @DisplayName("PageLinks Tests")
    class PageLinksTests {

        @Test
        @DisplayName("Should create PageLinks with default constructor")
        void shouldCreatePageLinksWithDefaultConstructor() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();

            assertThat(links.getSelf()).isNull();
            assertThat(links.getFirst()).isNull();
            assertThat(links.getLast()).isNull();
            assertThat(links.getNext()).isNull();
            assertThat(links.getPrevious()).isNull();
        }

        @Test
        @DisplayName("Should create PageLinks with baseUrl for first page")
        void shouldCreatePageLinksWithBaseUrlForFirstPage() {
            String baseUrl = "http://localhost:8080/api/test";
            PagedResponse.PageLinks links = new PagedResponse.PageLinks(baseUrl, 0, 10, 3);

            assertThat(links.getSelf()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getFirst()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getLast()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getNext()).isEqualTo("http://localhost:8080/api/test?page=1&size=10");
            assertThat(links.getPrevious()).isNull();
        }

        @Test
        @DisplayName("Should create PageLinks with baseUrl for middle page")
        void shouldCreatePageLinksWithBaseUrlForMiddlePage() {
            String baseUrl = "http://localhost:8080/api/test";
            PagedResponse.PageLinks links = new PagedResponse.PageLinks(baseUrl, 1, 10, 3);

            assertThat(links.getSelf()).isEqualTo("http://localhost:8080/api/test?page=1&size=10");
            assertThat(links.getFirst()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getLast()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getNext()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getPrevious()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
        }

        @Test
        @DisplayName("Should create PageLinks with baseUrl for last page")
        void shouldCreatePageLinksWithBaseUrlForLastPage() {
            String baseUrl = "http://localhost:8080/api/test";
            PagedResponse.PageLinks links = new PagedResponse.PageLinks(baseUrl, 2, 10, 3);

            assertThat(links.getSelf()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getFirst()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getLast()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getNext()).isNull();
            assertThat(links.getPrevious()).isEqualTo("http://localhost:8080/api/test?page=1&size=10");
        }

        @Test
        @DisplayName("Should create PageLinks with baseUrl for single page")
        void shouldCreatePageLinksWithBaseUrlForSinglePage() {
            String baseUrl = "http://localhost:8080/api/test";
            PagedResponse.PageLinks links = new PagedResponse.PageLinks(baseUrl, 0, 10, 1);

            assertThat(links.getSelf()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getFirst()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getLast()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getNext()).isNull();
            assertThat(links.getPrevious()).isNull();
        }

        @Test
        @DisplayName("Should set and get all PageLinks properties")
        void shouldSetAndGetAllPageLinksProperties() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();

            links.setSelf("http://localhost:8080/api/test?page=1&size=10");
            links.setFirst("http://localhost:8080/api/test?page=0&size=10");
            links.setLast("http://localhost:8080/api/test?page=5&size=10");
            links.setNext("http://localhost:8080/api/test?page=2&size=10");
            links.setPrevious("http://localhost:8080/api/test?page=0&size=10");

            assertThat(links.getSelf()).isEqualTo("http://localhost:8080/api/test?page=1&size=10");
            assertThat(links.getFirst()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
            assertThat(links.getLast()).isEqualTo("http://localhost:8080/api/test?page=5&size=10");
            assertThat(links.getNext()).isEqualTo("http://localhost:8080/api/test?page=2&size=10");
            assertThat(links.getPrevious()).isEqualTo("http://localhost:8080/api/test?page=0&size=10");
        }

        @Test
        @DisplayName("PageLinks should be equal to itself")
        void pageLinksShouldBeEqualToItself() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks("http://test.com", 0, 10, 1);

            assertThat(links).isEqualTo(links);
            assertThat(links.hashCode()).isEqualTo(links.hashCode());
        }

        @Test
        @DisplayName("PageLinks should be equal to another with same values")
        void pageLinksShouldBeEqualToAnotherWithSameValues() {
            PagedResponse.PageLinks links1 = new PagedResponse.PageLinks("http://test.com", 0, 10, 2);
            PagedResponse.PageLinks links2 = new PagedResponse.PageLinks("http://test.com", 0, 10, 2);

            assertThat(links1).isEqualTo(links2);
            assertThat(links1.hashCode()).isEqualTo(links2.hashCode());
        }

        @Test
        @DisplayName("PageLinks should not be equal to null")
        void pageLinksShouldNotBeEqualToNull() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();

            assertThat(links).isNotEqualTo(null);
        }

        @Test
        @DisplayName("PageLinks should not be equal to different class")
        void pageLinksShouldNotBeEqualToDifferentClass() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks();
            String differentObject = "different";

            assertThat(links).isNotEqualTo(differentObject);
        }

        @Test
        @DisplayName("PageLinks should not be equal when self differs")
        void pageLinksShouldNotBeEqualWhenSelfDiffers() {
            PagedResponse.PageLinks links1 = new PagedResponse.PageLinks();
            PagedResponse.PageLinks links2 = new PagedResponse.PageLinks();
            links1.setSelf("http://test1.com");
            links2.setSelf("http://test2.com");

            assertThat(links1).isNotEqualTo(links2);
        }

        @Test
        @DisplayName("PageLinks should generate toString")
        void pageLinksShouldGenerateToString() {
            PagedResponse.PageLinks links = new PagedResponse.PageLinks("http://test.com", 1, 5, 3);
            String toString = links.toString();

            assertThat(toString).contains("PageLinks{");
            assertThat(toString).contains("self='http://test.com?page=1&size=5'");
            assertThat(toString).contains("first='http://test.com?page=0&size=5'");
            assertThat(toString).contains("last='http://test.com?page=2&size=5'");
            assertThat(toString).contains("next='http://test.com?page=2&size=5'");
            assertThat(toString).contains("previous='http://test.com?page=0&size=5'");
        }

        @Test
        @DisplayName("PageLinks should handle null values in equals")
        void pageLinksShouldHandleNullValuesInEquals() {
            PagedResponse.PageLinks links1 = new PagedResponse.PageLinks();
            PagedResponse.PageLinks links2 = new PagedResponse.PageLinks();

            assertThat(links1).isEqualTo(links2);
            assertThat(links1.hashCode()).isEqualTo(links2.hashCode());
        }
    }
}
