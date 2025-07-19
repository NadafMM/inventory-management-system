# Inventory Management System - Technical Product Requirements Document (PRD)

## 1. Technical Executive Summary

### Architecture Overview
The Inventory Management System follows a layered architecture pattern built on Spring Boot framework with SQLite database. The system implements RESTful APIs with JWT authentication, structured logging, and comprehensive caching strategies to deliver high-performance inventory management capabilities.

### Key Technical Decisions
- **Architecture Pattern**: Layered Architecture (Controller-Service-Repository)
- **Database Strategy**: SQLite with Path Enumeration for hierarchical categories
- **API Design**: RESTful with URL path versioning and JWT authentication
- **Caching**: Multi-level caching with Caffeine and future Redis integration
- **Security**: JWT tokens with Role-Based Access Control (RBAC)

### Technical Objectives
- Ensure sub-2-second API response times for 95% of requests
- Implement comprehensive error handling and resilience patterns
- Provide structured logging and distributed tracing capabilities
- Support concurrent operations with optimistic locking
- Enable seamless horizontal scaling through modular design

## 2. Database Architecture & Design

### 2.1 Database Schema Design

#### Hierarchical Category Structure
- **Pattern**: Path Enumeration Pattern for optimal read performance
- **Implementation**: Store category path as materialized string (e.g., `/electronics/smartphones/apple/`)
- **Benefits**: Excellent query performance, simple ancestor/descendant queries
- **Trade-offs**: Requires path updates when moving categories

#### Soft Delete Strategy
- **Approach**: `deleted_at` timestamp approach
- **Benefits**: Better auditing, data insight, and recovery capabilities
- **Implementation**: All entities include `deleted_at TIMESTAMP NULL`
- **Query Pattern**: Always filter `WHERE deleted_at IS NULL` for active records

#### Referential Integrity
- **Primary Strategy**: Database-level cascading constraints
- **Supplementary**: Application-level validation for business rules
- **Foreign Key Constraints**: Enforce at database level with appropriate CASCADE/RESTRICT rules

### 2.2 Data Storage & Performance

#### Product Images Storage
- **Strategy**: Store file system paths or cloud blob storage URLs in database
- **Database Field**: `image_urls TEXT[]` or JSON field for multiple images
- **External Storage**: File system for development, cloud blob storage for production
- **CDN Integration**: Ready for future CDN implementation

#### SKU Variant Attributes
- **Structured Attributes**: Separate normalized tables for common attributes (color, size, weight, dimensions)
- **Flexible Attributes**: JSON fields for less structured or product-specific attributes
- **Schema Design**:
  ```sql
  -- Normalized approach for common attributes
  CREATE TABLE sku_attributes (
    id INTEGER PRIMARY KEY,
    sku_id INTEGER REFERENCES skus(id),
    attribute_name VARCHAR(50),
    attribute_value VARCHAR(255)
  );
  
  -- JSON approach for flexible attributes
  ALTER TABLE skus ADD COLUMN flexible_attributes JSON;
  ```

#### Database Indexing Strategy
- **Primary Keys**: Auto-indexed by SQLite
- **Foreign Keys**: Explicit indexes on all foreign key columns
- **Query Optimization**: Indexes on frequently queried columns
- **Composite Indexes**: Strategic multi-column indexes
- **Specific Indexes**:
  ```sql
  CREATE INDEX idx_categories_path ON categories(path);
  CREATE INDEX idx_categories_parent_deleted ON categories(parent_id, deleted_at);
  CREATE INDEX idx_products_category_status ON products(category_id, status, deleted_at);
  CREATE INDEX idx_skus_product_status ON skus(product_id, status, deleted_at);
  CREATE INDEX idx_products_name_search ON products(name COLLATE NOCASE);
  ```

### 2.3 Transaction Management

#### Concurrent Stock Updates
- **Primary Strategy**: Optimistic locking using version numbers
- **Implementation**: Add `version` column to SKU table
- **Fallback**: Pessimistic locking for highly critical sections
- **Database Transactions**: Wrap all inventory operations in transactions

