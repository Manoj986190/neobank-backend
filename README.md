# NeoBank Backend 🏦

Spring Boot REST API for the NeoBank Platform.  
**PMIS Internship · Infosys Bhubaneswar DC**

This project serves as the core banking engine for NeoBank, handling secure authentication, account management, and transaction processing.

## 🛠 Tech Stack
- **Java:** 17
- **Spring Boot:** 3.5.13
- **Security:** Spring Security + JJWT 0.12.6
- **Persistence:** Spring Data JPA + MySQL 8.0
- **Documentation:** SpringDoc OpenAPI 2.8.5
- **Testing:** JUnit 5 + Mockito

## 🚀 Local Setup

### 1. Database Configuration
Ensure MySQL is running and execute the following:
```sql
CREATE DATABASE neobank_db 
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'neobank_user'@'localhost' IDENTIFIED BY 'your_password';

GRANT ALL PRIVILEGES ON neobank_db.* TO 'neobank_user'@'localhost';
```

### Configuration
Update `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/neobank_db
    username: neobank_user
    password: your_password
jwt:
  secret: your_256bit_hex_secret
  expiration: 86400000
```

### Run
```bash
./mvnw spring-boot:run
```

### Test
```bash
./mvnw test
```

## ✅ Sprint 1 Acceptance Criteria (Sign-off Ready)

### 🔐 Authentication & Security
. AC01: Registration enforces email uniqueness and password complexity.

. AC02: Login returns a valid JWT (Verified in DevTools & Swagger).

. AC03/07: JWT validation enforced; unauthorized access to protected routes returns 401/403.

. AC05: Inactive user login handling implemented.

### 💰 Banking Logic
. AC08: Account creation automatically links to the authenticated JWT userId.

. AC09: Data Isolation: Users can only view and transact on their own accounts.

. AC10: Overdraft Prevention: Debits exceeding balance return HTTP 422 Unprocessable Entity.

. AC13: Transaction color-coding logic (RED for Debit, GREEN for Credit) verified in API     response.

### 🏗 Engineering Excellence
. AC14: 24/24 Unit Tests Passed (AuthService, JwtUtil, TransactionService).

. AC15: Swagger UI Fully Functional at /swagger-ui.html with Bearer Token support.

. AC16: Clean Git history on feature/sprint1-signoff.

. AC18: Complete README setup instructions (This file).

## 📖 API Documentation
Interactive Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI Spec (JSON): docs/openapi-sprint1.json

## Seed Credentials
|       Email          |  Password |         Role        |
|----------------------|-----------|---------------------|
| admin@neobank.in     | Admin@123 | ADMIN               |
| customer1@neobank.in | Admin@123 | CUSTOMER            |
| inactive@neobank.in  | Admin@123 | CUSTOMER (inactive) |

## 🌿 Branching Strategy
```
. main: Sprint sign-off only
. develop: Integration branch
. feature/ : Daily feature work
```
