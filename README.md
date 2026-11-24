# Bycap Commercial Payment Gateway (v2.0.0)

**Bycap** is an enterprise cloud & NFC contactless payment gateway inspired by Wompi, built with **Java 17**, **Spring Boot 3**, **Spring Data JPA (PostgreSQL / H2)**, and a modern **HTML5 / CSS3 / Vanilla JavaScript** commercial dashboard.

## Architecture & Design Principles
- **Clean Architecture & Layered Design**: `controller` -> `service` -> `dao` -> `model` -> `dto`.
- **SOLID & GRASP Patterns**:
  - **Strategy Pattern** (`PaymentStrategy`): Extensible payment adapters for `CARD`, `NFC`, `PSE`, and `NEQUI` without modifying core payment processing logic (Open/Closed Principle).
  - **Pure Fabrication** (`SignatureVerificationService`): Isolates SHA-256 webhook cryptographic verification from domain entities.
  - **Information Expert** (`Transaction`, `Employee`): Domain entities enforce business invariants (minimum amount, status lifecycle, email format).
  - **Repository / DAO Pattern**: Seamless JPA persistence supporting both **PostgreSQL** and **H2** databases.

## Database Configuration
By default, the application starts out-of-the-box using an embedded **H2 database** (with automatic schema creation and realistic demo data seeding) so you can run it immediately with zero external setup.

To connect to **PostgreSQL**, edit `src/main/resources/application.properties` or pass environment variables:
```bash
export SPRING_PROFILES_ACTIVE=postgres
export DB_URL=jdbc:postgresql://localhost:5432/bycap_db
export DB_USER=postgres
export DB_PASS=your_password
```
A complete PostgreSQL DDL script is also included at `src/main/resources/schema-postgres.sql`.

## How to Run
1. Build and run with Maven:
   ```bash
   mvn spring-boot:run
   ```
2. Open your browser and visit:
   - **Commercial Dashboard**: `http://localhost:8080`
   - **H2 Database Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bycapdb`, User: `sa`, Password: empty)
   - **Health Check API**: `http://localhost:8080/api/health`