#### SQLite Limitations
- **No Distributed Transactions**: Rely on SQLite's internal locking
- **Write Serialization**: SQLite serializes writes inherently
- **Connection Strategy**: Minimize connection hold time

## 3. API Architecture & Design

### 3.1 API Versioning Strategy

#### URL Path Versioning
- **Format**: `/api/v1/products`, `/api/v2/products`
- **Version Header**: Optional `API-Version` header for client preferences
- **Backward Compatibility Rules**:
  - Additive changes only for minor versions
  - Graceful deprecation with 6-month notice
  - New major versions for breaking changes

#### Version Management
- **Current Version**: v1 (initial release)
- **Deprecation Process**: 
  1. Announce deprecation in API response headers
  2. Provide migration documentation
  3. Maintain old version for 6 months minimum
  4. Sunset with proper HTTP 410 Gone responses

### 3.2 Request/Response Design

#### Operation Support
- **Single Operations**: All CRUD operations support single-item processing
- **Bulk Operations**: Support for bulk create/update/delete operations
- **Batch Size Limits**: Maximum 100 items per bulk operation

#### Response Strategy
- **Default**: Minimal representations for list operations
- **Expansion**: Optional expansion via query parameters
  - `?expand=category,skus` for detailed product responses
  - `?fields=id,name,price` for field selection
- **Consistency**: Uniform response structure across all endpoints

#### Response Format
```json
{
  "data": {}, // or [] for collections
  "meta": {
    "total": 150,
    "page": 1,
    "pageSize": 10,
    "totalPages": 15
  },
  "links": {
    "self": "/api/v1/products?page=1",
    "next": "/api/v1/products?page=2",
    "prev": null
  }
}
```

### 3.3 Search & Filtering Implementation

#### Search Strategy
- **Phase 1**: SQL LIKE queries with proper indexing
- **Phase 2**: Elasticsearch integration for complex search needs
- **Search Fields**: Product name, description, brand, SKU codes
- **Search Types**:
  - Exact match for IDs and codes
  - Partial match for names and descriptions
  - Future: Fuzzy search and full-text search

#### Filtering Logic
- **Default Logic**: AND operation between different filter types
- **OR Logic**: Comma-separated values within same filter type
- **Filter Examples**:
  - `?category=electronics&brand=apple,samsung`
  - `?priceMin=100&priceMax=500&status=active`
  - `?inStock=true&brand=apple`

#### Search Results Caching
- **Cache Strategy**: TTL-based with event-driven invalidation
- **Cache Keys**: Hash of search parameters and filters
- **Cache Duration**: 15 minutes for search results
- **Invalidation**: Category/product updates trigger cache invalidation

## 4. Security & Authentication

### 4.1 Authentication Mechanism

#### JWT Token Strategy
- **Token Type**: Stateless JWT tokens
- **Token Lifetime**: 24 hours for access tokens
- **Refresh Strategy**: Refresh tokens with 7-day lifetime
- **Token Claims**: User ID, roles, permissions, expiration
- **Signing Algorithm**: RS256 for production security

#### Token Management
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
  "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

### 4.2 Role-Based Access Control (RBAC)

#### Role Definitions
- **ADMIN**: Full system access, user management
- **INVENTORY_MANAGER**: Product/SKU/Category management
- **WAREHOUSE_STAFF**: Stock updates, inventory operations
- **VIEWER**: Read-only access to products and inventory

#### Permission Matrix
| Operation | ADMIN | INVENTORY_MANAGER | WAREHOUSE_STAFF | VIEWER |
|-----------|-------|-------------------|-----------------|--------|
| Create Categories | ✅ | ✅ | ❌ | ❌ |
| Update Products | ✅ | ✅ | ❌ | ❌ |
| Update Stock | ✅ | ✅ | ✅ | ❌ |
| View Reports | ✅ | ✅ | ✅ | ✅ |
| User Management | ✅ | ❌ | ❌ | ❌ |

### 4.3 API Rate Limiting

