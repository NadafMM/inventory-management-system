package com.inventorymanagement.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;

/**
 * Paged response wrapper for paginated API responses. Provides pagination metadata along with the data.
 *
 * @param <T> the type of data being returned
 * @version 1.0.0
 * @since 2025-01-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private List<T> content;
    private PageMetadata meta;
    private PageLinks links;

    // Constructors
    public PagedResponse() {}

    public PagedResponse(List<T> content, PageMetadata meta) {
        this.content = content;
        this.meta = meta;
    }

    public PagedResponse(List<T> content, PageMetadata meta, PageLinks links) {
        this.content = content;
        this.meta = meta;
        this.links = links;
    }

    // Static factory method to create from Spring Data Page
    public static <T> PagedResponse<T> of(Page<T> page) {
        return of(page, null);
    }

    public static <T> PagedResponse<T> of(Page<T> page, String baseUrl) {
        PageMetadata meta =
                new PageMetadata(
                        page.getTotalElements(),
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious());

        PageLinks links = null;
        if (baseUrl != null) {
            links = new PageLinks(baseUrl, page.getNumber(), page.getSize(), page.getTotalPages());
        }

        return new PagedResponse<>(page.getContent(), meta, links);
    }

    // Getters and setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageMetadata getMeta() {
        return meta;
    }

    public void setMeta(PageMetadata meta) {
        this.meta = meta;
    }

    public PageLinks getLinks() {
        return links;
    }

    public void setLinks(PageLinks links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PagedResponse<?> that = (PagedResponse<?>) obj;
        return Objects.equals(content, that.content)
                && Objects.equals(meta, that.meta)
                && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, meta, links);
    }

    @Override
    public String toString() {
        return "PagedResponse{" + "content=" + content + ", meta=" + meta + ", links=" + links + '}';
    }

    /**
     * Pagination metadata information.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageMetadata {

        @JsonProperty("total_elements")
        private long totalElements;

        @JsonProperty("page_number")
        private int pageNumber;

        @JsonProperty("page_size")
        private int pageSize;

        @JsonProperty("total_pages")
        private int totalPages;

        @JsonProperty("is_first")
        private boolean isFirst;

        @JsonProperty("is_last")
        private boolean isLast;

        @JsonProperty("has_next")
        private boolean hasNext;

        @JsonProperty("has_previous")
        private boolean hasPrevious;

        public PageMetadata() {}

        public PageMetadata(
                long totalElements,
                int pageNumber,
                int pageSize,
                int totalPages,
                boolean isFirst,
                boolean isLast,
                boolean hasNext,
                boolean hasPrevious) {
            this.totalElements = totalElements;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.isFirst = isFirst;
            this.isLast = isLast;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getters and setters
        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public boolean isFirst() {
            return isFirst;
        }

        public void setFirst(boolean first) {
            isFirst = first;
        }

        public boolean isLast() {
            return isLast;
        }

        public void setLast(boolean last) {
            isLast = last;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            PageMetadata that = (PageMetadata) obj;
            return totalElements == that.totalElements
                    && pageNumber == that.pageNumber
                    && pageSize == that.pageSize
                    && totalPages == that.totalPages;
        }

        @Override
        public int hashCode() {
            return Objects.hash(totalElements, pageNumber, pageSize, totalPages);
        }

        @Override
        public String toString() {
            return "PageMetadata{"
                    + "totalElements="
                    + totalElements
                    + ", pageNumber="
                    + pageNumber
                    + ", pageSize="
                    + pageSize
                    + ", totalPages="
                    + totalPages
                    + ", isFirst="
                    + isFirst
                    + ", isLast="
                    + isLast
                    + ", hasNext="
                    + hasNext
                    + ", hasPrevious="
                    + hasPrevious
                    + '}';
        }
    }

    /**
     * Pagination links for navigation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageLinks {

        private String self;
        private String first;
        private String last;
        private String next;
        private String previous;

        public PageLinks() {}

        public PageLinks(String baseUrl, int currentPage, int pageSize, int totalPages) {
            this.self = buildUrl(baseUrl, currentPage, pageSize);
            this.first = buildUrl(baseUrl, 0, pageSize);
            this.last = buildUrl(baseUrl, totalPages - 1, pageSize);

            if (currentPage > 0) {
                this.previous = buildUrl(baseUrl, currentPage - 1, pageSize);
            }

            if (currentPage < totalPages - 1) {
                this.next = buildUrl(baseUrl, currentPage + 1, pageSize);
            }
        }

        private String buildUrl(String baseUrl, int page, int size) {
            return baseUrl + "?page=" + page + "&size=" + size;
        }

        // Getters and setters
        public String getSelf() {
            return self;
        }

        public void setSelf(String self) {
            this.self = self;
        }

        public String getFirst() {
            return first;
        }

        public void setFirst(String first) {
            this.first = first;
        }

        public String getLast() {
            return last;
        }

        public void setLast(String last) {
            this.last = last;
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getPrevious() {
            return previous;
        }

        public void setPrevious(String previous) {
            this.previous = previous;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            PageLinks pageLinks = (PageLinks) obj;
            return Objects.equals(self, pageLinks.self)
                    && Objects.equals(first, pageLinks.first)
                    && Objects.equals(last, pageLinks.last)
                    && Objects.equals(next, pageLinks.next)
                    && Objects.equals(previous, pageLinks.previous);
        }

        @Override
        public int hashCode() {
            return Objects.hash(self, first, last, next, previous);
        }

        @Override
        public String toString() {
            return "PageLinks{"
                    + "self='"
                    + self
                    + '\''
                    + ", first='"
                    + first
                    + '\''
                    + ", last='"
                    + last
                    + '\''
                    + ", next='"
                    + next
                    + '\''
                    + ", previous='"
                    + previous
                    + '\''
                    + '}';
        }
    }
}
