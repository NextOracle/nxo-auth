# nxo-auth — Spring Boot Auth Starter

> **`nxo-auth`** is a Spring Boot auto-configuration library that drops full authentication infrastructure into any Spring Boot application with zero boilerplate. It ships JWT (RS256), OAuth2 social login, WebAuthn (passkeys), role-based access control, rate limiting, and database-managed users — all driven by Liquibase migrations.

---

## Table of Contents

1. [Requirements](#requirements)
2. [Tech Stack](#tech-stack)
3. [Installation](#installation)
4. [Quick Start](#quick-start)
5. [Configuration Reference](#configuration-reference)
6. [Database & Liquibase](#database--liquibase)
7. [Roles](#roles)
8. [REST API](#rest-api)
9. [Security Annotations](#security-annotations)
10. [OAuth2 / Social Login](#oauth2--social-login)
11. [WebAuthn (Passkeys)](#webauthn-passkeys)
12. [Rate Limiting](#rate-limiting)
13. [JWT Details](#jwt-details)
14. [Building from Source](#building-from-source)
15. [Publishing to GitHub Packages](#publishing-to-github-packages)
16. [License](#license)

---

## Requirements

| Requirement | Minimum version |
|---|---|
| **Java** | 25 |
| **Spring Boot** | 4.1.0 |
| **Maven** | 3.9+ |
| **Database** | Any JDBC-compatible DB supported by Liquibase (PostgreSQL recommended) |

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Framework | Spring Boot | 4.1.0 |
| Security | Spring Security | (managed by Boot) |
| JWT | JJWT (jjwt-api / impl / jackson) | 0.13.0 |
| WebAuthn | Yubico `webauthn-server-core` | 2.9.0 |
| Rate Limiting | Bucket4j | 8.10.1 |
| DB Migrations | Liquibase (via Spring Boot starter) | (managed by Boot) |
| ORM | Spring Data JPA / Hibernate | (managed by Boot) |
| Mapping | MapStruct | 1.6.3 |
| Boilerplate | Lombok | (managed by Boot) |
| OpenAPI | Swagger Annotations v3 | 2.2.28 |

---

## Installation

The library is published to **GitHub Packages**. Add the repository and dependency to your consuming project.

### Maven

Add the GitHub Packages repository to your `pom.xml` (or `~/.m2/settings.xml`):

```xml
<repositories>
  <repository>
    <id>github-nxo-auth</id>
    <url>https://maven.pkg.github.com/NextOracle/nxo-auth</url>
  </repository>
</repositories>
```

Add the dependency:

```xml
<dependency>
  <groupId>org.nextoracle</groupId>
  <artifactId>nxo-auth</artifactId>
  <version>0.0.4</version>
</dependency>
```

> **Authentication:** GitHub Packages requires a personal access token (PAT) with `read:packages` scope.  
> Add the following to `~/.m2/settings.xml`:
> ```xml
> <servers>
>   <server>
>     <id>github-nxo-auth</id>
>     <username>YOUR_GITHUB_USERNAME</username>
>     <password>YOUR_GITHUB_PAT</password>
>   </server>
> </servers>
> ```

### Gradle

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/NextOracle/nxo-auth")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key")  as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.nextoracle:nxo-auth:0.0.4")
}
```

---

## Quick Start

1. Add the dependency (see [Installation](#installation)).
2. Add a JDBC data source to your `application.yml`.
3. Configure the required `nxo-auth` properties (see below).
4. Run your application — Liquibase creates all auth tables automatically.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: postgres
    password: secret
  jpa:
    hibernate:
      ddl-auto: none        # Liquibase owns the schema

nxo-auth:
  frontend-url: http://localhost:3000
  admin:
    username: admin
    mail: admin@example.com
    password: changeme
    role: ADMIN
  jwt:
    secret: your-256-bit-or-rsa-key
    expiration: 3600000     # 1 hour in ms
```

No `@ComponentScan`, `@EnableJpaRepositories`, or `@EntityScan` are needed in your application — the library registers everything automatically via Spring Boot's auto-configuration mechanism.

> **Note:** If your application has its own JPA entities/repositories in a *different* package, add the following to your main class to cover both packages:
> ```java
> @EntityScan(basePackages = {"com.yourapp.entity", "org.nextoracle.entity"})
> @EnableJpaRepositories(basePackages = {"com.yourapp.repository", "org.nextoracle.repository"})
> ```

---

## Configuration Reference

All properties live under the `nxo-auth` prefix.

```yaml
nxo-auth:
  # Base URL of the front-end (used for OAuth2 post-login redirects)
  frontend-url: https://app.example.com

  # URL patterns that bypass JWT authentication entirely
  whitelist:
    - /api/public/**
    - /actuator/health

  # Seeded admin account (created on first startup if it doesn't exist)
  admin:
    username: admin
    mail: admin@example.com
    password: changeme          # BCrypt-hashed before storage
    role: ADMIN

  # OAuth2 social login
  oauth2:
    redirect-url: https://app.example.com/oauth2/callback?token=

  # JWT settings
  jwt:
    expiration: 3600000         # access-token TTL in milliseconds
    cookie:
      http-only: true
      secure: true
      max-age: 604800000        # refresh-token cookie TTL (7 days)
      same-site: Strict

  # WebAuthn (passkeys)
  webauthn:
    rp-id: app.example.com
    rp-name: My Application
    origin: https://app.example.com
```

---

## Database & Liquibase

`nxo-auth` manages its own schema through **Liquibase**. Migrations run automatically on startup via the Spring Boot Liquibase integration.

### Tables created

| Table | Purpose |
|---|---|
| `auth_role` | Role definitions (`ar_id` UUID PK, `ar_name`, `ar_description`) |
| `auth_user` | User accounts (`au_id` UUID PK, username, email, provider, avatar, password hash, etc.) |
| `auth_role_user` | Many-to-many join between users and roles |

### Custom changelog location

The library's changelog is located at:

```
classpath:db/changelog/nxo-auth-changelog.yaml
```

If you need to add your own migrations, **do not** edit the library's changelog. Instead configure your own changelog in `application.yml`:

```yaml
spring:
  liquibase:
    change-log: classpath:db/changelog/your-app-changelog.yaml
```

Then include the library's changelog from yours:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/nxo-auth-changelog.yaml
      relativeToChangelogFile: false
  - include:
      file: db/changelog/your-changes.yaml
      relativeToChangelogFile: false
```

---

## Roles

Built-in roles are defined in the `AppRole` enum:

| Role | Description |
|---|---|
| `ADMIN` | Full administrative access |
| `USER` | Base authenticated user |
| `STANDARD` | Standard subscription tier |
| `PREMIUM` | Premium subscription tier |
| `ENTERPRISE` | Enterprise subscription tier |

Roles are seeded in the `auth_role` table on startup. Users are assigned roles via the `auth_role_user` join table.

---

## REST API

All endpoints are prefixed with `/api/management/auth`.

### Authentication

| Method | Path | Auth required | Description |
|---|---|---|---|
| `POST` | `/api/management/auth/login` | ❌ | Authenticate with username + password. Returns a JWT access token in the body and sets a `refresh_token` HttpOnly cookie. |
| `POST` | `/api/management/auth/refresh` | 🍪 refresh cookie | Exchange a valid refresh-token cookie for a new access token. |
| `GET` | `/api/management/auth/me` | ✅ JWT | Returns the currently authenticated user. |
| `POST` | `/api/management/auth/logout` | ✅ JWT | Clears the refresh-token cookie. |

#### Login request body

```json
{
  "username": "admin",
  "password": "changeme"
}
```

#### Login response body

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer"
}
```

### WebAuthn

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/webauthn/register/start` | Begin passkey registration |
| `POST` | `/api/webauthn/register/finish` | Complete passkey registration |
| `POST` | `/api/webauthn/login/start` | Begin passkey authentication |
| `POST` | `/api/webauthn/login/finish` | Complete passkey authentication |

### JWKS

| Method | Path | Description |
|---|---|---|
| `GET` | `/.well-known/jwks.json` | Public RSA key set for JWT verification |

---

## Security Annotations

Protect your own endpoints with the built-in method-security annotations:

```java
import org.nextoracle.aspect.*;

@RestController
public class MyController {

    @GetMapping("/admin-only")
    @IsAdmin
    public String adminEndpoint() { ... }

    @GetMapping("/premium-feature")
    @IsPremium
    public String premiumEndpoint() { ... }

    @GetMapping("/enterprise-feature")
    @IsEnterprise
    public String enterpriseEndpoint() { ... }

    @GetMapping("/standard-feature")
    @IsStandard
    public String standardEndpoint() { ... }

    @GetMapping("/freemium-feature")
    @IsFreemium
    public String freemiumEndpoint() { ... }

    // Or check for a specific role dynamically
    @GetMapping("/custom")
    @HasRole("PREMIUM")
    public String customRoleEndpoint() { ... }
}
```

---

## OAuth2 / Social Login

`nxo-auth` ships a Google OAuth2 integration out of the box. After a successful Google login, the user is created or updated in `auth_user` and redirected to `nxo-auth.oauth2.redirect-url` with the JWT appended as a query parameter.

### Configure Google credentials

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope: openid, email, profile

nxo-auth:
  oauth2:
    redirect-url: https://app.example.com/oauth2/callback?token=
```

---

## WebAuthn (Passkeys)

WebAuthn support is provided by the **Yubico `webauthn-server-core`** library. Credentials are stored in the `webauthn_credential` table (managed by Liquibase).

### Minimum configuration

```yaml
nxo-auth:
  webauthn:
    rp-id: app.example.com          # must match the browser's origin hostname
    rp-name: My Application
    origin: https://app.example.com
```

The registration and authentication flows follow the standard WebAuthn ceremony:  
`start` → browser calls `navigator.credentials.create()` / `get()` → `finish`.

---

## Rate Limiting

Incoming requests are rate-limited per IP address using **Bucket4j**. The `RateLimitFilter` is registered automatically. No extra configuration is required for default limits.

---

## JWT Details

- **Algorithm:** RS256 (RSA asymmetric key pair)
- **Key management:** RSA key pair auto-generated on startup (`RsaKeyConfig`) and exposed via `/.well-known/jwks.json`
- **Claims:** `sub` (username), `userId`, `roles`, `provider`
- **Refresh tokens:** Opaque tokens stored server-side, delivered via HttpOnly cookie
- **Cookie properties:** configurable `httpOnly`, `secure`, `sameSite`, `maxAge`

---

## Building from Source

```bash
# Clone
git clone https://github.com/NextOracle/nxo-auth.git
cd nxo-auth

# Build (produces target/nxo-auth.jar — plain JAR, not a fat JAR)
./mvnw clean package -DskipTests

# Run tests
./mvnw test
```

> The Spring Boot repackage goal is **skipped** intentionally. `nxo-auth` is a library, not a standalone application, so it produces a plain JAR suitable for inclusion as a dependency.

### Docker / Compose (development database)

A `compose.yaml` is included for spinning up a local PostgreSQL instance:

```bash
docker compose up -d
```

---

## Publishing to GitHub Packages

```bash
./mvnw deploy
```

Requires a `~/.m2/settings.xml` entry for the `github` server ID with a PAT that has `write:packages` scope:

```xml
<servers>
  <server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_PAT</password>
  </server>
</servers>
```

---

## License

This project is licensed under the **MIT License** — see [https://opensource.org/licenses/MIT](https://opensource.org/licenses/MIT) for details.

---

> Made with ☕ by [NextOracle](https://github.com/NextOracle) · [dev@nextoracle.org](mailto:dev@nextoracle.org)