#### Rate Limiting Strategy
- **Implementation**: Token bucket algorithm
- **Limits by Role**:
  - ADMIN: 1000 requests/hour
  - INVENTORY_MANAGER: 500 requests/hour
  - WAREHOUSE_STAFF: 200 requests/hour
  - VIEWER: 100 requests/hour
- **Rate Limit Headers**:
  ```
  X-RateLimit-Limit: 1000
  X-RateLimit-Remaining: 999
  X-RateLimit-Reset: 1642694400
  ```

## 5. Error Handling & Resilience

### 5.1 Exception Handling Strategy

#### Global Exception Handler
- **Implementation**: Spring Boot `@ControllerAdvice`
- **Standardized Responses**: Consistent error format across all endpoints
- **Error Categories**: Validation, Business Logic, System, Security

#### Controller-Specific Handling
- **Validation Errors**: Detailed field-level validation messages
- **Business Rule Violations**: Domain-specific error codes and messages
- **Bulk Operation Errors**: Individual item success/failure reporting

### 5.2 Database Resilience

#### Connection Management
- **Connection Pool**: HikariCP with SQLite-optimized settings
- **Pool Size**: 
  - Write operations: 1-2 connections
  - Read operations: 5-10 connections
- **Connection Timeout**: 30 seconds
- **Retry Strategy**: Exponential backoff for transient failures

#### Failure Handling
- **Database Unavailable**: Graceful degradation with cached responses
- **Transaction Failures**: Automatic retry with exponential backoff
- **Deadlock Detection**: Retry mechanism with jitter

### 5.3 Bulk Operation Error Handling

#### Multi-Status Response (207)
```json
{
  "results": [
    {
      "index": 0,
      "status": "success",
      "data": {"id": 1, "name": "Product 1"}
    },
    {
      "index": 1,
      "status": "error",
      "error": {
        "code": "VALIDATION_ERROR",
        "message": "Product name is required"
      }
    }
  ],
  "summary": {
    "total": 2,
    "successful": 1,
    "failed": 1
  }
}
```

## 6. Logging & Monitoring

### 6.1 Structured Logging

#### JSON Format
- **Log Format**: Structured JSON for machine readability
- **Required Fields**: timestamp, level, message, service, traceId, spanId
- **Contextual Fields**: userId, operation, duration, status

#### Log Levels Implementation
- **ERROR**: System failures, unhandled exceptions, critical business errors
- **WARN**: Deprecated API usage, performance degradation, business rule violations
- **INFO**: Request/response logging, significant business events, startup/shutdown
- **DEBUG**: Detailed execution flow, variable values, SQL queries (dev only)

### 6.2 Distributed Tracing

#### Implementation
- **Framework**: Spring Cloud Sleuth with Zipkin
- **Trace Propagation**: HTTP headers for cross-service correlation
- **Sampling Rate**: 10% in production, 100% in development
- **Trace Context**: Includes user context, operation type, performance metrics

### 6.3 Application Metrics

#### Metrics Endpoint
- **Format**: Prometheus-compatible metrics endpoint `/actuator/metrics`
- **Custom Metrics**: Business-specific metrics (inventory levels, search performance)
- **System Metrics**: JVM metrics, database connections, cache hit rates

#### Key Metrics
- **API Performance**: Request duration, error rates, throughput
- **Business Metrics**: Inventory turnover, search query performance
- **System Health**: Database connection pool utilization, memory usage

## 7. Performance & Scalability

### 7.1 Caching Strategy

#### Multi-Level Caching
- **L1 Cache**: In-memory Caffeine cache for frequently accessed data
- **L2 Cache**: Redis for distributed caching (future implementation)
- **Cache Hierarchy**: Application → In-Memory → Distributed → Database

#### Cache Configuration
```yaml
cache:
  categories:
    ttl: 1h
    maxSize: 1000
  products:
    ttl: 30m
    maxSize: 5000
  searchResults:
    ttl: 15m
    maxSize: 10000
```

#### Cache Invalidation
- **Event-Driven**: Entity updates trigger cache invalidation
- **Tag-Based**: Group related cache entries for bulk invalidation
- **TTL Fallback**: Time-based expiration as safety net

