# Inventory Management System - Project Structure

## Overview

This document provides a comprehensive overview of the Inventory Management System's project structure, explaining the purpose and organization of each directory and file. The project follows Spring Boot best practices with a layered architecture pattern for maintainability and scalability.

## 📁 Root Directory Structure

```
inventory-management-system/
├── src/                          # Source code directory
│   ├── main/                    # Application source code
│   └── test/                    # Test source code
├── target/                      # Maven build output (generated)
├── data/                        # H2 database files (runtime)
├── docs/                        # Project documentation
├── tasks/                       # Task planning documents
├── pom.xml                      # Maven project configuration
├── README.md                    # Project overview and setup
├── project-structure.md         # This file - project structure guide
├── API_DOCUMENTATION.md         # API documentation
├── business.prd.md             # Business requirements document
├── technical.prd.md            # Technical requirements document
├── progress.md                 # Development progress tracker
├── checkstyle.xml              # Checkstyle configuration
├── pmd-rules.xml              # PMD rules configuration
├── .gitignore                 # Git ignore patterns
└── .env                       # Environment variables (not tracked)
```

## 🏗️ Source Code Structure (`src/`)

### Main Application (`src/main/`)

```
src/main/
├── java/com/inventorymanagement/     # Java source code
│   ├── application/                  # Application startup and configuration
│   ├── common/                      # Shared components and utilities
│   ├── category/                    # Category management module
│   ├── product/                     # Product management module
│   └── inventory/                   # Inventory/SKU management module
└── resources/                       # Configuration and static resources
    ├── application.yml             # Main application configuration
    ├── logback-spring.xml          # Logging configuration
    └── db/migration/               # Database migration scripts
        ├── V1__Create_initial_schema.sql
        └── V2__Insert_seed_data.sql
```

### Test Code (`src/test/`)

```
src/test/
├── java/com/inventorymanagement/     # Test source code
│   ├── application/                  # Application startup tests
│   ├── common/                      # Common test utilities and base classes
│   ├── category/                    # Category module tests
│   ├── product/                     # Product module tests
│   └── inventory/                   # Inventory module tests
└── resources/                       # Test configuration
    └── application-test.yml         # Test-specific configuration
```

## 🎯 Module Architecture

Each business module follows a consistent layered architecture pattern:

### Category Module (`category/`)

```
category/
├── controller/                      # REST API controllers
│   └── CategoryController.java     # Category REST endpoints
├── service/                        # Business logic layer
│   └── CategoryService.java       # Category business operations
├── repository/                     # Data access layer
│   └── CategoryRepository.java    # Category data access
└── model/                          # Data models and DTOs
    ├── Category.java              # JPA entity
    ├── CategoryDto.java           # Data transfer object
    └── CategoryMapper.java        # Entity-DTO mapping
```

### Product Module (`product/`)

```
product/
├── controller/
│   └── ProductController.java      # Product REST endpoints
├── service/
│   └── ProductService.java        # Product business operations
├── repository/
│   └── ProductRepository.java     # Product data access
└── model/
    ├── Product.java               # JPA entity
    ├── ProductDto.java            # Data transfer object
    └── ProductMapper.java         # Entity-DTO mapping
```

### Inventory Module (`inventory/`)

```
inventory/
├── controller/
│   └── SkuController.java          # SKU REST endpoints
├── service/
│   ├── SkuService.java            # SKU business operations
│   └── InventoryService.java      # Inventory management operations
├── repository/
│   ├── SkuRepository.java         # SKU data access
│   └── InventoryTransactionRepository.java # Transaction history
└── model/
    ├── Sku.java                   # SKU JPA entity
    ├── SkuDto.java                # SKU data transfer object
    ├── SkuMapper.java             # SKU entity-DTO mapping
    └── InventoryTransaction.java  # Transaction history entity
```

### Common Module (`common/`)

