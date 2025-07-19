# Inventory Management System

A comprehensive Spring Boot application for managing inventory, products, categories, and SKUs with hierarchical category structure, advanced search capabilities, and robust API design.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#️-technology-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Code Quality](#-code-quality)
- [Database](#-database)
- [Logging](#-logging)
- [Performance](#-performance)
- [Security](#-security)
- [Deployment](#-deployment)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [Support](#-support)

## 🚀 Features

### Core Functionality
- **Hierarchical Category Management**: Support for nested categories with path enumeration pattern
- **Product & SKU Management**: Complete product lifecycle with variant tracking and inventory control
- **Advanced Search & Filtering**: Powerful search capabilities with multiple filter options
- **Inventory Tracking**: Real-time inventory level monitoring and transaction history
- **Bulk Operations**: Efficient bulk create, update, and delete operations

### Technical Features
- **RESTful API Design**: Clean, versioned APIs with comprehensive OpenAPI documentation
- **Multi-level Caching**: Caffeine cache for optimal performance with configurable TTL
- **Structured Logging**: JSON-formatted logs with correlation IDs for observability
- **Database Migrations**: Flyway-managed database schema versioning
- **Comprehensive Testing**: JUnit 5 with integration and unit tests
- **Code Quality**: Integrated Checkstyle, PMD, and SpotBugs analysis
- **Performance Monitoring**: Built-in metrics and health check endpoints

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.2.1 |
| **Language** | Java | 17 |
| **Database** | H2 Database | (embedded) |
| **Migration** | Flyway | Latest |
| **Caching** | Caffeine Cache | Latest |
| **Documentation** | OpenAPI 3 | 2.3.0 |
| **Testing** | JUnit 5, MockMvc, AssertJ | Latest |
| **Build Tool** | Maven | 3.6+ |
| **Code Quality** | Checkstyle, PMD, SpotBugs | Latest |

## 📋 Prerequisites

Ensure you have the following installed on your system:

- **Java 17 or higher** - [Download Java](https://adoptium.net/)
- **Maven 3.6 or higher** - [Download Maven](https://maven.apache.org/download.cgi)
- **Git** - [Download Git](https://git-scm.com/downloads)

### Verify Installation

```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check Git version
git --version
```

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd inventory-management-system
```

### 2. Build the Project

```bash
# Clean and compile
mvn clean compile

# Or build everything including tests
mvn clean package
```

### 3. Run Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report
```

### 4. Start the Application

```bash
# Development mode (with auto-reload)
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run the compiled JAR
java -jar target/inventory-management-system-1.0.0.jar
```

### 5. Verify Installation

Once running, verify the application is working:

| Endpoint | URL | Description |
|----------|-----|-------------|
| **Health Check** | http://localhost:8080/api/v1/health | Basic health status |
| **API Documentation** | http://localhost:8080/api/swagger-ui.html | Interactive API docs |
| **H2 Console** | http://localhost:8080/api/h2-console | Database console |
| **Actuator** | http://localhost:8080/api/actuator/health | Detailed health info |

**Default H2 Database Connection:**
- **JDBC URL**: `jdbc:h2:file:./data/inventory`
- **Username**: `sa`
- **Password**: (empty)

## 🔧 Configuration

### Environment Profiles

The application supports multiple deployment profiles:

| Profile | Purpose | Database | Logging |
|---------|---------|----------|---------|
| **dev** | Development | H2 file-based | Console + DEBUG |
| **test** | Testing | H2 in-memory | Console + INFO |
| **prod** | Production | H2 file-based | File + INFO |

### Configuration Files

- `src/main/resources/application.yml` - Main configuration
- `src/test/resources/application-test.yml` - Test-specific settings

### Key Configuration Properties

```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:file:./data/inventory
    driver-class-name: org.h2.Driver
    username: sa
    password:

# Application Configuration
inventory:
  pagination:
    default-page-size: 10
    max-page-size: 50
```

### Environment Variables

You can override configuration using environment variables:

```bash
# Set active profile
export SPRING_PROFILES_ACTIVE=prod

# Override server port
export SERVER_PORT=9090

# Override database URL
export SPRING_DATASOURCE_URL=jdbc:h2:file:/data/inventory-prod
```

## 📊 API Documentation

### Interactive Documentation

The application provides comprehensive API documentation through Swagger UI:

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/api/v3/api-docs.yaml

### Core API Endpoints

| Category | Method | Endpoint | Description |
|----------|--------|----------|-------------|
| **Health** | GET | `/api/v1/health` | Application health check |
| **Categories** | GET | `/api/v1/categories` | List all categories |
| **Categories** | POST | `/api/v1/categories` | Create new category |
| **Categories** | GET | `/api/v1/categories/{id}` | Get category by ID |
| **Products** | GET | `/api/v1/products` | List all products |
| **Products** | POST | `/api/v1/products` | Create new product |
| **SKUs** | GET | `/api/v1/skus` | List all SKUs |
| **SKUs** | POST | `/api/v1/skus` | Create new SKU |

### API Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/categories",
  "correlationId": "abc123"
}
```

Error responses include detailed information:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input provided",
    "details": ["Name is required", "Price must be positive"]
  },
  "timestamp": "2025-01-15T10:30:00Z",
  "path": "/api/v1/products",
  "correlationId": "abc123"
}
```

## 🧪 Testing

### Test Structure

The project includes comprehensive testing:

```
src/test/java/
├── application/          # Application startup tests
├── category/            # Category module tests
│   ├── controller/     # API endpoint tests
│   ├── service/       # Business logic tests
│   └── repository/    # Data access tests
├── product/            # Product module tests
├── inventory/          # Inventory module tests
└── common/            # Shared test utilities
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CategoryServiceTest

# Run specific test method
mvn test -Dtest=CategoryServiceTest#testCreateCategory

# Run tests with coverage
mvn test jacoco:report

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run only unit tests
mvn test -Dtest="*Test" -Dtest.exclude="*IntegrationTest"
```

### Test Categories

| Test Type | Pattern | Purpose |
|-----------|---------|---------|
| **Unit Tests** | `*Test.java` | Test individual components |
| **Integration Tests** | `*IntegrationTest.java` | Test component interactions |
| **API Tests** | `*ControllerTest.java` | Test REST endpoints |
| **Repository Tests** | `*RepositoryTest.java` | Test data access |

### Test Configuration

Tests use a separate H2 in-memory database configured in `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 🔍 Code Quality

### Static Analysis Tools

The project enforces code quality through multiple tools:

| Tool | Purpose | Configuration |
|------|---------|---------------|
| **Checkstyle** | Code style compliance | `checkstyle.xml` |
| **PMD** | Code quality analysis | `pmd-rules.xml` |
| **SpotBugs** | Bug pattern detection | Maven plugin config |
| **Google Java Format** | Code formatting | Maven plugin |

### Running Quality Checks

```bash
# Run all quality checks
mvn validate

# Run specific checks
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check

# Format code
mvn fmt:format

# Generate quality reports
mvn site
```

### Quality Metrics

The project maintains:
- **Code Coverage**: > 80% line coverage
- **Checkstyle**: Zero violations
- **PMD**: Zero priority 1-3 violations
- **SpotBugs**: Zero high-priority bugs

## 💾 Database

### Database Schema

The application uses H2 database with Flyway migrations for schema management:

```
data/
├── inventory.db        # Main database file
├── inventory.db.mv.db  # H2 internal file
└── inventory.trace.db  # H2 trace file (if enabled)
```

### Schema Overview

| Table | Purpose | Key Features |
|-------|---------|--------------|
| **categories** | Product categories | Hierarchical with path enumeration |
| **products** | Product information | Links to categories |
| **skus** | Stock keeping units | Product variants with inventory |
| **inventory_transactions** | Inventory movements | Audit trail for stock changes |

### Database Migrations

Migrations are located in `src/main/resources/db/migration/`:

- `V1__Create_initial_schema.sql` - Initial schema creation
- `V2__Insert_seed_data.sql` - Sample data for development

### Managing Database

```bash
# View current schema
# Connect to H2 console at http://localhost:8080/api/h2-console

# Reset database (development only)
rm -rf data/inventory*
mvn spring-boot:run
```

## 📝 Logging

### Logging Configuration

The application uses Logback with structured JSON logging:

- **Development**: Console output with readable format
- **Production**: File-based logging with JSON format
- **Test**: Console output with WARN level

### Log Structure

```json
{
  "timestamp": "2025-01-15T10:30:00.000Z",
  "level": "INFO",
  "logger": "com.inventorymanagement.category.service.CategoryService",
  "message": "Category created successfully",
  "correlationId": "abc123",
  "userId": "user123",
  "service": "inventory-management-system",
  "version": "1.0.0"
}
```

### Log Levels

| Level | Usage | Examples |
|-------|-------|----------|
| **ERROR** | Application errors | Database failures, exceptions |
| **WARN** | Warning conditions | Deprecated API usage, retries |
| **INFO** | General information | Service startup, business events |
| **DEBUG** | Detailed debugging | SQL queries, method entry/exit |

## ⚡ Performance

### Caching Strategy

The application implements multi-level caching:

| Cache | TTL | Max Size | Purpose |
|-------|-----|----------|---------|
| **Categories** | 1 hour | 1000 | Category hierarchy |
| **Products** | 30 min | 5000 | Product catalog |
| **Search Results** | 15 min | 2000 | Search query results |

### Performance Targets

- **API Response Time**: < 2 seconds for 95% of requests
- **Database Queries**: Optimized with proper indexing
- **Memory Usage**: < 512MB heap for typical workloads
- **Startup Time**: < 30 seconds

### Monitoring

Monitor performance through:
- Spring Boot Actuator endpoints
- Application logs with timing information
- JVM metrics via Actuator

## 🔒 Security

### Current Security Features

- **CSRF Protection**: Disabled for API endpoints
- **CORS Configuration**: Configured for cross-origin requests
- **Input Validation**: Bean validation on all DTOs
- **Error Handling**: Secure error responses without sensitive data

### Planned Security Features

- **JWT Authentication**: Token-based authentication
- **Role-Based Access Control**: Admin, Manager, User roles
- **API Rate Limiting**: Request throttling
- **Security Headers**: HSTS, CSP, X-Frame-Options

### Security Configuration

Basic security is configured in `SecurityConfig.java` with plans for expansion.

## 🚢 Deployment

### Development Deployment

```bash
# Build the application
mvn clean package

# Run with development profile
java -jar target/inventory-management-system-1.0.0.jar \
  --spring.profiles.active=dev
```

### Production Deployment

```bash
# Build for production
mvn clean package -Pprod

# Run with production configuration
java -jar target/inventory-management-system-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080 \
  --spring.datasource.url=jdbc:h2:file:/data/inventory
```

### Docker Deployment (Planned)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/inventory-management-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 📁 Project Structure

For detailed information about the project structure, see [project-structure.md](project-structure.md).

```
inventory-management-system/
├── src/main/java/com/inventorymanagement/
│   ├── application/     # Main application class
│   ├── category/       # Category management
│   ├── product/        # Product management
│   ├── inventory/      # Inventory/SKU management
│   └── common/         # Shared components
├── src/main/resources/
│   ├── db/migration/   # Database migrations
│   └── application.yml # Configuration
├── src/test/           # Test files
└── docs/               # Documentation
```

## 🤝 Contributing

### Development Guidelines

1. **Code Style**: Follow Google Java Style guidelines
2. **Testing**: Ensure minimum 80% test coverage
3. **Documentation**: Update API documentation for changes
4. **Quality**: All quality checks must pass
5. **Git**: Use conventional commit messages

### Development Workflow

```bash
# 1. Create feature branch
git checkout -b feature/new-feature

# 2. Make changes and test
mvn test

# 3. Run quality checks
mvn validate

# 4. Commit changes
git commit -m "feat: add new feature"

# 5. Push and create PR
git push origin feature/new-feature
```

### Code Review Checklist

- [ ] Tests pass and coverage maintained
- [ ] Code quality checks pass
- [ ] API documentation updated
- [ ] Configuration documented
- [ ] Error handling implemented

## 🆘 Support

### Getting Help

1. **API Documentation**: http://localhost:8080/api/swagger-ui.html
2. **Health Checks**: http://localhost:8080/api/v1/health
3. **Application Logs**: Check console output or log files
4. **Database Console**: http://localhost:8080/api/h2-console

### Common Issues

| Issue | Solution |
|-------|----------|
| **Port 8080 in use** | Use `-Dserver.port=9090` or kill process |
| **Database locked** | Stop application and remove `.lock` files |
| **Tests failing** | Run `mvn clean test` to ensure clean state |
| **Build errors** | Check Java 17 is being used |

### Project Documentation

- **Technical PRD**: [technical.prd.md](technical.prd.md)
- **Business PRD**: [business.prd.md](business.prd.md)
- **API Documentation**: [API_DOCUMENTATION.md](API_documentation.md)
- **Progress Tracking**: [progress.md](progress.md)

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for details.

## 🗺️ Roadmap

This project follows a 14-task implementation plan:

- ✅ **Tasks 001-005**: Foundation, Database, Models, Repositories, Services
- ⏳ **Tasks 006-010**: Security, APIs, Caching, Error Handling, Documentation
- ⏳ **Tasks 011-014**: Testing, Configuration, Events, Deployment

---

**Version**: 1.0.0  
**Last Updated**: January 15, 2025  
**Author**: Inventory Management Team 