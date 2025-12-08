# LearnOnline – CSIS 231 Final Project

> **University of Balamand – Faculty of Arts & Sciences**  
> **CSIS 231 – Java Technology**  
> Secure online learning platform with **Spring Boot + PostgreSQL + JavaFX 2D/3D**,  
> **JWT + OTP 2FA**, and **role-based dashboards (Admin)**.

---

## 1. Project Overview

**TicketHaven** is a modular Online Ticketing Box Office platform built as the final project for **CSIS 231 – Java Technology / Advanced Java**.

The system provides:

- A **secure REST backend** (Spring Boot + Spring Security + JWT + OTP 2FA).
- A **desktop JavaFX client** with:
    - Modular **FXML views** and shared styling (`styles.css`).
    - **2D dashboards** using JavaFX charts (`BarChart`, `PieChart`).
    - **3D analytics** using JavaFX 3D (`SubScene`, `Box`).
- A **PostgreSQL** database as the single source of truth for all business logic and persistence.

### Roles & flows

- **Customer**
    - UI not finished yet
    - Register and login (with **OTP-based 2FA** when required).
    - Browse events, purchase tickets, register in events etc.. .
    - View **registered events**.
- **Admin**
    - Global view and management of users, events, categories, sections.
    - Ticket and Transaction management not finished yet.
    - Access **data visualizations (2D + 3D)** for the events.

This project is explicitly designed to match the **CSIS 231 Final Project requirements** (see section 11) and to demonstrate:

- Real **JavaFX 2D + 3D graphics** for dashboards and analytics.
- **JWT + OTP-based security**, with OTP used both for:
- **Two-Factor Authentication (2FA)** on login (`LOGIN_2FA`).
- **Forgot-password** flows (`PASSWORD_RESET` OTP).

---

## 2. High-Level Architecture

**Monorepo structure:**

```text
GeorgeProject/
    ├── csis231-api/          # Spring Boot backend (REST API + security)
    └── demo/                 # JavaFX 17 client (FXML + 2D/3D visualizations)
```

### Backend (Spring Boot – `csis231-api`)

- **Layered architecture**:
    - `controller` – REST endpoints.
    - `service` – business logic, validation, security checks.
    - `repository` – Spring Data JPA repositories.
    - `model`      – entities (User, Event, Category, etc..).
- Stateless security with **JWT**.
- Email-based **OTP** for:
    - **LOGIN_2FA** (optional 2-factor on login).
    - **PASSWORD_RESET** (forgot-password).
- Centralized **error handling** via `@ControllerAdvice`.

### Database (PostgreSQL)

- Stores:
    - Users & Roles
    - Categories
    - Events
    - Sections
    - Tickets
    - Transactions
    - OTP tokens (purpose + expiry)
- Uses **Spring Data JPA** for ORM.
- 
### Frontend (JavaFX – `demo`)

- JavaFX 17 application with:
    - **FXML and CSS views** under `demo/src/main/resources/com/example/demo`.
    - Controllers under `demo/src/main/java/com/example/demo`.
    - Models under `demo/src/main/java/com/example/demo`.
    - Graphics under `demo/src/main/java/com/example/demo`.
    - APIs under `demo/src/main/java/com/example/demo`.
  - Shared stylesheet: `styles.css` (dark theme, buttons, cards, tabs, 2D/3D viz styles).
- Navigation:
    - `Launcher` + `HelloApplication` handle scene switching and role-based routing.

## 3. Technology Stack

### Backend

- Java 17+
- Spring Boot (Web, Security, Validation)
- Spring Data JPA / Hibernate
- Spring Mail / JavaMail (for OTP e-mails)
- Spring Security + **JWT**
- Jakarta Bean Validation (`jakarta.validation`)

### Database

- PostgreSQL
- Configuration via `application.yml` + environment variables.

### Frontend

- JavaFX 17 (controls, FXML, 2D + 3D)
- JavaFX charts:
    - `BarChart<String, Number>`
    - `PieChart<String, Number>`
- JavaFX 3D:
    - `SubScene`, `Group`, `Box`,
- Shared CSS (`styles.css`) with:
    - Buttons: `.primary-button`, `.secondary-button`, …
    - Layout helpers: cards, sections, chips, toolbars
    - Styled `TabPane` and chart backgrounds

