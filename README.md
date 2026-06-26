# SoftBridge Backend — Spring Boot REST API

> Software Requirements Portal · Phase 2 · Java 17 + Spring Boot 3.2

---

## 📁 Project Structure

```
softbridge-backend/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/softbridge/
    │   │   ├── SoftBridgeApplication.java          ← Entry point
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java                 ← CORS origins
    │   │   │   ├── DataInitializer.java            ← Seeds admin on startup
    │   │   │   ├── JacksonConfig.java              ← ObjectMapper bean
    │   │   │   ├── SecurityConfig.java             ← JWT + route protection
    │   │   │   └── SwaggerConfig.java              ← OpenAPI / Swagger UI
    │   │   ├── controller/
    │   │   │   ├── AuthController.java             ← /auth/**
    │   │   │   ├── RequirementController.java      ← /requirements/**
    │   │   │   └── UserController.java             ← /users/me, /admin/users/**
    │   │   ├── dto/
    │   │   │   ├── request/                        ← Input DTOs (validated)
    │   │   │   └── response/                       ← Output DTOs
    │   │   ├── entity/
    │   │   │   ├── User.java                       ← JPA entity + UserDetails
    │   │   │   └── Requirement.java                ← JPA entity
    │   │   ├── enums/
    │   │   │   ├── Role.java                       ← CLIENT | ADMIN | DEVELOPER
    │   │   │   ├── RequirementStatus.java          ← PENDING | INHOUSE | OUTSOURCE
    │   │   │   └── Priority.java                   ← LOW | MEDIUM | HIGH
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java     ← @RestControllerAdvice
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── ConflictException.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java             ← JPA + custom JPQL
    │   │   │   └── RequirementRepository.java      ← JPA + filter + search
    │   │   ├── security/
    │   │   │   ├── JwtUtil.java                    ← Generate + validate JWT
    │   │   │   └── JwtAuthenticationFilter.java    ← Bearer token interceptor
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── RequirementService.java
    │   │       ├── UserService.java
    │   │       ├── EmailService.java               ← Async email (Spring Mail)
    │   │       ├── CustomUserDetailsService.java
    │   │       └── impl/
    │   │           ├── AuthServiceImpl.java
    │   │           ├── RequirementServiceImpl.java
    │   │           └── UserServiceImpl.java
    │   └── resources/
    │       ├── application.yml                     ← Main config
    │       └── application-test.yml                ← H2 test config
    └── test/
        └── java/com/softbridge/
            ├── SoftBridgeApplicationTests.java
            ├── controller/
            │   └── AuthControllerTest.java
            └── service/
                ├── RequirementServiceTest.java
                └── UserServiceTest.java
```

---

## ⚙️ Prerequisites

| Tool      | Version  |
|-----------|----------|
| Java      | 17+      |
| Maven     | 3.9+     |
| MySQL     | 8.0+     |

---

## 🚀 Setup & Run

### 1. Create MySQL Database

```sql
CREATE DATABASE softbridge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure `application.yml`

Update these values in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/softbridge_db
    username: root
    password: YOUR_MYSQL_PASSWORD   # ← change this

  mail:
    username: your_email@gmail.com  # ← change this
    password: your_app_password     # ← Gmail App Password

jwt:
  secret: YOUR_256_BIT_HEX_SECRET  # ← change in production
```

### 3. Build & Run

```bash
# Clone / navigate to project
cd softbridge-backend

# Build (skip tests)
mvn clean package -DskipTests

# Run
java -jar target/softbridge-backend-1.0.0.jar

# OR run directly with Maven
mvn spring-boot:run
```

Server starts on: `http://localhost:8080/api`

---

## 🔐 Authentication Flow

