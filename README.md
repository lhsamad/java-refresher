# E-Commerce Microservice (Java Refresher)

A robust e-commerce microservice built with **Spring Boot 4**, **Spring Data JPA**, **PostgreSQL**, and **OAuth2 Resource Server (Keycloak)**.

This project features a **dual-layer architecture**:
1. **Server-Side Spring MVC UI**: Pure HTML5 and CSS rendered server-side via **Thymeleaf** with **zero JavaScript**.
2. **Full REST API Layer**: Exposed JSON endpoints documented with **OpenAPI / Swagger UI**, designed for consumption by single-page apps (such as **Angular**) and external clients.

---

## Table of Contents
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Architecture & Interview Master Guide](#architecture--interview-master-guide)
- [Prerequisites](#prerequisites)
- [Keycloak Identity Provider (Local Docker Setup)](#keycloak-identity-provider-local-docker-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Authentication & Login](#authentication--login)
- [Spring Batch Pipeline: Running & Testing](#spring-batch-pipeline-running--testing)
- [Web & API Access Points](#web--api-access-points)
- [REST API Reference](#rest-api-reference)
- [Testing](#testing)
- [Postman Collection](#postman-collection)
- [Future Angular Integration](#future-angular-integration)

---

## Architecture & Tech Stack

* **Framework:** Spring Boot 4.1.2 (Java 25 toolchain)
* **Web Layer:** Spring Web MVC (`spring-boot-starter-webmvc`)
* **Server-Rendered UI:** Thymeleaf (`spring-boot-starter-thymeleaf`) — pure HTML/CSS, zero client-side JavaScript
* **Database & ORM:** PostgreSQL, Spring Data JPA, Hibernate 7
* **Security:** Spring Security 6+ as an OAuth2 Resource Server with JWT validation
* **Identity Provider:** Keycloak (`dev-realm` on port `8777`)
* **API Documentation:** SpringDoc OpenAPI 3.2.0 / Swagger UI
* **Testing:** JUnit 5, Mockito, Spring Security Test, MockMvc
* **Database Seeder:** [`DatabaseSeeder.java`](src/main/java/com/practice/java_refresher/config/DatabaseSeeder.java) automatically populates 33 realistic products across 8 categories and 10 sample orders across all lifecycle statuses (`DELIVERED`, `SHIPPED`, `PROCESSING`, `CONFIRMED`, `PENDING`, `CANCELLED`).

---

## Architecture & Interview Master Guide

An in-depth, senior-level guide covering transaction internals, distributed Sagas, database locking, and interview preparation is available at:
👉 **[`SPRING_BOOT_INTERVIEW_AND_ARCHITECTURE_GUIDE.md`](SPRING_BOOT_INTERVIEW_AND_ARCHITECTURE_GUIDE.md)**

---

## Prerequisites

- **Java JDK 21+** (Java 25 toolchain supported)
- **PostgreSQL 14+** running locally on port `5432` with a database named `ecommerce`
- **Docker & Docker Compose** (for running the preconfigured Keycloak identity provider on port `8777`)

---

## Keycloak Identity Provider (Local Docker Setup)

The project includes a preconfigured Keycloak instance backed by PostgreSQL with an automatically imported realm (`dev-realm`), clients, roles, and test users defined in [`realm-config/dev-realm-realm.json`](realm-config/dev-realm-realm.json).

### 1. Start Keycloak
Run the following in the project root:
```bash
# Start PostgreSQL and Keycloak in the background
docker compose up -d

# Follow the startup logs
docker compose logs -f keycloak
```
Wait until the logs display:
```text
Keycloak ... started in ... Running the server in development mode
```

### 2. Preconfigured Endpoints & Credentials
* **Keycloak Web Admin Console**: [http://localhost:8777](http://localhost:8777)
  * **Admin Username**: `admin`
  * **Admin Password**: `adminpass123`
* **Realm**: `dev-realm`
* **Realm OpenID Discovery**: [http://localhost:8777/realms/dev-realm/.well-known/openid-configuration](http://localhost:8777/realms/dev-realm/.well-known/openid-configuration)
* **Token Endpoint**: `http://localhost:8777/realms/dev-realm/protocol/openid-connect/token`

### 3. Preconfigured Clients & Credentials
| Client ID | Access Type | Secret | Description / Redirect URIs |
| :--- | :--- | :--- | :--- |
| **`ecommerce-client`** | Confidential | `dev-secret` | Primary Spring Boot client for **OAuth2 Login (OIDC SSO)** and REST API calls. Redirect URI: `http://localhost:8080/login/oauth2/code/keycloak` |
| **`spring-boot-client`** | Confidential | `spring-boot-client-secret` | Confidential backend OAuth2 client |
| **`spring-boot-public`** | Public | *(none)* | Public client for Postman, curl, or Single-Page Apps (SPA) |

### 4. Preconfigured Users & Roles
| Username | Password | Roles | Access Level |
| :--- | :--- | :--- | :--- |
| **`user`** | `user123` | `ROLE_USER` | Standard customer: browse products, manage cart, checkout, view own orders |
| **`admin`** | `admin123` | `ROLE_ADMIN`, `ROLE_USER` | Administrator: full customer access + add/edit products, manage all orders, update status |

> [!IMPORTANT]
> **Modifying the Realm JSON & Forcing Re-Import:**  
> Keycloak's `--import-realm` flag **only** imports JSON if the realm does **not** already exist in the database. If you modify [`realm-config/dev-realm-realm.json`](realm-config/dev-realm-realm.json), wipe the database volume and restart to force a clean re-import:
> ```bash
> docker compose down -v
> docker compose up -d
> ```

---

## Configuration

Default settings are defined in [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
spring.application.name=ecommerce-service
server.port=8080

# PostgreSQL Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/ecommerce}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:luqmansamad}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# Keycloak / OAuth2 Resource Server Configuration (REST API JWT validation)
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8777/realms/dev-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8777/realms/dev-realm/protocol/openid-connect/certs

# Keycloak OAuth2 Login (OIDC SSO Client) Configuration (Spring MVC Web UI)
spring.security.oauth2.client.registration.keycloak.client-id=ecommerce-client
spring.security.oauth2.client.registration.keycloak.client-secret=dev-secret
spring.security.oauth2.client.registration.keycloak.scope=openid,profile,email,roles
spring.security.oauth2.client.registration.keycloak.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.keycloak.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

spring.security.oauth2.client.provider.keycloak.authorization-uri=http://localhost:8777/realms/dev-realm/protocol/openid-connect/auth
spring.security.oauth2.client.provider.keycloak.token-uri=http://localhost:8777/realms/dev-realm/protocol/openid-connect/token
spring.security.oauth2.client.provider.keycloak.jwk-set-uri=http://localhost:8777/realms/dev-realm/protocol/openid-connect/certs
spring.security.oauth2.client.provider.keycloak.user-info-uri=http://localhost:8777/realms/dev-realm/protocol/openid-connect/userinfo
spring.security.oauth2.client.provider.keycloak.user-name-attribute=preferred_username

# Spring Batch Configuration
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always
```

---

## Running the Application

### Start the Service
```bash
./gradlew bootRun
```

### Build & Package
```bash
./gradlew clean build
```

---

## Authentication & Login (Keycloak OpenID Connect SSO)

The application delegates all authentication directly to **Keycloak** (`dev-realm` on port `8777`), completely eliminating local database password storage and local session-only authentication:

### 1. Browser Spring MVC UI (OAuth2 / OIDC Login)
* **Single Sign-On (SSO)**: Clicking **"Sign In"** or accessing protected routes (`/checkout`, `/web/orders`) redirects the user to Keycloak's centralized login page via OpenID Connect authorization code flow (`/oauth2/authorization/keycloak`).
* **Role Mapping**: Keycloak's `realm_access.roles` are dynamically mapped via [`KeycloakOidcAuthoritiesMapper.java`](src/main/java/com/practice/java_refresher/security/KeycloakOidcAuthoritiesMapper.java) to Spring Security authorities (`ROLE_USER`, `ROLE_ADMIN`).
* **Authentication-Aware Navbar**:
  * Unauthenticated users see a **"Sign In"** button linking directly to Keycloak SSO.
  * Authenticated users see **"Hello, `<username>`"** (extracted from Keycloak's `preferred_username` claim) and a **"Logout"** button.
  * The **"+ Add Product"** link is conditionally displayed only to users with `ROLE_ADMIN`.

#### Keycloak Test Accounts
| Username | Password | Assigned Realm Roles | Permissions / Access |
| :--- | :--- | :--- | :--- |
| **`user`** | `user123` | `ROLE_USER` | Browse catalog, add to cart, checkout / place orders, view personal order history |
| **`admin`** | `admin123` | `ROLE_ADMIN`, `ROLE_USER` | Full customer access plus add new products, edit catalog, view all customer orders, and update order statuses |

### 2. REST API Layer (`/api/**`)
* **100% Stateless OAuth2 Resource Server**: Protected by Keycloak JWT tokens (`Authorization: Bearer <token>`).
* **No Cookies or Sessions**: Configured with `SessionCreationPolicy.STATELESS`—never generates or reads `JSESSIONID` cookies.
* **No HTML Redirects**: API requests never redirect to `/login`; unauthenticated calls return standard `401 Unauthorized` JSON responses suitable for Angular, mobile apps, and Postman.
* **Keycloak Token Retrieval (cURL)**:
  Obtain a JWT access token using Resource Owner Password Credentials (ROPC):
  ```bash
  # Customer token:
  curl -X POST "http://localhost:8777/realms/dev-realm/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=ecommerce-client" \
    -d "client_secret=dev-secret" \
    -d "username=user" \
    -d "password=user123" \
    -d "grant_type=password"

  # Admin token:
  curl -X POST "http://localhost:8777/realms/dev-realm/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=ecommerce-client" \
    -d "client_secret=dev-secret" \
    -d "username=admin" \
    -d "password=admin123" \
    -d "grant_type=password"
  ```
* **Keycloak Token Generation via Postman**: You can also use folder `0. Authentication (Token Generation)` in `postman_collection.json` to generate valid tokens using `user` / `user123` and `admin` / `admin123`.

---

## Spring Batch Pipeline: Running & Testing

The project includes an end-to-end implementation of **Spring Batch 6 Chunk-Oriented Processing** designed to handle multi-gigabyte files with constant memory consumption ($O(1)$ heap allocation) and ACID transaction boundaries per chunk.

### Pipeline Architecture Overview
```
JOB: productPipelineJob
  │
  ├──► STEP 1: importCsvToDbStep (Chunk size = 50)
  │       ItemReader   : FlatFileItemReader (Streams lines from CSV without loading full file into memory)
  │       ItemProcessor: ProductCsvProcessor (Cleans prices, validates stock, filters corrupted rows)
  │       ItemWriter   : JpaItemWriter (Batch-persists 50 products per physical DB transaction)
  │       Listener     : LoggingChunkListener (Logs transaction boundaries & commit/rollback counts)
  │
  └──► STEP 2: exportDbToJsonStep (Chunk size = 50)
          ItemReader   : JpaPagingItemReader (Paginates PostgreSQL via keyset/ordered cursor)
          ItemProcessor: ProductJsonProcessor (Enriches DTO with stock status & export timestamps)
          ItemWriter   : ProductJsonStreamWriter (Appends JSON directly to disk, keeping memory bounded)
```

---

### How to Run the Batch Job

You can trigger and monitor the batch pipeline through three different interfaces:

#### Option 1: Via the Web UI Dashboard (Zero Setup)
1. Open your browser to: **[`http://localhost:8080/web/batch`](http://localhost:8080/web/batch)**
2. Inspect the current CSV dataset status and file sizes.
3. Select your input file (default: `data/products_large.csv`) and click **"Run Batch Pipeline"**.
4. The dashboard displays the returned `jobExecutionId`, execution status (`COMPLETED` or `STARTED`), and processing duration.

#### Option 2: Via cURL / Terminal
Trigger the batch job with custom input and output paths:
```bash
# Run with the pre-generated 2,000-item dataset (40 transaction chunks)
curl -X POST "http://localhost:8080/api/batch/run?inputCsvPath=data/products_large.csv&outputJsonPath=data/exported_products.json"

# Run with the 10,000-item dataset (200 transaction chunks)
curl -X POST "http://localhost:8080/api/batch/run?inputCsvPath=data/products_10k.csv&outputJsonPath=data/exported_products.json"
```

**Sample API Response:**
```json
{
  "jobExecutionId": 1,
  "jobName": "productPipelineJob",
  "status": "COMPLETED",
  "startTime": "2026-11-08T01:27:50Z",
  "inputCsvPath": "data/products_large.csv",
  "outputJsonPath": "data/exported_products.json"
}
```

#### Option 3: Via Swagger UI
1. Open **[`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)**
2. Expand the **`Spring Batch Pipeline`** tag.
3. Execute **`POST /api/batch/run`** with parameters or **`GET /api/batch/status/{jobExecutionId}`**.

---

### Monitoring & Inspecting Job Execution State

Check the live metrics, item read/write counters, and commit counts:
```bash
curl -X GET "http://localhost:8080/api/batch/status/1"
```

**Detailed Metrics Output:**
```json
{
  "jobExecutionId": 1,
  "jobName": "productPipelineJob",
  "status": "COMPLETED",
  "exitStatus": "COMPLETED",
  "startTime": "2026-11-08T01:27:50.000+00:00",
  "endTime": "2026-11-08T01:27:51.250+00:00",
  "stepExecutions": {
    "importCsvToDbStep": {
      "status": "COMPLETED",
      "itemsRead": 2000,
      "itemsWritten": 2000,
      "commits": 41,
      "rollbacks": 0,
      "filtered": 0
    },
    "exportDbToJsonStep": {
      "status": "COMPLETED",
      "itemsRead": 2033,
      "itemsWritten": 2033,
      "commits": 42,
      "rollbacks": 0,
      "filtered": 0
    }
  }
}
```

---

### Pre-Generated Datasets & Generating Custom Files

The repository includes pre-built sample datasets:
* [`data/products_large.csv`](data/products_large.csv): **2,000 products** across 8 categories (~266 KB) $\rightarrow$ **40 transaction chunks**.
* [`data/products_10k.csv`](data/products_10k.csv): **10,000 products** (~1.3 MB) $\rightarrow$ **200 transaction chunks**.

#### Generate a Custom Dataset on the Fly:
```bash
# Generate 50,000 rows
curl -X POST "http://localhost:8080/api/batch/generate-sample-csv?outputPath=data/products_50k.csv&totalRows=50000"
```

---

### How to Test & Verify the Batch Pipeline

#### 1. Automated Test Suite
Run the Gradle test suite which verifies batch beans, controllers, and job wiring:
```bash
./gradlew test
```

#### 2. Live Console Log Verification
When running the pipeline via `./gradlew bootRun`, observe the real-time transaction chunk logs emitted by [`LoggingChunkListener.java`](src/main/java/com/practice/java_refresher/batch/listener/LoggingChunkListener.java):
```text
>>> [TRANSACTION START] [importCsvToDbStep] Processing chunk #1 (Thread: http-nio-8080-exec-1)
<<< [TRANSACTION COMMIT] [importCsvToDbStep] Chunk #1 successfully committed! Commits: 1, Items Read: 50, Items Written: 50
...
>>> [TRANSACTION START] [importCsvToDbStep] Processing chunk #40 (Thread: http-nio-8080-exec-1)
<<< [TRANSACTION COMMIT] [importCsvToDbStep] Chunk #40 successfully committed! Commits: 40, Items Read: 2000, Items Written: 2000
================================================================================
✅ SPRING BATCH JOB COMPLETED SUCCESSFULLY in 1245 ms
  Step: 'importCsvToDbStep' | Status: COMPLETED | Read: 2000 | Written: 2000 | Commits: 41 | Rollbacks: 0
  Step: 'exportDbToJsonStep' | Status: COMPLETED | Read: 2033 | Written: 2033 | Commits: 42 | Rollbacks: 0
================================================================================
```

#### 3. Inspecting the Generated JSON Output
Verify that the streamed JSON file was created without memory overflow:
```bash
# Inspect the first few records:
head -n 25 data/exported_products.json

# Check total lines:
wc -l data/exported_products.json
```

#### 4. Querying Spring Batch Metadata in PostgreSQL
Spring Batch stores its execution state in relational metadata tables. You can inspect these directly:
```sql
-- View all job instances
SELECT job_instance_id, job_name FROM batch_job_instance;

-- View execution status and timing
SELECT job_execution_id, status, exit_code, start_time, end_time FROM batch_job_execution;

-- View chunk metrics and commits per step
SELECT step_name, status, read_count, write_count, commit_count, rollback_count 
FROM batch_step_execution 
ORDER BY step_execution_id DESC;
```

---

## Web & API Access Points

| Resource | URL | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| **MVC Dashboard** | `http://localhost:8080/` | Featured products, catalog statistics, quick links | None (Public) |
| **MVC Catalog** | `http://localhost:8080/web/products` | Browse catalog, search, category filter, Add to Cart | None (Public) |
| **Shopping Cart** | `http://localhost:8080/cart` | View items, modify quantities, remove items | None (Public) |
| **Checkout Review** | `http://localhost:8080/checkout` | Review cart and confirm order | Authenticated |
| **My Orders** | `http://localhost:8080/web/orders` | Customer order history with status tracking | Authenticated |
| **Order Receipt** | `http://localhost:8080/web/orders/{orderNumber}` | Single order confirmation and item breakdown | Authenticated |
| **Spring Batch UI**| `http://localhost:8080/web/batch` | Interactive Spring Batch chunking demo dashboard | None (Public) |
| **Sign In** | `http://localhost:8080/login` | Spring MVC login screen initiating Keycloak OIDC SSO | None (Public) |
| **Register Account**| `http://localhost:8080/register` | User self-registration screen | None (Public) |
| **MVC Add Product** | `http://localhost:8080/web/products/new` | Server-validated form to create products | `ROLE_ADMIN` |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Interactive OpenAPI documentation & test runner | None (Public) |
| **OpenAPI Spec (JSON)** | `http://localhost:8080/v3/api-docs` | Raw OpenAPI 3.0 specification | None (Public) |
| **Service Health / Info**| `http://localhost:8080/api/public/info` | API status and metadata | None (Public) |

---

## REST API Reference

All REST endpoints return JSON and handle errors uniformly via `GlobalExceptionHandler` with RFC-compliant timestamps, HTTP status codes, and error descriptions.

### 0. Authentication & Registration
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user account (`RegisterRequestDto`) with hashed credentials |

### 1. Public Products
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/products` | Paginated product list with optional `?category=` and `?search=` filters |
| `GET` | `/api/products/{id}` | Retrieve single product by numeric ID |

### 2. Admin Product Management (`ROLE_ADMIN`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/products` | Create a new product (`ProductRequestDto`) |
| `PUT` | `/api/products/{id}` | Update product details (`ProductRequestDto`) |
| `DELETE` | `/api/products/{id}` | Remove a product from the catalog |

### 3. Customer Orders (`ROLE_USER` or `ROLE_ADMIN`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/orders` | Place order with items and quantities; checks stock and decrements inventory |
| `GET` | `/api/orders` | Retrieve order history for the authenticated user |
| `GET` | `/api/orders/{orderNumber}` | View order details (owner or admin only) |

### 4. Admin Order Processing (`ROLE_ADMIN`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/admin/orders` | View all customer orders with optional `?status=` filter |
| `PATCH`| `/api/admin/orders/{orderNumber}/status` | Update status (`PENDING`, `SHIPPED`, `CANCELLED`). Cancelling auto-restores stock |

### 5. User Profile (`authenticated`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/user/profile` | Returns current user ID, username, email, and realm roles |

### 6. Spring Batch Pipeline (Public / Admin)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/batch/run` | Triggers the 2-step chunked batch pipeline (`CSV -> DB -> JSON`) |
| `GET` | `/api/batch/status/{jobId}` | Inspects job status, chunk commit counts, and item counters |
| `POST` | `/api/batch/generate-sample-csv` | Generates a realistic large product dataset CSV (default: 2,000 items) |

---

## Testing

The project includes unit and integration tests covering business services, controllers, and server-side views:

* **`ProductServiceTest`**: Validates product CRUD, pagination, and `ResourceNotFoundException`.
* **`OrderServiceTest`**: Verifies transactional checkout, inventory decrements, `InsufficientStockException`, customer authorization guards, and stock restoration on order cancellation.
* **`ProductControllerTest`**: Tests public read access vs. admin-only write enforcement with `@WithMockUser`.
* **`OrderControllerTest`**: Validates unauthenticated 401 response and authenticated order placement.
* **`WebViewControllerTest`**: Verifies server-side Spring MVC rendering and model population.

### Run All Tests
```bash
./gradlew test
```

---

## Postman Collection

A complete, import-ready Postman collection is located at [`postman_collection.json`](postman_collection.json) in the project root.

### How to Use:
1. Open **Postman**.
2. Click **Import** &rarr; select [`postman_collection.json`](postman_collection.json).
3. **Automated Token Generation (No copy-pasting required!)**:
   - Go to folder **`0. Authentication (Token Generation)`**.
   - Click **Send** on **`Generate User Token (user / user123)`** &rarr; Postman's test script automatically parses `access_token` and populates the `{{userToken}}` variable!
   - Click **Send** on **`Generate Admin Token (admin / admin123)`** &rarr; Postman automatically populates the `{{adminToken}}` variable!
4. **End-to-End Workflow**:
   - Run **`2. Products Admin CRUD -> Create Product (Admin)`**: Creates a product and automatically sets `{{productId}}`.
   - Run **`3. Customer Orders -> Place Order (Checkout)`**: Uses `{{userToken}}` and `{{productId}}`, and automatically captures the returned `orderNumber` into `{{orderNumber}}`.
   - Run **`3. Customer Orders -> Get Order By Order Number`**: Instantly resolves `{{orderNumber}}`.
   - Run **`4. Admin Orders -> Update Order Status (Admin)`**: Uses `{{adminToken}}` to mark `{{orderNumber}}` as `SHIPPED` or `CANCELLED`.
   - Run **`5. User Profile -> Get Authenticated User Profile`**: Returns user profile and roles for the active `{{userToken}}`.

---

## Future Angular Integration

When developing the Angular client:
1. **CORS:** Already pre-configured in `SecurityConfig.java` to accept requests from `http://localhost:3000` and `http://localhost:4200`.
2. **Authentication:** Attach the Keycloak access token in an `HttpInterceptor`:
   ```typescript
   Authorization: `Bearer ${token}`
   ```
3. **Consume REST Endpoints:** Connect your Angular services to `/api/products`, `/api/orders`, and `/api/user/profile`.

