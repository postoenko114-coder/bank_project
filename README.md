# Bank API

Spring Boot REST API that emulates core banking workflows: authentication, user profiles, accounts, cards, transactions, branch services, reservations, notifications and support messages.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-blue)
![Security](https://img.shields.io/badge/Security-JWT%20%2B%20OAuth2-red)
![Tests](https://img.shields.io/badge/Tests-JUnit%205%20%2B%20Mockito%20%2B%20Testcontainers-brightgreen)

## Features

- JWT authentication and Google OAuth2 login.
- Role-based access for `CLIENT` and `ADMIN`.
- Account lifecycle: open, activate, block, close and delete.
- Money operations: deposit, withdrawal, card payment and account transfer.
- Card lifecycle: create, activate, block, close, change card type and delete.
- Transaction history with filtering and client-side hiding.
- Bank branches, services, schedules, nearest-branch search and reservations.
- WebSocket/user notifications plus SMTP email support.
- Swagger/OpenAPI documentation for all controller endpoints.
- Centralized exception handling and structured application logging.
- Unit, MVC slice and Testcontainers integration tests.

## Tech Stack

- Java 17
- Spring Boot 3.4.1
- Spring Web, Spring Security 6, Spring Data JPA, Validation, WebSocket, Mail, Actuator
- JWT via `jjwt`, OAuth2 Client
- PostgreSQL, Hibernate, Flyway
- MapStruct, Lombok
- JUnit 5, Mockito, Spring Security Test, Rest Assured, Testcontainers
- Maven, Docker
- Static frontend: HTML5, CSS3, JavaScript

## Project Structure

```text
backend/
  src/main/java/com/alex/bank/
    config/          application configuration, AOP logging
    controllers/     public, client and admin REST controllers
    dto/             request/response DTO models
    exception/       global API error handling
    mapper/          MapStruct mappers
    models/          JPA entities and enums
    repositories/    Spring Data repositories
    security/        JWT, OAuth2 and ownership checks
    services/        business logic
  src/main/resources/
    db/migration/    Flyway migrations
    application*.yaml
  src/test/java/com/alex/bank/
    unit/            unit and controller tests
    integration/     Testcontainers tests
```

## Local Setup

Requirements:

- JDK 17
- Maven 3.9+
- PostgreSQL 15+ for the `local` profile
- Docker Desktop for `mvn verify` integration tests

Environment variables:

```bash
JWT_SECRET=base64-encoded-256-bit-secret
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
MAIL_USERNAME=your-smtp-username
MAIL_PASSWORD=your-smtp-password
LOCAL_DB_PASSWORD=postgres
FRONTEND_ORIGIN=http://localhost:63342
```

Run locally:

```bash
cd backend
mvn spring-boot:run
```

Default profile is `local`. The local database URL is configured in `application-local.yaml` as `jdbc:postgresql://localhost:5432/db_bank`.

## API Documentation

After starting the application:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`

## Tests

Run unit and MVC/controller tests:

```bash
cd backend
mvn test
```

Run the full suite including Testcontainers integration tests:

```bash
cd backend
mvn verify
```

`mvn verify` requires Docker Desktop to be running.

## Docker

Build and run:

```bash
cd backend
docker build -t bank-api .
docker run --env-file .env -p 8080:8080 bank-api
```

## Notes

- Main package: `com.alex.bank`
- Maven coordinates: `com.alex:bank-api`
- All controller endpoints are documented with Swagger `@Operation`.
- Client endpoints validate resource ownership for user-scoped resources.
- DTOs are used on API boundaries; sensitive password fields are write-only.