```
common/
├── config/                         # Configuration classes
│   ├── DatabaseConfig.java       # Database configuration
│   ├── CacheConfig.java          # Caching configuration
│   ├── SecurityConfig.java       # Security configuration
│   ├── SwaggerConfig.java        # API documentation configuration
│   ├── CorrelationIdFilter.java  # Request correlation tracking
│   └── ApiExamplesConfig.java    # API documentation examples
├── controller/                    # Common controllers
│   ├── HealthController.java     # Health check endpoints
│   └── DatabaseExplorerController.java # Database exploration
├── exception/                     # Exception handling
│   ├── GlobalExceptionHandler.java # Global exception handler
│   ├── BusinessException.java    # Business logic exceptions
│   ├── EntityNotFoundException.java # Entity not found exception
│   ├── ValidationException.java  # Validation exceptions
│   └── InvalidRequestException.java # Invalid request exceptions
├── model/                         # Common models and DTOs
│   ├── BaseAuditEntity.java      # Base entity with audit fields
│   ├── ApiResponse.java          # Standard API response wrapper
│   ├── BulkOperationRequest.java # Bulk operation request DTO
│   ├── BulkOperationResponse.java # Bulk operation response DTO
│   ├── ErrorResponse.java        # Error response DTO
│   ├── PagedResponse.java        # Paginated response wrapper
│   └── ApiDocumentationModels.java # Documentation model examples
└── repository/
    └── BaseRepository.java        # Base repository interface
```

### Application Module (`application/`)

```
application/
└── InventoryManagementApplication.java # Main Spring Boot application class
```

## 📊 Test Structure

### Test Organization

Tests are organized to mirror the main source structure with additional test-specific utilities:

```
src/test/java/com/inventorymanagement/
├── application/
│   └── InventoryManagementApplicationTest.java # Application startup test
├── common/                                    # Test utilities and base classes
│   ├── BaseUnitTest.java                     # Base class for unit tests
│   ├── BaseIntegrationTest.java              # Base class for integration tests
│   ├── BaseApiTest.java                      # Base class for API tests
│   ├── config/
│   │   └── TestConfig.java                   # Test configuration
│   ├── controller/
│   │   └── HealthControllerTest.java         # Health controller tests
│   ├── model/                                # Common model tests
│   └── testdata/                             # Test data utilities
│       ├── TestDataFactory.java             # Test data generation
│       └── EdgeCaseIntegrationTest.java     # Edge case testing
├── category/                                 # Category module tests
│   ├── controller/
│   │   └── CategoryControllerTest.java       # Category API tests
│   ├── service/
│   │   └── CategoryServiceTest.java          # Category business logic tests
│   ├── repository/
│   │   └── CategoryRepositoryTest.java       # Category data access tests
│   └── model/
│       └── CategoryMapperTest.java           # Category mapping tests
├── product/                                  # Product module tests
│   ├── controller/
│   │   └── ProductControllerTest.java        # Product API tests
│   ├── service/
│   │   └── ProductServiceTest.java           # Product business logic tests
│   └── model/
│       └── ProductMapperTest.java            # Product mapping tests
└── inventory/                                # Inventory module tests
    ├── controller/
    │   └── SkuControllerTest.java             # SKU API tests
    ├── service/
    │   └── InventoryServiceTest.java          # Inventory business logic tests
    └── model/
        ├── SkuDtoTest.java                    # SKU DTO tests
        └── SkuMapperTest.java                 # SKU mapping tests
```

### Test Types

| Test Pattern | Purpose | Example |
|--------------|---------|---------|
| `*Test.java` | Unit tests | `CategoryServiceTest.java` |
| `*IntegrationTest.java` | Integration tests | `EdgeCaseIntegrationTest.java` |
| `*ControllerTest.java` | API endpoint tests | `CategoryControllerTest.java` |
| `*RepositoryTest.java` | Data access tests | `CategoryRepositoryTest.java` |

## 🗃️ Database Structure

### Migration Files (`src/main/resources/db/migration/`)

```
db/migration/
├── V1__Create_initial_schema.sql   # Initial database schema
└── V2__Insert_seed_data.sql        # Sample data for development
```

### Database Files (`data/`)

```
data/
├── inventory.db                    # Main H2 database file
├── inventory.db.mv.db             # H2 internal storage file
└── inventory.trace.db             # H2 trace file (if enabled)
```

## 📖 Documentation Structure

### Core Documentation

| File | Purpose |
|------|---------|
| `README.md` | Project overview, setup, and usage instructions |
| `project-structure.md` | This file - detailed project structure |
| `API_DOCUMENTATION.md` | Comprehensive API documentation |
| `business.prd.md` | Business requirements and specifications |
| `technical.prd.md` | Technical requirements and architecture |
| `progress.md` | Development progress and task completion status |

### Task Planning (`tasks/`)

