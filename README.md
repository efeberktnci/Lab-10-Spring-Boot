# 🔐 Security of Web Applications – Project

A comprehensive Spring Boot application implementing authentication, authorization, and secure access control for the Security of Web Applications course.

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technologies](#️-technologies)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Authentication & Authorization](#-authentication--authorization)
- [Testing Guide](#-testing-guide-mvc-demo)
- [Testing & CI/CD](#-testing--cicd-lab-14)
- [Lab Status](#-lab-status)
- [Security Features](#-security-features)
- [Environment Variables](#-environment-variables)

---

## 🎯 Overview

This project demonstrates secure web application development practices, focusing on:

- HTTP fundamentals and request-response flow
- Session-based authentication with Spring Security
- Role-based access control (RBAC)
- Password security with BCrypt hashing
- CSRF protection for form submissions
- Secure database layer with Flyway migrations
- User-owned secure CRUD (Notes) with strict data isolation
- Security logging + safe error handling (no stack traces to users)
- HTTP security headers (CSP, nosniff, frame options, referrer policy)

---

## ✨ Features

### 🔑 Authentication

- ✅ User registration with server-side validation (email, duplicates)
- ✅ Password policy enforcement (strong password + blacklist)
- ✅ Passwords stored hashed using BCrypt (never plain text)
- ✅ Secure login handled by Spring Security (session-based)
- ✅ CSRF protection enabled for all POST forms
- ✅ Session invalidation on logout

### 🛡️ Authorization

- ✅ Role-based access control (ROLE_USER, ROLE_ADMIN)
- ✅ Protected endpoints enforced by Spring Security config
- ✅ Method-level protection using @PreAuthorize
- ✅ Admin registration requires secret key
- ✅ Safe 401/403 handling (HTML redirect vs JSON status)

### 📝 Secure Notes (Lab 12)

- ✅ Authenticated users can create/edit/delete notes
- ✅ Strict user data isolation enforced using user_id
- ✅ User B cannot view/update/delete User A's notes

### 📊 Database

- ✅ SQLite database with Flyway migrations
- ✅ Users table: username, email (unique), password hash, role
- ✅ Notes table: includes user_id foreign key (ownership)
- ✅ Safe DB access via JPA + prepared statements (JdbcTemplate)

### 🧾 Logging + Error Handling (Lab 13)

- ✅ Failed login attempts are logged (no password/token logging)
- ✅ Unauthorized (401) access attempts are logged
- ✅ Forbidden (403) access attempts are logged
- ✅ Global error handler returns safe error responses (no stack trace)

### 🧱 HTTP Security Headers (Lab 13/14)

- ✅ X-Content-Type-Options: nosniff
- ✅ X-Frame-Options
- ✅ Content-Security-Policy
- ✅ Referrer-Policy

---

## 🛠️ Technologies

| Category | Technology |
|----------|------------|
| Language | Java 22 |
| Framework | Spring Boot 3.5.x |
| Security | Spring Security (Session-based) |
| Database | SQLite + Spring Data JPA |
| Migrations | Flyway |
| Template Engine | Thymeleaf |
| Build Tool | Maven |

---

## 🚀 Getting Started

### Prerequisites

- Java 22 or higher
- Maven 3.6+

### Installation

**1) Clone the repository**

```bash
git clone <REPOSITORY_URL>
cd lab10
```

**2) Create `.env` file in the project root**

```properties
DB_URL=jdbc:sqlite:database.db
DB_DRIVER=org.sqlite.JDBC
HIBERNATE_DIALECT=org.hibernate.community.dialect.SQLiteDialect
ADMIN_REGISTER_SECRET=CHANGE_ME_123
```

**3) Run the application**

```bash
mvn spring-boot:run
```

Or using Maven wrapper:

```bash
./mvnw spring-boot:run
```

**4) Open in browser**

```
http://localhost:8080
```

---

## 📁 Project Structure

```
src/main/java/com/berk/lab10
├── config/
│   ├── GlobalExceptionHandler.java
│   ├── LoggingAccessDeniedHandler.java
│   ├── LoggingAuthenticationEntryPoint.java
│   ├── SecurityConfig.java
│   └── SecurityEventLogger.java
├── controller/
│   ├── AccessDeniedController.java
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── HomeController.java
│   ├── NoteController.java
│   └── UserController.java
├── dto/
│   ├── NoteRequest.java
│   ├── NoteResponse.java
│   └── UserResponse.java
├── model/
│   ├── Note.java
│   └── User.java
├── repository/
│   ├── NoteJdbcRepository.java
│   ├── NoteRepository.java
│   └── UserRepository.java
└── service/
    ├── CustomUserDetailsService.java
    ├── NoteService.java
    └── UserService.java

src/main/resources
├── db/migration/
│   ├── V1__create_users.sql
│   ├── V2__add_role_to_users.sql
│   └── V3__create_notes.sql
├── templates/
│   ├── access-denied.html
│   ├── admin-ping.html
│   ├── admin-users.html
│   ├── error.html
│   ├── home.html
│   ├── login.html
│   ├── note-form.html
│   ├── notes-list.html
│   └── register.html
└── application.properties
```

---

## 🔐 Authentication & Authorization

### Session-Based Authentication

This project uses Spring Security session-based authentication:

- Sessions stored server-side
- Browser stores only JSESSIONID cookie
- CSRF tokens for form protection
- No JWT tokens (MVC track)

### Access Rules

| Path | Access |
|------|--------|
| `/login`, `/register`, `/access-denied`, `/error`, `/css/**`, `/js/**` | 🌐 Public |
| `/user/**` | 👤 ROLE_USER or ROLE_ADMIN |
| `/admin/**` | 👑 ROLE_ADMIN only |
| All other endpoints | 🔒 Authenticated users |

### User Roles

- **ROLE_USER** (default): Standard user access
- **ROLE_ADMIN**: Administrative privileges

### Admin Registration

To register as admin (MVC form), provide:

- `role` = ROLE_ADMIN
- `adminSecret` = ADMIN_REGISTER_SECRET

⚠️ **The admin secret must match the `ADMIN_REGISTER_SECRET` value in `.env`**

---

## 🧪 Testing Guide (MVC Demo)

### 1. Register a normal user

Open: `http://localhost:8080/register`

Register with a strong password (example): `StrongPass1!`

**Expected:**
- ✅ Redirect to `/login`
- ✅ User saved with BCrypt password hash

### 2. Show validation errors

**Duplicate email:**
- Register again with same email → `error=exists`

**Weak password:**
- Try a password without uppercase/number/symbol → `error=weak`

**Blacklisted password:**
- Try `password123!` → `error=common`

### 3. Login

Open: `http://localhost:8080/login`

- Failed login → shows generic "Login failed"
- Successful login → redirected to `/home`

### 4. Proof of authentication (session cookie)

DevTools → Application → Cookies

- ✅ `JSESSIONID` exists after login

### 5. Authorization checks

**As USER:**
- `/admin/ping` → 403 (redirect to `/access-denied`)

**As ADMIN:**
- `/admin/ping` → "ADMIN OK"
- `/admin/users` → list users

### 6. User Data Isolation (Notes)

- Login as User A → create a note
- Logout
- Login as User B → notes list should be empty
- Try edit/delete by guessing id → should return 404 (ownership enforced)

### 7. HTTP Security Headers

DevTools → Network → open a request (e.g., `/home`)

Response headers should include:
- `X-Content-Type-Options`
- `X-Frame-Options`
- `Content-Security-Policy`
- `Referrer-Policy`

### 8. Logout behavior

Click Logout → Refresh `/home` or `/notes`

- ✅ Redirected to `/login`

---

## 🧪 Testing & CI/CD (Lab 14)

### Test Coverage

**Total Tests: 43+**

#### Unit Tests (17 tests)
- ✅ **UserService:** User listing and DTO conversion
- ✅ **NoteService:** CRUD operations with access control
- ✅ **Password Validation:** Strong password policy enforcement

#### Integration Tests (26 tests)
- ✅ **Authentication Flow:** Login, logout, session management
- ✅ **Authorization (RBAC):** Role-based access control (USER vs ADMIN)
- ✅ **CSRF Protection:** All POST requests require CSRF token
- ✅ **Data Isolation:** Users can only access their own notes

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run security scan
mvn dependency-check:check
```

### Code Coverage (JaCoCo)
- **Minimum threshold:** 30%
- **Report location:** `target/site/jacoco/`
- **Includes:** Line, branch, method, and class coverage

### CI/CD Pipeline (GitHub Actions)

**Workflow:** `.github/workflows/ci-cd.yml`

**Triggers:**
- Push to `main` or `develop`
- Pull requests to `main` or `develop`

**Pipeline Steps:**
1. Checkout code
2. Setup JDK 17
3. Build project
4. Run all tests
5. Generate JaCoCo coverage report
6. Check coverage threshold (30%)
7. OWASP dependency vulnerability scan
8. Upload reports as artifacts

**Build Fails If:**
- ❌ Any test fails
- ❌ Code coverage below 30%
- ❌ High-severity vulnerability found (CVSS ≥ 7.0)

### Security Testing Highlights

✅ **Access Control Tests**
- User A cannot view/edit/delete User B's notes
- Regular users cannot access admin endpoints
- Proper 403/404 responses for unauthorized access

✅ **CSRF Protection Tests**
- All state-changing operations require CSRF token
- Missing token returns 403 Forbidden
- Valid token allows operation

✅ **Password Security Tests**
- Weak passwords rejected
- Common passwords blocked (blacklist)
- Strong password policy enforced

✅ **Session Management Tests**
- Logout invalidates session
- Already authenticated users redirect to home
- Protected endpoints require authentication

### Test Files

```
src/test/java/com/berk/lab10/
├── service/
│   ├── UserServiceTest.java
│   └── NoteServiceTest.java
├── validation/
│   └── PasswordValidationTest.java
└── integration/
    ├── AuthenticationIntegrationTest.java
    └── CsrfIntegrationTest.java
```

### OWASP Dependency Check
- Scans third-party dependencies for known vulnerabilities
- Fails build on CVSS ≥ 7.0 (High/Critical severity)
- Report: `target/dependency-check-report.html`

---

## 📊 Lab Status

| Lab | Status | Description |
|-----|--------|-------------|
| Lab 10 | ✅ Completed | Spring Boot MVC + DB + Flyway |
| Lab 11 (Session) | ✅ Completed | Session-based authentication + RBAC |
| Lab 12 | ✅ Completed | Secure CRUD (Notes) + user_id isolation |
| Lab 13 | ✅ Completed | Logging, safe errors, security headers |
| Lab 14 | ✅ Completed | Testing, CI/CD, code coverage, OWASP security scanning |

---

## 🔒 Security Features

- ✅ **Password Hashing:** BCrypt (strength 12)
- ✅ **CSRF Protection:** Enabled for all POST forms
- ✅ **Session Management:** Logout invalidates session + deletes cookies
- ✅ **SQL Injection Prevention:** JdbcTemplate prepared statements + JPA
- ✅ **Role-Based Access:** Route rules + @PreAuthorize
- ✅ **Input Validation:** Jakarta Validation (@NotBlank, @Size)
- ✅ **Safe Error Handling:** GlobalExceptionHandler (no stack trace to user)
- ✅ **Security Headers:** CSP, frame options, nosniff, referrer policy
- ✅ **Secure Logging:** Failed login + unauthorized/forbidden attempts logged without secrets

---

## 📝 Environment Variables

Create a `.env` file with:

```properties
DB_URL=jdbc:sqlite:database.db
DB_DRIVER=org.sqlite.JDBC
HIBERNATE_DIALECT=org.hibernate.community.dialect.SQLiteDialect
ADMIN_REGISTER_SECRET=CHANGE_ME_123
```

⚠️ **Never commit `.env` to version control!**

---

## 📄 License

This project is created for educational purposes as part of the Security of Web Applications course.

---

## 👤 Author

**Berk**

For questions or issues, please open an issue in the repository.
