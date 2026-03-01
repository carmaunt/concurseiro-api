# concurseiro-api

A Java Spring Boot REST API for managing exam questions (concursos) in Brazil. Provides endpoints for cataloging disciplines, subjects, examining boards, institutions, and questions with full JWT-based authentication.

## Architecture

- **Language:** Java 19 (GraalVM CE 22.3.1)
- **Framework:** Spring Boot 4.0.2
- **Build System:** Maven (Maven Wrapper `./mvnw`)
- **Database:** PostgreSQL (Replit built-in, via `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` env vars)
- **Authentication:** JWT via JJWT 0.12.6
- **Schema Management:** Hibernate `ddl-auto=update` (auto-creates/updates tables)
- **API Docs:** Springdoc OpenAPI / Swagger UI at `/swagger-ui.html`
- **Observability:** Spring Boot Actuator at `/actuator/health`

## Project Structure

```
src/main/java/br/com/concurseiro/api/
├── admin/          - Admin controllers for content management
├── catalogo/       - Catalog entities (assunto, banca, disciplina, instituicao, subassunto)
├── infra/          - Cross-cutting concerns (config, exception, response, security)
├── questoes/       - Core question management
└── usuarios/       - User management and authentication
src/main/resources/
└── application.properties - Main configuration
```

## Running

The application starts via:
```
./mvnw spring-boot:run
```
It listens on port **8080**.

## Configuration

- `APP_ADMIN_API_KEY` - Admin API key (defaults to `admin-dev-key` in dev)
- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` - Database connection (auto-provided by Replit)
- JWT secret is hardcoded in `application.properties` (change for production)

## Key Endpoints

- `POST /api/v1/auth/register` - Register user
- `POST /api/v1/auth/login` - Login and get JWT token
- `GET /api/v1/questoes` - List/filter questions (public)
- `GET /swagger-ui.html` - API documentation
- `GET /actuator/health` - Health check
