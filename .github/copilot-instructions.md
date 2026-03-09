# AI Coding Assistant Instructions for stack-app-2025-v1

## Project Overview
Modern REST API server built with Spring Boot 3.x, optimized for Oracle Cloud VM deployment. Serves multiple client types (Web/Mobile/Desktop) with standardized APIs.

## Key Architecture Patterns

### Backend (Spring Boot)
- **API Response Format**: RFC 7807 for error responses (`ProblemDetail` format)
- **Authentication**: JWT-based with Spring Security
- **Caching Strategy**: Hibernate L2 cache + Redis for query optimization
- **Database**: Oracle with JPA/Hibernate (Docker for development)
- **API Documentation**: OpenAPI/Swagger UI at `/swagger-ui.html`

### Frontend (Vue.js)
- **Theme Architecture**: Strict separation between logic (`src/core/`) and presentation (`src/themes/`)
- **UI Components**: Base components in `themes/{admin,landing}/components/` wrapping PrimeVue
- **State Management**: Pinia stores in `core/store/` for business logic
- **Layouts**: Separate layouts per theme (Avalon for admin, Genesis for landing)

## Development Workflow

### Backend Setup
```bash
./mvnw clean package  # Build project
java -jar target/app.jar  # Run locally
```

### Important Paths
- `src/main/resources/config/` - Application configuration
- `src/main/java/com/daangcool/stack/` - Core application code
- `src/main/resources/config/liquibase/` - Database migrations

### Critical Conventions

1. **Frontend Component Rules**:
   - Never import PrimeVue components directly in views
   - Always use theme-specific Base components
   - Keep business logic in `core/`, UI in `themes/`

2. **Backend Patterns**:
   - Use RFC 7807 format for all error responses
   - Configure timezone as Asia/Seoul in queries
   - Follow JHipster-style application properties structure

3. **API Development**:
   - Document all APIs with OpenAPI annotations
   - Use problem-details for error responses
   - JWT token required for secured endpoints

## Integration Points
- Redis cache integration via Hibernate L2
- MQTT messaging for IoT device communication
- Oracle Cloud VM deployment target
- Swagger UI for API documentation

## Common Tasks

### Adding New API Endpoint
1. Create controller in appropriate package
2. Add OpenAPI documentation
3. Implement RFC 7807 error handling
4. Update Swagger documentation

### Adding New UI Feature
1. Add business logic in `core/`
2. Create Base components in appropriate theme
3. Implement view using Base components
4. Add i18n messages if needed

## Project-Specific Notes
- Multiple theme support (Avalon/Genesis) with strict separation
- Centralized auth and error handling in core layer
- SCSS namespacing per theme (.avalon-theme, .genesis-theme)
- Hibernate batch/fetch size optimization settings