```
Client App                    Spring Boot
    │                              │
    │  POST /api/auth/register     │
    │ ─────────────────────────►  │  validate + hash password
    │ ◄─────────────────────────  │  return JWT token
    │                              │
    │  POST /api/auth/login        │
    │ ─────────────────────────►  │  authenticate
    │ ◄─────────────────────────  │  return JWT token
    │                              │
    │  GET /api/requirements       │
    │  Authorization: Bearer <jwt> │
    │ ─────────────────────────►  │  validate JWT → set SecurityContext
    │ ◄─────────────────────────  │  return data
```

**Default Admin credentials** (seeded on first run):
- Email: `admin@softbridge.com`
- Password: `admin123`

---

## 📡 REST API Reference

Base URL: `http://localhost:8080/api`

### Auth Endpoints (Public)

| Method | Endpoint            | Description                    |
|--------|---------------------|--------------------------------|
| POST   | `/auth/register`    | Register CLIENT or DEVELOPER   |
| POST   | `/auth/login`       | Login → receive JWT            |
| POST   | `/auth/logout`      | Logout (client-side)           |

#### Register Request Body
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@company.com",
  "password": "Secret123",
  "company": "Acme Corp",
  "phone": "+91 98765 43210",
  "role": "CLIENT"
}
```

#### Login Request Body
```json
{
  "email": "jane@company.com",
  "password": "Secret123"
}
```

#### Auth Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "jane@company.com",
    "role": "CLIENT"
  }
}
```

---

### Requirement Endpoints

| Method | Endpoint                          | Role    | Description                     |
|--------|-----------------------------------|---------|---------------------------------|
| POST   | `/requirements`                   | CLIENT  | Submit new requirement          |
| GET    | `/requirements/my`                | CLIENT  | Get own submissions (paginated) |
| GET    | `/requirements/my/{id}`           | CLIENT  | Get one own submission          |
| GET    | `/requirements`                   | ADMIN   | Get all requirements (filtered) |
| GET    | `/requirements/{id}`              | ADMIN   | Get requirement by ID           |
| PUT    | `/requirements/{id}`              | ADMIN   | Update a requirement            |
| PATCH  | `/requirements/{id}/decision`     | ADMIN   | Make In-House/Outsource decision|
| DELETE | `/requirements/{id}`              | ADMIN   | Delete a requirement            |
| GET    | `/requirements/stats`             | ADMIN   | Dashboard statistics            |

#### Submit Requirement Body
```json
{
  "projectName": "Customer Analytics Platform",
  "projectType": "Web Application",
  "description": "Real-time analytics dashboard for customer behaviour tracking.",
  "timeline": "3-6 Months",
  "frontendStack": ["React", "Tailwind CSS"],
  "backendStack": ["Spring Boot", "GraphQL"],
  "databaseStack": ["PostgreSQL", "Redis"],
  "deploymentStack": ["AWS", "Docker"],
  "budget": "$15,000 – $50,000",
  "teamSize": "3–5 Developers",
  "specialFeatures": "Must include PDF export. GDPR compliant.",
  "priority": "HIGH"
}
```

#### Get All Requirements (Admin) — Query Params
```
GET /requirements?page=0&size=10&status=PENDING&query=analytics
```

#### Make Decision Body
```json
{
  "status": "INHOUSE",
  "adminNotes": "We have the skill set. Assigning to internal team."
}
```

---

### User Endpoints

| Method | Endpoint                          | Role    | Description                     |
|--------|-----------------------------------|---------|---------------------------------|
| GET    | `/users/me`                       | Any     | Get own profile                 |
| PUT    | `/users/me`                       | Any     | Update own profile              |
| PATCH  | `/users/me/password`              | Any     | Change own password             |
| GET    | `/admin/users`                    | ADMIN   | List all users (searchable)     |
| GET    | `/admin/users/{id}`               | ADMIN   | Get user by ID                  |
| PUT    | `/admin/users/{id}`               | ADMIN   | Update a user                   |
| PATCH  | `/admin/users/{id}/toggle-active` | ADMIN   | Activate / deactivate user      |
| DELETE | `/admin/users/{id}`               | ADMIN   | Delete a user                   |

