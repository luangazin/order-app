# Developer Guide

This document contains information for developers working on the Order App project.

## Development Setup

### Requirements

- Java 21 or higher
- Docker and Docker Compose
- Your favorite IDE (IntelliJ IDEA recommended)
- Gradle (wrapper included)

### Tech Stack

- Spring Boot 3.5.3
- Spring Data JPA
- Spring Data Redis
- PostgreSQL 17
- Redis 8
- OpenAPI/Swagger for API documentation
- TestContainers for integration testing
- Lombok for reducing boilerplate code

## Local Development

### Running Without Docker

1. Start dependencies locally:
```bash
docker compose up -d
```

2. Run the application:
```bash
./gradlew bootRun
```

### Development with IDE

1. Import the project as a Gradle project
2. Enable annotation processing for Lombok
3. Run/Debug the main application class with the following VM options:
```
-Dspring.profiles.active=local
```

### Building

```bash
./gradlew clean build
```

### Testing

Run all tests:
```bash
./gradlew test
```

The project uses TestContainers for integration tests, so Docker must be running on your machine.

## Code Style and Guidelines

- Use Java 21 features where appropriate
- Follow standard Spring Boot best practices
- Use Lombok annotations to reduce boilerplate
- Write tests for new features using TestContainers where appropriate
- Document API changes in the OpenAPI specification

## Database

### Local Development Database

- URL: jdbc:postgresql://localhost:5432/order_app
- Username: postgres
- Password: postgres

### Redis Cache

- Host: localhost
- Port: 6379

## API Development

### OpenAPI/Swagger

The API documentation is available at:
- Local: http://localhost:8080/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/v3/api-docs

### Adding New Endpoints

1. Create the controller class in the appropriate package
2. Add proper OpenAPI annotations for documentation
3. Update the openapi-swagger.yaml file if necessary
4. Write integration tests using TestContainers

### Exporting OpenAPI Specification from source code

Run the following command to generate the OpenAPI documentation from the source code and save it to `openapi-swagger.yaml`:
```bash
./gradlew generateOpenApiDocs
```


## Debugging

### Application Logs

- Logs are in ECS format when running in Docker
- Use the Spring Boot Actuator endpoints for monitoring
- Actuator endpoints are available at /actuator/*

### Common Issues

1. TestContainers failing:
   - Ensure Docker is running
   - Check Docker resource limits

2. Redis connection issues:
   - Verify Redis is running: `docker compose ps`
   - Check Redis logs: `docker compose logs redis`

3. Database connection issues:
   - Check PostgreSQL logs: `docker compose logs postgres`
   - Verify database initialization: `docker compose exec postgres psql -U postgres -d order_app`

## Continuous Integration

### Local Checks

Before pushing code, run:
```bash
./gradlew clean check
```

This will run:
- Compilation
- Unit tests
- Integration tests
- Style checks (if configured)

## Performance Testing

The application includes Spring Boot Actuator for monitoring. Key metrics are available at:
- http://localhost:8080/actuator/metrics
- http://localhost:8080/actuator/health

### Initial Data

The application comes with a pre-created Partner for testing:
- Partner ID: `1ffe19fd-cb50-4afe-b4cf-07aa691631df`

This Partner can be used for testing order creation and other API operations.