### 7.2 Database Performance

#### Query Optimization
- **Index Usage**: Analyze query plans and optimize indexes
- **Query Patterns**: Prefer indexed columns in WHERE clauses
- **Batch Operations**: Use batch inserts/updates for bulk operations
- **Connection Pooling**: Optimize pool size for SQLite characteristics

#### SQLite Optimization
- **WAL Mode**: Enable Write-Ahead Logging for better concurrency
- **Pragma Settings**: Optimize SQLite pragmas for performance
- **Query Planning**: Use EXPLAIN QUERY PLAN for optimization

### 7.3 API Performance

#### Response Time Targets
- **Simple Queries**: < 200ms (95th percentile)
- **Complex Searches**: < 1000ms (95th percentile)
- **Bulk Operations**: < 5000ms (95th percentile)

#### Performance Monitoring
- **Request Tracking**: Monitor API response times per endpoint
- **Slow Query Detection**: Log queries exceeding performance thresholds
- **Performance Alerts**: Automated alerts for performance degradation

## 8. Development & Deployment

### 8.1 Project Structure

#### Modular Architecture
```
inventory-management-system/
├── category-service/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
├── product-service/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
├── inventory-service/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── model/
├── common/
│   ├── security/
│   ├── exception/
│   ├── validation/
│   └── util/
└── application/
    ├── config/
    ├── Application.java
    └── resources/
```

#### Layer Responsibilities
- **Controller**: HTTP request/response handling, validation
- **Service**: Business logic, transaction management
- **Repository**: Data access, query optimization
- **Model**: Entity definitions, data transfer objects

### 8.2 Configuration Management

#### Environment Profiles
- **Development**: `application-dev.yml`
- **Testing**: `application-test.yml`
- **Production**: `application-prod.yml`

#### Configuration Strategy
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DATABASE_URL:jdbc:sqlite:inventory.db}
  
# Environment variables for sensitive data
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400}
```

#### Feature Flags
- **Implementation**: Spring Boot conditional beans
- **Configuration**: Environment-based feature toggles
- **Use Cases**: Gradual rollout, A/B testing, emergency switches

### 8.3 Testing Strategy

#### Testing Pyramid
- **Unit Tests**: 70% coverage, fast execution
- **Integration Tests**: 20% coverage, database interactions
- **End-to-End Tests**: 10% coverage, full API workflows

#### Database Testing
- **Unit Tests**: Embedded H2 database for speed
- **Integration Tests**: TestContainers with SQLite for accuracy
- **Test Data**: SQL scripts for baseline, factory patterns for dynamic data

#### Contract Testing
- **Implementation**: Spring Cloud Contract or Pact
- **Provider Tests**: Verify API contract compliance
- **Consumer Tests**: Mock external dependencies

## 9. Integration & Extensibility

### 9.1 Event-Driven Architecture

#### Message Queue Integration
- **Initial**: In-memory event publishing
- **Future**: RabbitMQ or Apache Kafka for distributed events
- **Event Types**: InventoryUpdated, ProductCreated, CategoryDeleted

#### Event Schema
```json
{
  "eventType": "InventoryUpdated",
  "timestamp": "2025-01-15T10:30:00Z",
  "source": "inventory-service",
  "data": {
    "skuId": 123,
    "oldQuantity": 10,
    "newQuantity": 8,
    "operation": "SALE"
  },
  "metadata": {
    "userId": "user123",
    "traceId": "trace456"
  }
}
```

### 9.2 Webhook Support

#### Webhook Configuration
- **Registration**: API endpoints for webhook registration
- **Security**: HMAC signature verification
- **Retry Logic**: Exponential backoff for failed deliveries
- **Event Types**: Configurable event subscriptions

#### Webhook Payload
```json
{
  "webhook_id": "wh_123",
  "event_type": "product.updated",
  "timestamp": "2025-01-15T10:30:00Z",
  "data": {
    "product_id": 456,
    "changes": ["name", "price"]
  }
}
```

### 9.3 External Integration Patterns

#### E-commerce Platform Integration
- **Adapter Pattern**: Platform-specific adapters
- **Common Interface**: Standardized internal API
- **Supported Platforms**: Shopify, WooCommerce, Magento (future)

#### Integration Architecture
```
Internal API ← → Platform Adapter ← → External Platform
     ↓                ↓                     ↓
   Events         Transformation      Platform-specific
                                         API calls