```
tasks/
├── 000-task-overview.md                    # Overall task planning
├── 001-project-setup-and-foundation.md     # Foundation setup
├── 002-database-schema-and-migration.md    # Database design
├── 003-core-domain-models-and-entities.md  # Entity modeling
├── 004-repository-layer-implementation.md  # Data access layer
├── 005-business-service-layer.md           # Business logic layer
├── 006-security-and-authentication.md      # Security implementation
├── 007-rest-api-controllers.md             # API layer
├── 008-caching-and-performance.md          # Performance optimization
├── 009-error-handling-and-logging.md       # Error handling
├── 010-api-documentation.md                # API documentation
├── 011-testing-framework.md                # Testing strategy
├── 012-feature-flags-and-configuration.md  # Configuration management
├── 013-event-driven-architecture.md        # Event handling
└── 014-system-integration-and-deployment.md # Deployment strategy
```

## ⚙️ Configuration Files

### Build Configuration

| File | Purpose |
|------|---------|
| `pom.xml` | Maven project configuration, dependencies, and plugins |
| `checkstyle.xml` | Code style rules and enforcement |
| `pmd-rules.xml` | Code quality analysis rules |

### Runtime Configuration

| File | Purpose |
|------|---------|
| `application.yml` | Main application configuration |
| `application-test.yml` | Test environment configuration |
| `logback-spring.xml` | Logging configuration |
| `.env` | Environment variables (development only) |

## 🔧 Build Output (`target/`)

The `target/` directory contains Maven build artifacts (automatically generated):

```
target/
├── classes/                        # Compiled main classes
├── test-classes/                   # Compiled test classes
├── generated-sources/              # Generated source code
├── maven-status/                   # Maven build status
├── surefire-reports/              # Test execution reports
├── site/                          # Generated site documentation
└── inventory-management-system-1.0.0.jar # Executable JAR file
```

## 🎨 Architectural Patterns

### Layered Architecture

The application follows a strict layered architecture:

1. **Controller Layer** (`*Controller.java`)
   - Handles HTTP requests and responses
   - Input validation and serialization
   - API documentation annotations

2. **Service Layer** (`*Service.java`)
   - Business logic implementation
   - Transaction management
   - Cross-cutting concerns (caching, security)

3. **Repository Layer** (`*Repository.java`)
   - Data access abstraction
   - Query implementation
   - Database transaction handling

4. **Model Layer** (`model/`)
   - JPA entities for database mapping
   - DTOs for API communication
   - Mappers for entity-DTO conversion

### Package Organization Principles

- **Feature-based packaging**: Each business domain has its own package
- **Layer separation**: Clear separation between controller, service, repository, and model layers
- **Common utilities**: Shared components in the `common` package
- **Test mirroring**: Test structure mirrors main source structure

### Naming Conventions

| Component Type | Naming Pattern | Example |
|----------------|----------------|---------|
| **Entities** | `[Entity].java` | `Category.java` |
| **DTOs** | `[Entity]Dto.java` | `CategoryDto.java` |
| **Controllers** | `[Entity]Controller.java` | `CategoryController.java` |
| **Services** | `[Entity]Service.java` | `CategoryService.java` |
| **Repositories** | `[Entity]Repository.java` | `CategoryRepository.java` |
| **Mappers** | `[Entity]Mapper.java` | `CategoryMapper.java` |
| **Tests** | `[Class]Test.java` | `CategoryServiceTest.java` |

## 🚀 Development Workflow

### File Creation Guidelines

When adding new features:

1. **Create entity** in `model/[Entity].java`
2. **Create DTO** in `model/[Entity]Dto.java`
3. **Create mapper** in `model/[Entity]Mapper.java`
4. **Create repository** in `repository/[Entity]Repository.java`
5. **Create service** in `service/[Entity]Service.java`
6. **Create controller** in `controller/[Entity]Controller.java`
7. **Create tests** for each layer following naming conventions

### Code Organization Best Practices

- Keep classes focused on single responsibility
- Use dependency injection for component coupling
- Follow consistent naming conventions
- Maintain clear separation of concerns
- Document public APIs with JavaDoc
- Write comprehensive tests for all layers

## 📈 Scalability Considerations

The project structure supports:

- **Horizontal scaling**: Stateless service design
- **Module extraction**: Business modules can become microservices
- **Team collaboration**: Clear module boundaries for team ownership
- **Test automation**: Comprehensive test structure for CI/CD
- **Configuration management**: Environment-specific configurations

---

**Last Updated**: January 15, 2025  
**Version**: 1.0.0  
**Author**: Inventory Management Team 