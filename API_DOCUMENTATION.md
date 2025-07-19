# Inventory Management System API Documentation

## Overview

The Inventory Management System provides a comprehensive REST API for managing hierarchical categories, products, SKUs, and inventory operations. The API is built using Spring Boot 3.x with OpenAPI 3.0 specification and includes advanced features like caching, comprehensive error handling, and structured responses.

## API Documentation Access

### Swagger UI
- **Development**: http://localhost:8080/api/swagger-ui.html
- **Production**: https://api.inventorymanagement.com/api/swagger-ui.html

### OpenAPI Specification
- **JSON Format**: http://localhost:8080/api/v3/api-docs
- **YAML Format**: http://localhost:8080/api/v3/api-docs.yaml

## Getting Started

The API provides open access to all inventory management endpoints. You can start making requests immediately:

```bash
# Example: Get all categories
curl -X GET http://localhost:8080/api/v1/categories

# Example: Get all products
curl -X GET http://localhost:8080/api/v1/products

# Example: Get all SKUs
curl -X GET http://localhost:8080/api/v1/skus
```

## API Endpoints

### Category Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/categories` | Create category |
| GET | `/api/v1/categories` | List categories |
| GET | `/api/v1/categories/{id}` | Get category by ID |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Delete category |
| GET | `/api/v1/categories/{id}/children` | Get child categories |
| GET | `/api/v1/categories/{id}/products` | Get category products |
| POST | `/api/v1/categories/bulk` | Bulk operations |
| GET | `/api/v1/categories/search` | Search categories |

### Product Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List products |
| GET | `/api/v1/products/{id}` | Get product by ID |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |
| GET | `/api/v1/products/search` | Search products |
| POST | `/api/v1/products/bulk` | Bulk operations |

### SKU Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/skus` | Create SKU |
| GET | `/api/v1/skus` | List SKUs |
| GET | `/api/v1/skus/{id}` | Get SKU by ID |
| PUT | `/api/v1/skus/{id}` | Update SKU |
| DELETE | `/api/v1/skus/{id}` | Delete SKU |
| GET | `/api/v1/skus/search` | Search SKUs |
| POST | `/api/v1/skus/bulk` | Bulk operations |

### Inventory Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/skus/{id}/inventory/adjust` | Adjust inventory |
| GET | `/api/v1/skus/{id}/inventory/transactions` | Get inventory history |
| POST | `/api/v1/skus/{id}/inventory/reserve` | Reserve inventory |
| POST | `/api/v1/skus/{id}/inventory/release` | Release reservation |
| GET | `/api/v1/inventory/low-stock` | Get low stock items |
| GET | `/api/v1/inventory/reports` | Get inventory reports |

### Health & Monitoring

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/health` | Basic health check |
| GET | `/api/v1/health/detailed` | Detailed health info |
| GET | `/api/v1/health/ready` | Readiness probe |
| GET | `/api/v1/health/live` | Liveness probe |

## Request/Response Format

### Standard Response Structure

All API responses follow a consistent structure:

```json
{
  "status": "success|error",
  "message": "Human-readable message",
  "data": {}, // Response data (success only)
  "timestamp": "2025-01-15T15:00:00",
  "path": "/api/v1/categories",
  "method": "GET",
  "correlationId": "abc123-def456-ghi789"
}
```

### Error Response Structure

```json
{
  "status": "error",
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-01-15T15:00:00",
  "path": "/api/v1/categories",
  "method": "POST",
  "status_code": 400,
  "correlationId": "abc123-def456-ghi789",
  "traceId": "trace123456",
  "spanId": "span789012",
  "validationErrors": [
    {
      "field": "name",
      "rejectedValue": "",
      "message": "Name is required"
    }
  ]
}
```

### Pagination

List endpoints support pagination:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "empty": false,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  }
}
```

## Query Parameters

### Pagination Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-based) |
| `size` | integer | 20 | Page size (1-100) |
| `sort` | string | `id,asc` | Sort field and direction |

### Common Filter Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `name` | string | Filter by name (partial match) |
| `isActive` | boolean | Filter by active status |
| `createdAfter` | date | Filter by creation date |
| `createdBefore` | date | Filter by creation date |

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid request data |

| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 422 | Unprocessable Entity - Business logic error |
| 500 | Internal Server Error - Server error |

## Caching

The API implements intelligent caching:

- **Categories**: 1 hour TTL
- **Products**: 30 minutes TTL
- **SKUs**: 30 minutes TTL
- **Search Results**: 15 minutes TTL

Cache headers are included in responses:
- `Cache-Control`: Caching directives
- `ETag`: Entity tag for cache validation
- `Last-Modified`: Last modification time

## Error Handling

### Common Error Codes

| Code | Description |
|------|-------------|
| `VALIDATION_ERROR` | Request validation failed |
| `ENTITY_NOT_FOUND` | Requested resource not found |
| `DUPLICATE_ENTITY` | Resource already exists |
| `INSUFFICIENT_STOCK` | Not enough inventory |


### Validation Rules

#### Category Validation
- `name`: Required, max 255 characters
- `description`: Optional, max 1000 characters
- `sortOrder`: Non-negative integer
- `parentId`: Must reference existing category

#### Product Validation
- `name`: Required, max 255 characters
- `price`: Positive decimal
- `categoryId`: Must reference existing category
- `sku`: Must be unique

## Examples

### Create Category
```bash
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "sortOrder": 1,
    "isActive": true
  }'
```

### Search Products
```bash
curl -X GET "http://localhost:8080/api/v1/products/search?q=laptop&page=0&size=20"
```

### Bulk Create Categories
```bash
curl -X POST http://localhost:8080/api/v1/categories/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "CREATE",
    "items": [
      {
        "name": "Smartphones",
        "description": "Mobile phones",
        "parentId": 1
      },
      {
        "name": "Tablets",
        "description": "Tablet computers",
        "parentId": 1
      }
    ]
  }'
```

## Testing

### Using Swagger UI

1. Navigate to the Swagger UI: http://localhost:8080/api/swagger-ui.html
2. Click "Authorize" button
3. Enter: `Bearer YOUR_ACCESS_TOKEN`
4. Test endpoints directly in the browser

### Using Postman

1. Import the OpenAPI specification: http://localhost:8080/api/v3/api-docs
2. Set up environment variables for base URL and token
3. Use the pre-configured requests

## Support

For API support and questions:
- **Email**: support@inventorymanagement.com
- **Documentation**: https://docs.inventorymanagement.com
- **Status Page**: https://status.inventorymanagement.com

## Changelog

### Version 1.0.0 (2025-01-15)
- Initial API release
- Category, Product, SKU management
- Inventory operations
- Comprehensive error handling
- Caching optimization
- OpenAPI 3.0 documentation 