### Build & Tools

- Maven (multi-module)
- IntelliJ IDEA
- pgAdmin (for DB management)

---

## 4. Core Features by Role

### Customer

-UI not finished
- Register / login with **JWT auth** and optional **OTP 2FA**.
- Browse available events and register.
- see their registered events.

### Admin

- Manage:
    - Users & roles
    - Categories
    - Events
    - Sections
    - Tickets (not finished yet)
    - Transactions (not finished yet)
- Open **“Data Visualizations”**:
    - See **3D analytics** for the events.
    - See **2D analytics** for the events.
---

## 5. Security & Authentication
### JWT + OTP for 2FA and Forgot Password

### Login + 2FA flow

1. User sends `POST /api/auth/login` with username/password.
2. Backend validates credentials:
    - If **2FA not required** → returns `AuthResponse` with JWT.
    - If **2FA required** → throws `OtpRequiredException`, and:
        - Returns HTTP **202 Accepted** with `otpRequired = true`.
        - Sends **LOGIN_2FA OTP** to the user’s e-mail.
3. JavaFX client opens **`otp.fxml`** and calls `POST /api/auth/otp/verify`.
4. Backend verifies OTP:
    - On success → returns normal `AuthResponse` with JWT.
    - On failure → `401 Unauthorized` with unified error JSON.
5. Client stores JWT in **TokenStore / SessionStore** and includes it in `Authorization: Bearer <jwt>` for all calls.

### Forgot password (OTP-based `PASSWORD_RESET`)

1. User clicks **“Forgot password?”** in the JavaFX client.
2. Client calls `POST /api/auth/password/forgot` with e-mail.
3. Backend sends a **PASSWORD_RESET OTP** to this e-mail.
4. User enters OTP + new password; client calls `POST /api/auth/password/reset`.
5. Backend validates OTP and updates the password.

### OTP endpoints

- `POST /api/auth/otp/verify`  
  Verify OTP for both **LOGIN_2FA** and **PASSWORD_RESET**.
- `POST /api/auth/otp/request`  
  Resend OTP when the user did not receive it or it expired.

### Authorization & errors

- Role-based access enforced via **Spring Security**.
- UI hides/disables actions that are not allowed for Student/Instructor/Admin.
- `GlobalExceptionHandler` converts exceptions into a unified `ErrorResponse` with:
    - timestamp
    - HTTP status
    - error code
    - message
    - validation details (if any)

---

## 6. 2D & 3D Visualizations

**Location:**

- `demo/src/main/java/com/example/demo/graphics/GraphicsController.java`
- `demo/src/main/resources/com/example/demo/fxml/graphics.fxml`

### Layout

- **Top bar**
    - Title + welcoming the user and logout routing the user back to the login.

### Backend data used for 2D/3D

- `GET /events`  
  get all events.
- `GET /events/status/{status}`  
  get events by status. → **3D bars** and **2D charts**.

---

## 7. Project Structure (Detailed)

### Backend – `csis231-api`

```text
csis231-api/
├── src/main/java/com/csis231/api
│   ├── auth/           # Login, register, forgot/reset password, JWT
│       └── otp/        # OTP verify/request endpoints
│   ├── user/           # User & Role domain, services, controllers
│   ├── category/       # Event categories
│   ├── config/         # Security
│   ├── Event/          # Events services and controllers
│   ├── Section/        # Event sections
│   ├── Ticket/         # Event tickets
│   ├── Transaction/    # Event transactions
└── src/main/resources
    └── application.yml
```

### JavaFX Client – `demo`

```text
demo/
├── src/main/java/com/example/demo
│   ├── api/          # api endpoints for category, section, auth, ...
│   ├── controllers/  # Controllers for category, dashboard, ...
│   ├── graphics/     # GraphicsController (2D/3D visualizations)
│   ├── model/        # Model classes for category, section, auth, ...
│   ├── security/     # Token handling
│   ├── util/         # Utility functions
│   ├── Launcher.java # Central navigation helper
│   └── HelloApplication.java # JavaFX entry point
└── src/main/resources/com/example/demo
    ├── fxml/          # login.fxml, register.fxml, otp.fxml, forgot.fxml, dashboard.fxml, ...
    ├── styles.css     # shared JavaFX CSS
```

---

## 8. REST API Reference (Actual Endpoints)

