# Order App

This is a Spring Boot application for managing orders, built with Java and containerized using Docker.

## Prerequisites

- Docker
- Docker Compose

## Getting Started

### Cloning the Repository
To get started, clone the repository:

```bash
  git clone https://github.com/luangazin/order-app.git
```
### Navigating to the Project Directory
Navigate to the project directory:
```bash
cd order-app
```

### Running the Application

To run the complete application with all its dependencies (PostgreSQL and Redis):

```bash
docker compose --profile complete up
```

This will start:
- The Order App service on port 8080
- PostgreSQL database on port 5432
- Redis on port 6379

### Services

- **Order App**: Main application service
  - URL: http://localhost:8080
- **PostgreSQL**:
  - Port: 5432
  - Database: order_app
  - Username: postgres
  - Password: postgres
- **Redis**:
  - Port: 6379

### Building from Source

If you want to build the application locally:

Run the containers:
```bash
docker compose --profile complete up --build
```

## Project Structure

- `src/`: Source code directory
- `docker-compose.yml`: Docker Compose configuration
- `Dockerfile`: Docker build configuration

## Stopping the Application

To stop all services:

```bash
docker compose --profile complete down
```

To stop and remove all data (including volumes):

```bash
docker compose --profile complete down -v
```

### Initial Data

The application comes with a pre-created Partner for testing:
- Partner ID: `1ffe19fd-cb50-4afe-b4cf-07aa691631df`

This Partner can be used for testing order creation and other API operations.
You can test the API using swagger UI at:

```
http://localhost:8080/swagger-ui/index.html
``` 
There is a swagger file in the project at root project `openapi-swagger.yaml` that can be used to import into Postman or any other API testing tool.


