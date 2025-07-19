# Inventory Management System

A comprehensive Spring Boot application for managing inventory, products, categories, and SKUs with hierarchical category structure, advanced search capabilities, and robust API design.

## 🚀 Features

- **Hierarchical Category Management**: Support for nested categories with path enumeration
- **Product & SKU Management**: Complete product lifecycle with variant tracking
- **Advanced Search & Filtering**: Powerful search with multiple filter options
- **RESTful API Design**: Clean, versioned APIs with comprehensive documentation
- **Multi-level Caching**: Caffeine cache for optimal performance
- **Structured Logging**: JSON-formatted logs for better observability
- **Code Quality**: Comprehensive testing and static analysis
- **Code Quality**: Integrated Checkstyle, PMD, and SpotBugs
- **Comprehensive Testing**: JUnit 5 with MockMvc integration

## 🏗️ Architecture

The application follows a layered architecture pattern:

```
├── application/           # Main application and configuration
├── common/               # Shared utilities and configurations
│   ├── config/          # Spring configurations
│   ├── controller/      # Common controllers (health checks)
│   ├── exception/       # Global exception handling
│   ├── security/        # Security configurations
│   └── util/           # Utility classes
├── category/            # Category management module
│   ├── controller/     # Category REST controllers
│   ├── service/        # Category business logic
│   ├── repository/     # Category data access
│   └── model/          # Category entities and DTOs
├── product/             # Product management module
│   └── ...             # Similar structure as category
└── inventory/           # Inventory/SKU management module
    └── ...             # Similar structure as category
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Database**: SQLite (with Hibernate community dialects)
- **Cache**: Caffeine Cache
- **Security**: Spring Security (JWT ready)
- **Documentation**: OpenAPI 3 (Swagger UI)
- **Testing**: JUnit 5, MockMvc, H2 (test database)
- **Build Tool**: Maven
- **Code Quality**: Checkstyle, PMD, SpotBugs, Google Java Format

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git (for version control)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd inventory-management-system
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Run Tests

```bash
mvn test
```

### 4. Start the Application

```bash
# Development mode (with auto-reload)
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Verify Installation

Once the application is running, you can verify it's working by accessing:

- **Health Check**: http://localhost:8080/api/v1/health
- **API Documentation**: http://localhost:8080/api/swagger-ui.html
- **Actuator Endpoints**: http://localhost:8080/api/actuator/health

## 🔧 Configuration

### Environment Profiles

The application supports multiple profiles:

- **dev**: Development environment with debug logging
- **test**: Test environment with H2 in-memory database
- **prod**: Production environment with optimized settings

### Key Configuration Properties

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:sqlite:inventory.db
    
# Application Configuration
inventory:
  jwt:
    secret: your-secret-key
    expiration: 86400
  pagination:
    default-page-size: 10
    max-page-size: 50
  cache:
    categories:
      ttl: 3600
      max-size: 1000
```

### Environment Variables

- `SPRING_PROFILES_ACTIVE`: Active profile (dev/test/prod)
- `DATABASE_URL`: Database connection URL
- `JWT_SECRET`: JWT signing secret
- `SERVER_PORT`: Server port (default: 8080)
- `LOG_LEVEL`: Application log level

## 📊 API Endpoints

### Health Check Endpoints

- `GET /api/v1/health` - Basic health check
- `GET /api/v1/health/detailed` - Detailed health information
- `GET /api/v1/health/ready` - Readiness probe
- `GET /api/v1/health/live` - Liveness probe

### Future API Endpoints (Upcoming Tasks)

- `GET /api/v1/categories` - List categories
- `POST /api/v1/categories` - Create category
- `GET /api/v1/products` - List products
- `POST /api/v1/products` - Create product
- `GET /api/v1/skus` - List SKUs
- `POST /api/v1/skus` - Create SKU

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Classes

```bash
mvn test -Dtest=HealthControllerTest
```

### Test Coverage

```bash
mvn test jacoco:report
```

## 🔍 Code Quality

### Run Code Quality Checks

```bash
# Run all quality checks
mvn validate

# Run specific checks
mvn checkstyle:check
mvn pmd:check
mvn spotbugs:check
```

### Format Code

```bash
mvn fmt:format
```

## 📝 Logging

The application uses structured JSON logging with different configurations per environment:

### Development
- Console output with pretty formatting
- DEBUG level for application packages
- SQL query logging enabled

### Production
- File-based logging with rotation
- INFO level logging
- Structured JSON format for log aggregation

### Log Structure

```json
{
  "timestamp": "2025-01-15T10:30:00.000Z",
  "level": "INFO",
  "message": "Application started",
  "service": "inventory-management-system",
  "version": "1.0.0",
  "environment": "dev"
}
```

## 🚢 Deployment

### Development Deployment

```bash
# Build the application
mvn clean package

# Run with development profile
java -jar target/inventory-management-system-1.0.0.jar --spring.profiles.active=dev
```

### Production Deployment

```bash
# Build for production
mvn clean package -Pprod

# Run with production configuration
java -jar target/inventory-management-system-1.0.0.jar \
  --spring.profiles.active=prod \
  --server.port=8080 \
  --spring.datasource.url=jdbc:sqlite:/data/inventory.db
```

## 📈 Performance Optimization

### SQLite Optimizations

The application includes several SQLite optimizations:

- WAL mode for better concurrency
- Optimized cache size (64MB)
- Memory-based temporary storage
- Foreign key constraints enabled
- Optimized pragma settings

### Caching Strategy

- **Categories**: 1-hour TTL (infrequently changed)
- **Products**: 30-minute TTL (moderately changed)
- **Search Results**: 15-minute TTL (frequently invalidated)
- **SKUs**: 30-minute TTL (inventory dependent)

## 🔒 Security

Basic security configuration is in place with plans for comprehensive security in Task 006:

- CSRF protection disabled for API endpoints
- JWT authentication framework ready
- Role-based access control structure prepared
- Security headers configuration planned

## 📚 Documentation

- **API Documentation**: Available at `/api/swagger-ui.html`
- **OpenAPI Spec**: Available at `/api/v3/api-docs`
- **Actuator Endpoints**: Available at `/api/actuator`

## 🤝 Contributing

1. Follow Google Java Style guidelines
2. Ensure all tests pass
3. Run code quality checks
4. Update documentation as needed
5. Follow the established project structure

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions:

- Check the API documentation at `/api/swagger-ui.html`
- Review the health check endpoints
- Check application logs for detailed error information
- Refer to the technical and business PRD documents

## 🗺️ Roadmap

This is Task 001 of a 14-task implementation plan:

- ✅ **Task 001**: Project Setup and Foundation
- ⏳ **Task 002**: Database Schema and Migration
- ⏳ **Task 003**: Core Domain Models and Entities
- ⏳ **Task 004**: Repository Layer Implementation
- ⏳ **Task 005**: Business Service Layer
- ⏳ **Task 006**: Security and Authentication
- ⏳ **Task 007**: REST API Controllers
- ⏳ **Task 008**: Caching and Performance
- ⏳ **Task 009**: Error Handling and Logging
- ⏳ **Task 010**: API Documentation
- ⏳ **Task 011**: Testing Framework
- ⏳ **Task 012**: Feature Flags and Configuration
- ⏳ **Task 013**: Event-Driven Architecture
- ⏳ **Task 014**: System Integration and Deployment

---

**Version**: 1.0.0  
**Last Updated**: January 15, 2025  
**Next Task**: Database Schema and Migration (Task 002) 