# spring-auth-core

> A self-contained, drop-in Spring Boot starter that provides a fully working authentication and authorization system with zero boilerplate — everything works out of the box and every piece is overridable.

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2%2B-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub Packages](https://img.shields.io/badge/Published-GitHub%20Packages-181717?logo=github)](https://github.com/biruksolomon/spring-auth-core/packages)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration Reference](#configuration-reference)
- [REST API](#rest-api)
- [Database Schema](#database-schema)
- [RBAC — Roles & Permissions](#rbac--roles--permissions)
- [Extensibility & Overrides](#extensibility--overrides)
- [Testing Support](#testing-support)
- [Module Structure](#module-structure)
- [Publishing](#publishing)
- [Contributing](#contributing)

---

## Overview

`spring-auth-core` is a Maven/Gradle library (packaged as a `jar`) built on top of Spring Boot 3.2+ and Spring Security. Pull it in as a dependency and your application immediately gains:

- JWT and session-based authentication
- OAuth2 / Social login (Google, GitHub)
- API Key auth for machine-to-machine calls
- MFA / TOTP (Google Authenticator compatible)
- Magic link / passwordless email login
- Full RBAC with fine-grained permissions
- Rate limiting, brute-force protection, and audit logging

Every bean is annotated `@ConditionalOnMissingBean`, meaning the consuming application can override any part of the system by simply declaring its own bean.

**Artifact coordinates:**

```xml
<groupId>io.github.biruksolomon</groupId>
<artifactId>spring-auth-core</artifactId>
```

---

## Features

### Authentication Mechanisms

| Mechanism | Status | Notes |
|---|---|---|
| JWT (access + refresh) | ✅ Default | Configurable expiry, secret, issuer |
| Refresh token rotation | ✅ Default | Old token invalidated on use |
| Token blacklisting | ✅ Default | Via Redis on logout/revoke |
| Session-based auth | ⚙️ Optional | Toggle via `auth.session.enabled` |
| OAuth2 / Social login | ⚙️ Optional | Google, GitHub — extensible |
| API Key auth | ⚙️ Optional | Keys stored hashed in DB |
| MFA / TOTP | ⚙️ Optional | Google Authenticator compatible |
| Magic link / passwordless | ⚙️ Optional | Email-based, configurable expiry |

### User Management

- Full CRUD with soft-delete
- Email verification (token-based, configurable expiry)
- Password reset (secure single-use token)
- Password policies (min length, complexity, history)
- Account locking after N failed login attempts (time-based or manual unlock)
- Audit log — every auth event persisted
- User profile extension point via interface

### RBAC — Role-Based Access Control

- **Roles** — named groups (`ADMIN`, `MANAGER`, `USER`, etc.)
- **Permissions** — fine-grained actions (`user:read`, `invoice:create`, `report:export`)
- **Role → Permission mapping** — many-to-many, fully API-managed
- **Permission inheritance** — roles can extend other roles
- **Dynamic permission checks** — `@RequiresPermission("invoice:create")` custom annotation
- **Resource-level permissions** — optional ownership check
- **Wildcard permissions** — `invoice:*` grants all invoice actions
- Permissions stored in DB, cached in Redis, cache-busted on change

### Security Infrastructure

- Pre-wired Spring Security filter chain (fully overridable via `@ConditionalOnMissingBean`)
- CORS defaults provided, overridable via `application.yml`
- CSRF disabled by default for stateless JWT; enabled automatically in session mode
- Rate limiting per IP and per user via **Bucket4j + Redis**
- Brute-force protection on the login endpoint

---

## Requirements

| Requirement | Minimum Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2 |
| Redis | 6.x |
| Relational DB | PostgreSQL 14+ / MySQL 8+ |

---

## Installation

### Maven

Add the GitHub Packages repository and the dependency to your `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/biruksolomon/spring-auth-core</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>io.github.biruksolomon</groupId>
    <artifactId>spring-auth-core</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/biruksolomon/spring-auth-core")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.biruksolomon:spring-auth-core:1.0.0")
}
```

> **Note:** GitHub Packages requires authentication. Create a [personal access token](https://github.com/settings/tokens) with `read:packages` scope and set it as `GITHUB_TOKEN`.

---

## Quick Start

1. Add the dependency (see [Installation](#installation)).
2. Configure your `application.yml` (see [Configuration Reference](#configuration-reference)).
3. Set required secrets as environment variables:

```bash
export JWT_SECRET="your-256-bit-secret"
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/mydb"
export SPRING_REDIS_HOST="localhost"
```

4. Start your Spring Boot application — all auth endpoints are registered automatically under the configured `base-path`.

No `@EnableXxx` annotation or additional wiring is required. The library registers itself via Spring Boot auto-configuration.

---

## Configuration Reference

All properties live under the `auth.*` namespace. Every property has a sensible default.

```yaml
auth:
  jwt:
    secret: ${JWT_SECRET}          # Required — min 256 bits
    access-token-expiry: 15m       # Default: 15 minutes
    refresh-token-expiry: 7d       # Default: 7 days
    issuer: my-app                 # Default: spring-auth-core

  password:
    min-length: 8                  # Default: 8
    require-special-chars: true    # Default: true
    history-count: 5               # Prevent reuse of last N passwords

  account:
    max-failed-attempts: 5         # Lock after N failures
    lock-duration: 30m             # Auto-unlock after this duration

  email:
    verification-required: true    # Default: true
    token-expiry: 24h              # Default: 24 hours

  mfa:
    enabled: false                 # Default: false

  oauth2:
    enabled: false                 # Default: false
    providers:
      - google
      - github

  api:
    base-path: /api/v1             # Default: /

  rate-limit:
    login:
      requests: 10                 # Max requests
      per: 1m                      # Per time window

  table-prefix: auth_              # Default: auth_ (avoids table name collisions)
```

---

## REST API

All endpoints are automatically registered under `auth.api.base-path` (default: `/`).

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login → returns JWT access + refresh token pair |
| `POST` | `/auth/refresh` | Exchange refresh token for a new access token |
| `POST` | `/auth/logout` | Blacklist the current access token |
| `POST` | `/auth/verify-email` | Verify email using token |
| `POST` | `/auth/forgot-password` | Request a password reset link |
| `POST` | `/auth/reset-password` | Execute password reset using token |
| `POST` | `/auth/mfa/setup` | Generate TOTP QR code for MFA enrollment |
| `POST` | `/auth/mfa/verify` | Verify a TOTP code |
| `GET`  | `/auth/me` | Get current authenticated user profile |
| `PUT`  | `/auth/me` | Update current user profile |

### Admin

> All admin endpoints require the `ADMIN` role.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/admin/users` | List all users (paginated) |
| `GET` | `/admin/users/{id}` | Get a specific user |
| `PUT` | `/admin/users/{id}/roles` | Assign roles to a user |
| `DELETE` | `/admin/users/{id}` | Soft-delete a user |
| `GET` | `/admin/roles` | List all roles |
| `POST` | `/admin/roles` | Create a new role |
| `PUT` | `/admin/roles/{id}/permissions` | Assign permissions to a role |
| `GET` | `/admin/permissions` | List all permissions |
| `POST` | `/admin/permissions` | Create a new permission |
| `GET` | `/admin/audit-log` | Query audit events (filterable, paginated) |

---

## Database Schema

Schema is managed by **Liquibase** (swappable — see [Extensibility](#extensibility--overrides)). All tables use the configurable `auth_` prefix to avoid collisions with your application's tables.

| Table | Purpose |
|---|---|
| `auth_users` | Core user records |
| `auth_roles` | Role definitions |
| `auth_permissions` | Permission definitions |
| `auth_role_permissions` | Role → Permission mapping (many-to-many) |
| `auth_user_roles` | User → Role mapping (many-to-many) |
| `auth_refresh_tokens` | Issued refresh tokens (for rotation + revocation) |
| `auth_api_keys` | Hashed API keys for machine-to-machine auth |
| `auth_audit_log` | Immutable record of every auth event |
| `auth_email_verification_tokens` | Single-use email verification tokens |
| `auth_password_reset_tokens` | Single-use, expiring password reset tokens |

To change the prefix, set `auth.table-prefix` in your `application.yml`. The consuming application can extend the Liquibase changelog by including additional changesets alongside the library's managed migrations.

---

## RBAC — Roles & Permissions

### Defining Permission Checks

Use the `@RequiresPermission` annotation on any controller method or service method:

```java
@GetMapping("/invoices")
@RequiresPermission("invoice:read")
public List<InvoiceDto> listInvoices() { ... }

@DeleteMapping("/invoices/{id}")
@RequiresPermission("invoice:delete")
public void deleteInvoice(@PathVariable Long id) { ... }
```

### Wildcard Permissions

Granting `invoice:*` to a role gives it all `invoice:` actions automatically. This is evaluated at runtime — no code change required when new invoice permissions are added.

### Permission Inheritance

A role can extend another role, inheriting all of its permissions:

```
ADMIN extends MANAGER extends USER
```

Assigning `ADMIN` to a user implicitly grants all `MANAGER` and `USER` permissions.

### Resource-Level Ownership

For endpoints where a user should only modify their own resources, annotate with `@RequiresOwnership`. The library will compare the resource's `ownerId` field to the authenticated user's ID before allowing the operation.

### Permission Caching

All role-permission mappings are cached in Redis. The cache is automatically invalidated whenever a permission or role assignment is modified via the Admin API.

---

## Extensibility & Overrides

Every core bean is annotated `@ConditionalOnMissingBean`. Declare your own bean in your application context and it will take precedence.

### Override the UserDetailsService

```java
@Service
public class MyUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // your custom logic
    }
}
```

### Override the PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new Argon2PasswordEncoder(...); // replace BCrypt(12) default
}
```

### Override the Security Filter Chain

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // fully custom Spring Security configuration
    return http.build();
}
```

### Listen to Auth Events

The library publishes a `Spring ApplicationEvent` for every auth action. Subscribe with `@EventListener`:

```java
@Component
public class AuthEventListener {

    @EventListener
    public void onUserLogin(UserLoginEvent event) {
        System.out.println("User logged in: " + event.getUsername());
    }

    @EventListener
    public void onPasswordChanged(PasswordChangedEvent event) {
        // send notification email, etc.
    }
}
```

### Extend the User Profile

Implement the `UserProfileExtension` interface to attach custom fields to the user entity without modifying the library:

```java
@Component
public class MyProfileExtension implements UserProfileExtension {
    @Override
    public Map<String, Object> getAdditionalFields(Long userId) {
        return Map.of("department", "Engineering", "employeeId", "E-1234");
    }
}
```

### Swap Liquibase for Flyway

Exclude the Liquibase auto-configuration and provide your own `DataSource`-based migration setup. The table structure is documented in [Database Schema](#database-schema) — mirror it with your preferred migration tool.

---

## Testing Support

The library ships a dedicated test module with utilities for integration testing:

### `@WithMockAuthUser`

Injects a mock authenticated user with custom roles and permissions into the Spring Security context:

```java
@Test
@WithMockAuthUser(username = "test@example.com", roles = {"ADMIN"}, permissions = {"user:read", "user:write"})
void adminCanListUsers() throws Exception {
    mockMvc.perform(get("/admin/users"))
           .andExpect(status().isOk());
}
```

### `AuthTestUtils`

Helper class for generating test JWTs and mock user objects:

```java
@Autowired
private AuthTestUtils authTestUtils;

@Test
void protectedEndpointRequiresToken() throws Exception {
    String token = authTestUtils.generateTestJwt("user@example.com", List.of("USER"));

    mockMvc.perform(get("/auth/me")
           .header("Authorization", "Bearer " + token))
           .andExpect(status().isOk());
}
```

### `TestAuthAutoConfiguration`

A lightweight auto-configuration for `@SpringBootTest` that stubs out Redis and the database, so you can test auth logic in isolation without external infrastructure:

```java
@SpringBootTest
@Import(TestAuthAutoConfiguration.class)
class AuthServiceTest { ... }
```

---

## Module Structure

```
spring-auth-core/
├── src/
│   ├── main/
│   │   ├── java/io/github/biruksolomon/auth/
│   │   │   ├── autoconfigure/       # @AutoConfiguration classes
│   │   │   ├── config/              # SecurityConfig, CorsConfig, etc.
│   │   │   ├── controller/          # All REST endpoints
│   │   │   ├── service/             # AuthService, UserService, RoleService, etc.
│   │   │   ├── domain/              # JPA entities (User, Role, Permission, …)
│   │   │   ├── repository/          # Spring Data JPA repositories
│   │   │   ├── security/            # JWT filter, RBAC filter, API key filter
│   │   │   ├── event/               # Auth event classes + publisher
│   │   │   ├── annotation/          # @RequiresPermission, @WithMockAuthUser
│   │   │   ├── dto/                 # Request / response DTOs
│   │   │   ├── exception/           # AuthException hierarchy + global handler
│   │   │   └── properties/          # AuthProperties (@ConfigurationProperties)
│   │   └── resources/
│   │       ├── META-INF/spring/
│   │       │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   │       └── db/changelog/        # Liquibase migration changesets
│   └── test/                        # Unit + integration tests
└── pom.xml                          # <packaging>jar</packaging>
                                     # No spring-boot-maven-plugin
```

---

## Publishing

### Versioning

This project follows [Semantic Versioning](https://semver.org/):

| Version bump | When to use |
|---|---|
| Patch (`1.0.x`) | Bug fixes, non-breaking internal changes |
| Minor (`1.x.0`) | New features, new optional config properties |
| Major (`x.0.0`) | Breaking API or configuration changes |

### GitHub Packages (Primary Target)

Publishing is automated via GitHub Actions. A new package version is published whenever a version tag is pushed:

```bash
git tag v1.0.0
git push origin v1.0.0
```

The workflow runs: **build → test → publish to GitHub Packages**.

### Maven Central (Optional)

When the project is ready for public distribution, publish to Maven Central via [Sonatype OSSRH](https://central.sonatype.org/). Requires:

1. A Sonatype account linked to `io.github.biruksolomon`
2. GPG signing configured in the GitHub Actions workflow
3. `OSSRH_USERNAME` and `OSSRH_PASSWORD` set as repository secrets

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
4. Open a pull request against `main`

Please ensure all tests pass (`./mvnw verify`) and new features include corresponding unit or integration tests before submitting a PR.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

<p align="center">
  Built by <a href="https://github.com/biruksolomon">biruksolomon</a>
</p>

