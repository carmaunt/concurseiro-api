# concurseiro-api

A Java Spring Boot REST API for managing exam questions (concursos) in Brazil. Provides endpoints for cataloging disciplines, subjects, examining boards, institutions, and questions with full JWT-based authentication.

## Architecture

- **Language:** Java 19 (GraalVM CE 22.3.1)
- **Framework:** Spring Boot 4.0.2
- **Build System:** Maven (Maven Wrapper `./mvnw`)
- **Database:** PostgreSQL (Replit built-in, via `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` env vars)
- **Authentication:** JWT via JJWT 0.12.6
- **Schema Management:** Hibernate `ddl-auto=update` in dev, `validate` in prod
- **API Docs:** Springdoc OpenAPI / Swagger UI at `/swagger-ui.html`
- **Observability:** Spring Boot Actuator (`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`)

## Project Structure

```
src/main/java/br/com/concurseiro/api/
├── admin/              - Admin controllers (questoes CRUD)
├── catalogo/           - Catalog entities (assunto, banca, disciplina, instituicao, subassunto)
│   └── */controller/   - Public + Admin controllers per entity
│   └── */service/      - Business logic
│   └── */repository/   - Spring Data JPA repos
│   └── */model/        - JPA entities
├── infra/
│   ├── config/         - SecurityConfig, OpenApiConfig, CorsConfig, RequestLoggingFilter
│   ├── exception/      - ApiExceptionHandler (RFC 7807 error responses)
│   ├── response/       - ApiResponse wrapper
│   └── security/       - JwtAuthFilter, JwtService, RateLimitFilter, UserDetailsServiceImpl
├── questoes/           - Core question management (model, DTO, service, controller, repository, specification)
└── usuarios/           - User management (model, DTO, service, controller, repository)
src/main/resources/
├── application.properties      - Dev configuration
└── application-prod.properties - Production overrides (ddl-auto=validate, swagger disabled)
```

## Running

The application starts via:
```
./mvnw spring-boot:run
```
It listens on port **8080**.

## Configuration

### Environment Variables
- `JWT_SECRET` - JWT signing secret (required, no default)
- `APP_ADMIN_API_KEY` - Admin API key (defaults to `admin-dev-key` in dev)
- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE` - Database connection (auto-provided by Replit)

### Profiles
- **default (dev):** `ddl-auto=update`, Swagger enabled, CORS allows `*`
- **prod:** `ddl-auto=validate`, Swagger disabled, restrictive CORS

## Key Endpoints

### Public
- `POST /api/v1/auth/register` - Register user
- `POST /api/v1/auth/login` - Login and get JWT token
- `GET /api/v1/questoes` - List/filter questions (paginated, many filter params)
- `GET /api/v1/questoes/{idQuestion}` - Get single question
- `GET /api/v1/catalogo/disciplinas` - List disciplines
- `GET /api/v1/catalogo/bancas` - List exam boards
- `GET /api/v1/catalogo/instituicoes` - List institutions
- `GET /api/v1/catalogo/disciplinas/{id}/assuntos` - List subjects
- `GET /api/v1/catalogo/assuntos/{id}/subassuntos` - List sub-subjects

### Admin (requires JWT + ADMIN role or API key)
- `POST/PUT/DELETE /api/v1/admin/questoes/{idQuestion}` - CRUD questions
- `POST/PUT/DELETE /api/v1/admin/catalogo/disciplinas/{id}` - CRUD disciplines
- `POST/PUT/DELETE /api/v1/admin/catalogo/bancas/{id}` - CRUD exam boards
- `POST/PUT/DELETE /api/v1/admin/catalogo/instituicoes/{id}` - CRUD institutions
- `POST /api/v1/admin/catalogo/assuntos` - Create subject
- `POST /api/v1/admin/catalogo/subassuntos` - Create sub-subject
- `GET /api/v1/admin/usuarios` - List users (paginated)
- `PATCH /api/v1/admin/usuarios/{id}/ativar` - Activate user

### Infrastructure
- `GET /swagger-ui.html` - API documentation
- `GET /actuator/health` - Health check
- `GET /actuator/info` - App info (version, DB status, runtime)

## Security Features

- JWT authentication with configurable secret
- Rate limiting on login endpoint (brute-force protection)
- Request logging with MDC (requestId, X-Request-Id response header)
- Bean-injected PasswordEncoder (BCrypt)
- Input validation with `@Valid` on all request bodies
- RFC 7807 error responses for validation, constraint, auth, and parse errors
- Database indexes on frequently queried fields