Below are the main mappings from:

- `AuthController`
- `OtpController`
- `UserController`
- `CategoryController`
- `EventController`
- `SectionController`

### 8.1 Auth & OTP

#### Auth (`/api/auth`)

| HTTP   | Path                         | Description                                         |
|--------|------------------------------|-----------------------------------------------------|
| POST   | `/api/auth/login`           | Login with username/password (with optional 2FA).   |
| POST   | `/api/auth/register`        | Register a new user.                               |
| POST   | `/api/auth/password/forgot` | Start **forgot-password** OTP flow.                |
| POST   | `/api/auth/password/reset`  | Reset password using a **PASSWORD_RESET** OTP.     |

#### OTP (`/api/auth/otp`)

| HTTP   | Path                         | Description                                     |
|--------|------------------------------|-------------------------------------------------|
| POST   | `/api/auth/otp/verify`      | Verify OTP (`LOGIN_2FA` or `PASSWORD_RESET`).   |
| POST   | `/api/auth/otp/request`     | Resend OTP for a given username.                |

---

### 8.2 Users (`UserController` – `/users`)

| HTTP   | Path        | Description                          |
|--------|-------------|--------------------------------------|
| GET    | `/users`    | List users (paged).                  |
| GET    | `/users/{id}` | Get a single user by id.            |
| POST   | `/users`    | Create a new user.                  |
| PUT    | `/users/{id}` | Update an existing user.            |
| DELETE | `/users/{id}` | Delete a user.                      |
| GET    | `/users/me` | Get the authenticated user profile. |

---

### 8.3 Categories (`CategoryController` – `/categories`)

| HTTP   | Path          | Description           |
|--------|---------------|-----------------------|
| GET    | `/categories` | List all categories.  |
| GET    | `/categories/{id}` | Get category by id.   |
| GET    | `/categories/name/{name}` | Get category by name. |
| POST   | `/categories` | Create category.      |
| PUT    | `/categories/{id}` | Update category.      |
| DELETE | `/categories/{id}` | Delete category.      |

---

### 8.4 Sections

#### Sections (`SectionController` – `/sections`)

| HTTP   | Path        | Description         |
|--------|-------------|---------------------|
| GET    | `/sections` | List sections.      |
| GET    | `/sections/{id}` | Get section details by id. |
| GET    | `/sections/event/{eventId}` | Get sections by event id. |
| GET    | `/sections/status/{status}` | Get section by status. |
| POST   | `/sections` | Create a new section. |
| PUT    | `/sections/{id}` | Update an existing section. |
| DELETE | `/sections/{id}` | Delete a section.   |

---

### 8.5 Events (`EventController` – `/events`)

| HTTP   | Path                            | Description                   |
|--------|---------------------------------|-------------------------------|
| POST   | `/events`                       | Create a new event.           |
| GET    | `/events`                       | List all events.              |
| GET    | `/events/{id}`                  | List event by id.             |
| GET    | `/events/category/{categoryId}` | List event by category id.    |
| GET    | `/events/status/{status}`       | List event by status.         |
| GET    | `/events/date/{date}`           | List event by date.           |
| GET    | `/events/between`               | List event between two dates. |
| GET    | `/events/upcoming`              | List upcoming events.         |
| PUT    | `/events/{id}`                  | Update an existing event.     |
| DELETE | `/events/{id}`                  | Delete an event.              |
| PATCH  | `/events/{id}/cancel`           | Cancel an event.              |
| PATCH  | `/events/{id}/complete`         | Complete an event.            |
| PATCH  | `/events/{id}/capacity`         | Update event capacity.        |

---

## 9. Running the Project

### Prerequisites

- **JDK 17+**
- **Maven 3+**
- **PostgreSQL** (local or Docker)

### 1) Clone the repository

```bash
git clone https://github.com/M677871/csis-231-project.git
cd csis-231-project/csis_231-login-registration-jwt
```

### 2) Create the database

```sql
CREATE DATABASE csis_231_db;
```

### 3) Configure `application.yml`