---

## 📊 Response Envelope

All responses follow this structure:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-05-20T14:30:00"
}
```

Paginated responses:
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

---

## 🗄️ Database Schema

Tables auto-created by Hibernate on first run (`ddl-auto: update`).

### `users`
| Column      | Type          | Notes                    |
|-------------|---------------|--------------------------|
| id          | BIGINT PK     | Auto-increment           |
| first_name  | VARCHAR(100)  |                          |
| last_name   | VARCHAR(100)  |                          |
| email       | VARCHAR(255)  | UNIQUE                   |
| password    | VARCHAR(255)  | BCrypt hashed            |
| company     | VARCHAR(200)  | nullable                 |
| phone       | VARCHAR(20)   | nullable                 |
| role        | ENUM          | CLIENT / ADMIN / DEVELOPER|
| active      | BOOLEAN       | default true             |
| created_at  | TIMESTAMP     |                          |
| updated_at  | TIMESTAMP     |                          |

### `requirements`
| Column           | Type          | Notes                      |
|------------------|---------------|----------------------------|
| id               | VARCHAR(20) PK| Format: REQ-000001         |
| client_id        | BIGINT FK     | → users.id                 |
| project_name     | VARCHAR(300)  |                            |
| project_type     | VARCHAR(100)  |                            |
| description      | TEXT          |                            |
| timeline         | VARCHAR(50)   | nullable                   |
| frontend_stack   | TEXT          | JSON array string          |
| backend_stack    | TEXT          | JSON array string          |
| database_stack   | TEXT          | JSON array string          |
| deployment_stack | TEXT          | JSON array string          |
| budget           | VARCHAR(100)  | nullable                   |
| team_size        | VARCHAR(100)  | nullable                   |
| special_features | TEXT          | nullable                   |
| priority         | ENUM          | LOW / MEDIUM / HIGH        |
| status           | ENUM          | PENDING / INHOUSE / OUTSOURCE|
| admin_notes      | TEXT          | nullable                   |
| submitted_at     | TIMESTAMP     |                            |
| updated_at       | TIMESTAMP     |                            |
| decided_at       | TIMESTAMP     | nullable                   |

---

## 🧪 Running Tests

```bash
# All tests (uses H2 in-memory DB)
mvn test

# Specific test class
mvn test -Dtest=RequirementServiceTest

# With coverage report
mvn test jacoco:report
```

---

## 📖 Swagger UI

After starting the server, visit:

```
http://localhost:8080/api/swagger-ui.html
```

- Click **Authorize** → Enter `Bearer <your_jwt_token>`
- All endpoints are documented with request/response schemas

---

## 🌍 CORS Configuration

Allowed origins (configure in `application.yml`):
```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:3000   # React dev server
      - http://localhost:5173   # Vite dev server
```

---

## 📧 Email Configuration (Gmail)

1. Enable 2-Factor Authentication on your Gmail account
2. Go to **Google Account → Security → App passwords**
3. Generate an App Password for "Mail"
4. Use that password in `spring.mail.password`

---

## 🔒 Security Notes

- Passwords hashed with **BCrypt** (strength 10)
- JWT tokens expire in **24 hours** (configurable)
- Admin endpoints protected by `@PreAuthorize("hasRole('ADMIN')")`
- Client can only access own requirements
- CORS restricted to configured origins

---

## 🗺️ Planned Phase 3 Additions

- [ ] Redis token blacklist for true logout
- [ ] Refresh token endpoint
- [ ] File attachment upload (Cloudinary)
- [ ] Admin comments / notes thread per requirement
- [ ] Client submission history pagination UI
- [ ] PDF export of requirement document
- [ ] Docker + docker-compose.yml
- [ ] AWS EC2 deployment scripts

---

*SoftBridge Backend — Built by Dhanusu · May 2026*