```

## 10. Data Migration & Schema Evolution

### 10.1 Database Migration Strategy

#### Migration Tool
- **Tool**: Flyway for database version control
- **Migration Files**: SQL scripts with version numbers
- **Rollback Strategy**: Separate rollback scripts for each migration

#### Migration Patterns
```sql
-- V1.0.0__Initial_schema.sql
-- V1.0.1__Add_product_images.sql
-- V1.1.0__Add_category_hierarchy.sql
-- V2.0.0__Add_inventory_reservations.sql
```

### 10.2 Data Seeding

#### Seed Data Strategy
- **Categories**: Hierarchical seed data for common categories
- **Test Products**: Sample products for development/testing
- **User Roles**: Default admin user and role definitions

#### Seed Data Management
```sql
-- R__Seed_categories.sql (repeatable)
-- R__Seed_test_products.sql (repeatable, dev only)
-- R__Seed_admin_user.sql (repeatable)
```

## 11. Specific Technical Implementations

### 11.1 SKU Code Generation

#### Generation Pattern
- **Format**: `{CATEGORY_PREFIX}-{YYYYMMDD}-{SEQUENCE}`
- **Example**: `APPAREL-20250115-00001`
- **Components**:
  - Category prefix: 3-8 character category identifier
  - Date: Creation date for temporal grouping
  - Sequence: 5-digit zero-padded sequence number

#### Implementation Strategy
```java
@Service
public class SkuCodeGenerator {
    public String generateSkuCode(String categoryPrefix) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = getNextSequence(categoryPrefix, datePart);
        return String.format("%s-%s-%05d", categoryPrefix, datePart, sequence);
    }
    
    @Retryable(value = {DataIntegrityViolationException.class}, maxAttempts = 3)
    private int getNextSequence(String categoryPrefix, String datePart) {
        // Implementation with database sequence
    }
}
```

### 11.2 Reserved Inventory Management

#### Reservation System
- **Duration**: 15-30 minutes (configurable)
- **Cleanup**: Background job every 5 minutes
- **Reservation Table**: Separate table for tracking reservations

#### Background Job Implementation
```java
@Scheduled(fixedDelay = 300000) // 5 minutes
public void cleanupExpiredReservations() {
    List<Reservation> expired = reservationRepository.findExpiredReservations();
    expired.forEach(this::releaseReservation);
}
```

### 11.3 Pagination Implementation

#### Offset-Based Pagination
- **Default**: Standard offset/limit pagination
- **Parameters**: `page` (1-based), `pageSize` (default 10, max 50)
- **Response**: Include total count and pagination metadata

#### Cursor-Based Pagination (Future)
- **Use Case**: Large datasets with frequent updates
- **Cursor**: Encoded combination of sort key and unique identifier
- **Benefits**: Consistent results during data changes

## 12. Monitoring & Observability

### 12.1 Health Checks

#### Health Endpoints
- **Application**: `/actuator/health`
- **Database**: Custom health indicator for SQLite
- **Cache**: Health check for cache connectivity
- **External Services**: Health checks for future integrations

### 12.2 Performance Monitoring

#### Key Performance Indicators
- **API Response Time**: P50, P95, P99 percentiles
- **Database Query Performance**: Slow query detection
- **Cache Hit Rates**: Cache effectiveness metrics
- **Error Rates**: 4xx and 5xx error tracking

### 12.3 Alerting Strategy

#### Alert Categories
- **Critical**: System down, database unavailable
- **Warning**: Performance degradation, high error rates
- **Info**: Capacity thresholds, maintenance windows

---

**Document Version**: 1.0  
**Last Updated**: January 15, 2025  
**Next Review**: February 15, 2025  
**Companion Document**: business.prd.md 