Create/edit: `csis231-api/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/csis_231_db}
    username: ${DB_USER:postgres}
    password: ${DB_PASS:postgres}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate.format_sql: true

  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-gmail@example.com}
    password: ${MAIL_PASSWORD:your-gmail-app-password}  # Use a Gmail App Password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
      mail.smtp.auth.mechanisms: LOGIN PLAIN
      mail.smtp.ssl.trust: smtp.gmail.com
      mail.smtp.connectiontimeout: 10000
      mail.smtp.timeout: 10000
      mail.smtp.writetimeout: 10000
      mail.debug: false

management:
  health:
    mail:
      enabled: false

jwt:
  secret: ${JWT_SECRET:ChangeThisSecretForProductionUseALongRandomString}
  expiration: ${JWT_EXPIRATION:900000}  # 15 minutes

mail:
  from: ${MAIL_FROM:your-gmail@example.com}
```

> **Important:** never commit real passwords or secrets. Use environment variables in production.

### Environment variables (optional, override defaults)

- `DB_URL`, `DB_USER`, `DB_PASS`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- `JWT_SECRET`, `JWT_EXPIRATION`

### 4) Run the backend

From `csis231-api`:

```bash
mvn clean install
mvn spring-boot:run
```

Backend will start on `http://localhost:8080` (unless overridden).

### 5) Run the JavaFX client

From `demo`:

```bash
mvn clean install
mvn javafx:run
```

Or run `HelloApplication` from your IDE.  
The JavaFX app will handle login (with OTP 2FA), dashboards, and the **2D/3D analytics playground**.

---

## 10. State Management, Error Handling & Validation

### State & communication (JavaFX)

- **SessionStore / TokenStore** track:
    - Current user
    - JWT token(s)
    - Role
- All HTTP calls go through a shared **ApiClient**:
    - Adds `Authorization: Bearer <jwt>` header.
    - Deserializes JSON into DTOs.
    - Wraps errors in `ApiException`.

### Error handling (Frontend)

- Errors are displayed using:
    - `ErrorDialog.showError(ex)` or
    - `AlertUtils.showError(...)`
- Async calls use `CompletableFuture` + `Platform.runLater` to keep UI responsive.

### Validation (Backend)

- DTOs annotated with `@NotBlank`, `@Email`, `@Size`, etc.
- Validation errors are handled in `GlobalExceptionHandler` and returned in a structured JSON format that the client shows nicely.

---

## 11. How This Project Meets CSIS 231 Final Requirements

**Spring Boot Backend**

- Proper **layered architecture** (Controller / Service / Repository / Domain).
- PostgreSQL schema mapped via JPA entities.
- Business logic pushed into service layer, not controllers.
- Full, documented REST API (see section 8).

**Advanced Features & JavaFX 2D/3D**

- **Real 3D graphics**:
    - JavaFX 3D (`SubScene`, `Box`).
    - 3D bars for events.
- **2D charts**:
    - `BarChart`/`PieChart` for events.
- Visualizations fed from real backend endpoints:
    - `GET /events`
    - `GET /events/status/{status}`

**JavaFX UI**

- Multiple FXML screens: login, register, forgot password, OTP, dashboards, events, categories, graphics.
- Shared `styles.css` with reusable style classes.
- Clear role-based dashboards (Admin / Customer not finished yet).
- Dedicated **2D/3D visualization playground**.

**Security, OTP 2FA & Forgot Password**

- **JWT + Spring Security** for authentication and authorization.
- **OTP 2FA on login** via `/api/auth/login` + `/api/auth/otp/verify`.
- **OTP-based forgot-password** via `/api/auth/password/forgot` + `/api/auth/password/reset`.
- Centralized error handling and validation.

**Documentation & API Docs**

- This README includes:
    - Setup and configuration via `application.yml`.
    - Architecture & role-based feature overview.
    - Detailed description of **2D/3D** visualizations.
    - Full REST API reference.
    - Explanation of **OTP 2FA** and **forgot-password OTP** flows.
- Code is structured and ready for Javadoc / Swagger (springdoc) if extended.

---

## 12. Possible Future Improvements

- Add **springdoc-openapi** for Swagger UI.
- Add **Qr code** for the events.
- More analytics (engagement, retention, registration rate).
- Multi-metric 3D scenes (e.g., 2–3 bars per event).
- Audit logging and admin activity reports.

---

## 13. Credits

**Author:** George Frangieh  
**Course:** CSIS 231 – Java Technology  
**Institution:** University of Balamand – Faculty of Arts & Sciences  