# springboot-toolkit

> A collection of production-ready, fully customizable Spring Boot starter libraries.
> Drop any module in as a Maven dependency — zero boilerplate, everything overridable.

[![Build](https://github.com/biruk-auth/springboot-toolkit/actions/workflows/ci.yml/badge.svg)](https://github.com/biruk-auth/springboot-toolkit/actions)
[![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue)](https://github.com/biruk-auth/springboot-toolkit/packages)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Overview

`springboot-toolkit` is a monorepo of Spring Boot auto-configuration libraries built for developers who want a solid, consistent foundation across multiple projects without copy-pasting the same boilerplate every time.

Each module is an independent `jar` published to GitHub Packages. You pick only what you need. Every bean is declared with `@ConditionalOnMissingBean` — your own beans always win.

---

## Modules

| Module | Description | Status |
|--------|-------------|--------|
| [`auth-starter`](#auth-starter) | JWT, OAuth2, RBAC, MFA, user management, audit log | 🚧 In Progress |
| [`websocket-starter`](#websocket-starter) | STOMP WebSocket, JWT auth, presence tracking | 📋 Planned |
| [`notification-starter`](#notification-starter) | Email, SMS, push — unified API | 📋 Planned |
| [`storage-starter`](#storage-starter) | S3, MinIO, local disk — unified StorageService | 📋 Planned |
| [`api-starter`](#api-starter) | Response envelope, pagination, versioning, OpenAPI | 📋 Planned |

---

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.9+
- A GitHub account (for GitHub Packages)

### Configure GitHub Packages in your `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>  <!-- needs read:packages scope -->
    </server>
  </servers>
</settings>
```

### Add the repository to your project `pom.xml`

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/biruk-auth/springboot-toolkit</url>
  </repository>
</repositories>
```

### Add the dependency you need

```xml
<!-- Auth only -->
<dependency>
  <groupId>io.github.biruksolomon</groupId>
  <artifactId>auth-starter</artifactId>
  <version>1.0.0</version>
</dependency>

<!-- Or combine modules -->
<dependency>
  <groupId>io.github.biruk-auth</groupId>
  <artifactId>notification-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Module Details

---

### `auth-starter`

The most complete module. Drop it in and get a fully working auth system backed by Spring Security.

#### Features

| Feature | Description |
|---------|-------------|
| **JWT Auth** | Access + refresh token pair, configurable expiry, token rotation |
| **Token Blacklist** | Redis-backed logout/revocation, zero DB reads per request |
| **OAuth2** | Google, GitHub social login — extensible to any provider |
| **API Key Auth** | Machine-to-machine; keys stored SHA-256 hashed |
| **MFA / TOTP** | Google Authenticator compatible, per-user opt-in |
| **Magic Link** | Passwordless email login |
| **RBAC** | Roles → Permissions (many-to-many), wildcard support (`invoice:*`) |
| **Permission Inheritance** | Roles can extend other roles |
| **@RequiresPermission** | Custom annotation for method-level permission checks |
| **Resource Ownership** | Optional check — user can only modify their own resource |
| **User Management** | Full CRUD, soft delete, email verification, account locking |
| **Password Policy** | Min length, complexity, history — all configurable |
| **Rate Limiting** | Per-IP and per-user via Bucket4j + Redis |
| **Audit Log** | Every auth event stored and queryable |
| **Email Flows** | Verify email, password reset — Thymeleaf HTML templates |

#### Package Structure

```
auth-starter/src/main/java/io/github/birukauth/auth/
├── autoconfigure/      Spring Boot auto-config entry points (@ConditionalOnMissingBean)
├── properties/         Typed @ConfigurationProperties for all auth.* yaml keys
├── domain/
│   ├── entity/         JPA entities (AuthUser, AuthRole, AuthPermission, ...)
│   └── enums/          AuthProvider, AuditAction, TokenType
├── repository/         Spring Data JPA repos — one per entity
├── dto/
│   ├── request/        Validated inbound payloads (@Valid)
│   └── response/       Outbound API shapes — entities never exposed directly
├── security/
│   ├── jwt/            Token creation, parsing, validation, Redis blacklist
│   ├── apikey/         Header-based API key filter
│   ├── rbac/           Permission resolution, caching, @RequiresPermission handling
│   ├── ratelimit/      Bucket4j filter — per IP and per authenticated user
│   └── userdetails/    Bridge between AuthUser entity and Spring Security
├── service/            All business logic — no Spring Security knowledge here
├── controller/         Thin HTTP layer — delegates to services
├── event/              Decoupled auth events — hook via @EventListener
├── annotation/         @RequiresPermission, @RequiresRole, @CurrentUser
├── exception/          Typed exception hierarchy + @RestControllerAdvice handler
└── mapper/             MapStruct entity ↔ DTO — zero manual mapping
```

#### REST Endpoints

All endpoints prefixed by `auth.api.base-path` (default `/`).

```
POST   /auth/register
POST   /auth/login
POST   /auth/refresh
POST   /auth/logout
POST   /auth/verify-email
POST   /auth/forgot-password
POST   /auth/reset-password
POST   /auth/mfa/setup
POST   /auth/mfa/verify
GET    /auth/me
PUT    /auth/me
GET    /admin/users
GET    /admin/users/{id}
PUT    /admin/users/{id}/roles
DELETE /admin/users/{id}
GET    /admin/roles
POST   /admin/roles
PUT    /admin/roles/{id}/permissions
GET    /admin/permissions
POST   /admin/permissions
GET    /admin/audit-log
```

#### Configuration

```yaml
auth:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry: 15m
    refresh-token-expiry: 7d
    issuer: my-app
  password:
    min-length: 8
    require-special-chars: true
    history-count: 5
  account:
    max-failed-attempts: 5
    lock-duration: 30m
  email:
    verification-required: true
    token-expiry: 24h
  mfa:
    enabled: false
  oauth2:
    enabled: false
    providers: [google, github]
  api:
    base-path: /api/v1
  rate-limit:
    login:
      requests: 10
      per: 1m
  table-prefix: auth_
```

#### Override Any Bean

```java
// Replace the default UserDetailsService
@Bean
public UserDetailsService myUserDetailsService(UserRepository repo) {
    return username -> repo.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
}

// Replace the SecurityFilterChain entirely
@Bean
public SecurityFilterChain myFilterChain(HttpSecurity http) throws Exception {
    // your custom config
}

// Listen to auth events
@EventListener
public void onLogin(UserLoggedInEvent event) {
    analyticsService.track(event.getUserId(), "login");
}
```

---

### `websocket-starter`

STOMP over WebSocket with first-class auth integration.

#### Features

- STOMP protocol with SockJS fallback
- JWT handshake interceptor — authenticated connections only
- Redis pub/sub message broker — scales across instances
- Presence tracking — who is online, per room
- Room/channel management — create, join, leave
- Typed message routing with `@MessageMapping`
- Heartbeat + reconnect configuration
- Optional `auth-starter` integration for user context

#### Configuration

```yaml
ws:
  endpoint: /ws
  allowed-origins: "*"
  heartbeat:
    incoming: 10000
    outgoing: 10000
  presence:
    enabled: true
    ttl: 30s
```

---

### `notification-starter`

One `NotificationService` interface. Three delivery channels.

#### Features

- **Email** — SMTP (Spring Mail) or SendGrid, Thymeleaf templates
- **SMS** — Twilio, template variables supported
- **Push** — Firebase FCM, topic and device targeting
- Async delivery via Spring `@Async`
- Retry with exponential backoff
- Delivery log persisted to DB (optional)
- Per-channel enable/disable via config
- Deduplication via Redis (prevent double-send)

#### Configuration

```yaml
notification:
  email:
    provider: smtp          # smtp | sendgrid
    from: noreply@myapp.com
  sms:
    enabled: false
    provider: twilio
  push:
    enabled: false
    provider: firebase
  async:
    enabled: true
    thread-pool-size: 5
  retry:
    max-attempts: 3
    backoff-ms: 1000
```

---

### `storage-starter`

One `StorageService` interface. Any backend.

#### Features

- **AWS S3** — upload, download, delete, pre-signed URLs, multi-part
- **MinIO** — self-hosted S3-compatible, same API
- **Local disk** — for development and testing
- File type validation via Apache Tika (no extension spoofing)
- File size limits — configurable per endpoint
- CDN URL generation — prefix swap for CloudFront/Cloudflare
- File metadata persisted to DB (optional)
- Image resizing hooks (integration point, not bundled)

#### Configuration

```yaml
storage:
  provider: s3              # s3 | minio | local
  s3:
    bucket: my-bucket
    region: us-east-1
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    cdn-url: https://cdn.myapp.com
  minio:
    endpoint: http://localhost:9000
    bucket: my-bucket
    access-key: ${MINIO_ACCESS_KEY}
    secret-key: ${MINIO_SECRET_KEY}
  local:
    base-path: /tmp/uploads
  allowed-types: [image/jpeg, image/png, application/pdf]
  max-file-size: 10MB
```

---

### `api-starter`

Consistent REST API surface in one dependency.

#### Features

- **Response envelope** — `{ status, message, data, timestamp, requestId }`
- **Global exception handler** — maps all exceptions to structured error responses
- **Pagination** — `PageRequest` builder, `PageResponse<T>` wrapper
- **API versioning** — header-based (`X-API-Version`) or URL-based (`/v1/`)
- **Request logging** — MDC-enriched, configurable log level
- **Request ID** — `X-Request-Id` propagated through response headers
- **CORS** — configurable allowed origins, methods, headers
- **Health checks** — extended Actuator endpoints
- **OpenAPI config** — pre-wired Swagger UI with auth integration

#### Configuration

```yaml
api:
  versioning:
    strategy: url            # url | header
    default-version: v1
  response:
    wrap-enabled: true
  logging:
    requests: true
    level: INFO
  cors:
    allowed-origins: "*"
    allowed-methods: [GET, POST, PUT, DELETE, PATCH]
```

---

## Monorepo Structure

```
springboot-toolkit/
├── pom.xml                          ← Parent POM — version + dependency management
│
├── auth-starter/
│   ├── pom.xml
│   └── src/
│       ├── main/java/.../auth/      ← Full auth implementation
│       └── test/java/.../auth/      ← Unit + integration tests
│
├── websocket-starter/
│   ├── pom.xml
│   └── src/
│       ├── main/java/.../websocket/
│       └── test/
│
├── notification-starter/
│   ├── pom.xml
│   └── src/
│       ├── main/java/.../notification/
│       └── test/
│
├── storage-starter/
│   ├── pom.xml
│   └── src/
│       ├── main/java/.../storage/
│       └── test/
│
└── api-starter/
    ├── pom.xml
    └── src/
        ├── main/java/.../api/
        └── test/
```

---

## Development

### Clone and build all modules

```bash
git clone https://github.com/biruk-auth/springboot-toolkit.git
cd springboot-toolkit
mvn clean install -DskipTests
```

### Build a single module

```bash
mvn clean install -pl auth-starter -am -DskipTests
```

### Run tests for a module

```bash
mvn test -pl auth-starter
```

### Build with coverage report

```bash
mvn clean verify -pl auth-starter
# Report: auth-starter/target/site/jacoco/index.html
```

---

## Git Branch Strategy

```
main          ← stable releases only — tagged (v1.0.0, v1.1.0)
develop       ← integration branch — all features merge here first

feature/auth-core           JWT, login, register, refresh, logout
feature/user-management     CRUD, soft-delete, account lock, password policy
feature/rbac                Roles, permissions, @RequiresPermission, cache
feature/email-flows         Email verify, password reset, magic link
feature/mfa                 TOTP setup + verify
feature/oauth2              Google + GitHub social login
feature/api-keys            Machine-to-machine API key auth
feature/rate-limiting       Bucket4j per-IP + per-user
feature/audit-log           Audit event storage + query API
feature/test-support        @WithMockAuthUser, AuthTestUtils
feature/openapi-docs        Swagger annotations on all endpoints
feature/websocket-core      websocket-starter base
feature/notification-core   notification-starter base
feature/storage-core        storage-starter base
feature/api-core            api-starter base
```

Flow:
```
feature/* → PR → develop → (all tests green) → PR → main → tag → publish
```

---

## Publishing

### Publish to GitHub Packages

```bash
mvn deploy -pl auth-starter
```

### Publish all modules

```bash
mvn deploy
```

Requires `GITHUB_TOKEN` set in environment or `~/.m2/settings.xml` as shown above.

---

## Roadmap

- [x] Monorepo scaffolding + parent POM
- [ ] `auth-starter` — core JWT + user management
- [ ] `auth-starter` — RBAC + permissions
- [ ] `auth-starter` — email flows + MFA
- [ ] `auth-starter` — OAuth2 + API keys
- [ ] `api-starter` — response envelope + exception handling
- [ ] `notification-starter` — email + SMS + push
- [ ] `storage-starter` — S3 + MinIO
- [ ] `websocket-starter` — STOMP + presence
- [ ] GitHub Actions CI/CD pipeline
- [ ] Maven Central publishing

---

## License

MIT — use freely in personal and commercial projects.
