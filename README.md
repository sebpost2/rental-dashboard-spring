# Rental Dashboard API — Spring Boot edition

![ci](https://github.com/sebpost2/rental-dashboard-spring/actions/workflows/ci.yml/badge.svg)

**Live demo:** https://rental-dashboard-java.vercel.app (click "Ver demo en vivo") · **API docs:** https://rental-dashboard-spring.onrender.com/swagger-ui.html

A rental-property ledger: each owner tracks income and expenses per property and gets a monthly summary, a month-by-month chart and a CSV export. This is the Spring Boot rewrite of a FastAPI backend — the same Next.js frontend runs unchanged against either one.

> The free Render instance sleeps when idle; the first request can take up to a minute.

## Stack

Java 25 · Spring Boot 4 · Spring MVC · Spring Data JPA (Hibernate 7) · Flyway · PostgreSQL · Spring Security (OAuth2 resource server, HS256 JWT) · Bean Validation · springdoc-openapi · Bucket4j · JUnit 5 + MockMvc + Testcontainers · JaCoCo · Docker · GitHub Actions · Render · Neon

## Architecture

Package by feature, layered inside each feature:

```
com.sebpostigo.rental
├── auth       register, login, logout, me
├── property   owner-scoped property CRUD
├── ledger     incomes and expenses nested under a property
├── report     summary, monthly timeseries, CSV export
├── demo       self-resetting demo account (demo profile)
├── security   JWT, cookie token resolver, origin check, rate limit
└── common     ProblemDetail errors, config, health, OpenAPI
```

Requests flow controller → service → repository. Controllers only map HTTP to record DTOs; entities never leave the service layer.

## Key decisions

- **Flyway owns the schema; Hibernate runs with `ddl-auto=validate`**, so the app refuses to start if entities and database drift apart.
- **Money is `BigDecimal` / `numeric(10,2)`**, validated with `@Digits(integer = 8, fraction = 2)` so oversized or over-precise amounts are a 422, not a database error.
- **Reports aggregate in SQL** (`JdbcClient`, `SUM` / `GROUP BY to_char(date, 'YYYY-MM')`) instead of loading every row.
- **Ownership is part of every query** (`findByIdAndOwnerId`), and another user's data is a 404 — never a 403 — so ids can't be probed.
- **Stateless JWT in an httpOnly cookie**, validated by Spring Security's resource server with a custom `BearerTokenResolver` (cookie first, then `Authorization` header). Public endpoints ignore the cookie, so a stale one can't block login.
- **CSRF: an Origin check instead of CSRF tokens.** The API is stateless and cross-site (`SameSite=None` in production); login and logout reject a foreign `Origin`, and every other endpoint needs a JWT the attacker can't read.
- **422 for validation errors** (Spring's default is 400) to keep the API contract identical to the FastAPI version.
- **Demo data resets on every startup** and follows the calendar, so the demo is always clean and current.

## Run it

```bash
docker compose up --build        # API on :8080 with Postgres and the demo account
./mvnw verify                    # tests (needs Docker) + 80% coverage gate
```

Demo login: `demo@rentalledger.com` / `demopass123`.

## Tests

62 tests: MockMvc integration tests against a real Postgres 17 in Testcontainers (auth, properties, ledger, reports, schema, demo seeder) plus JWT unit tests. CI runs `./mvnw verify` on every push.

## Ported from FastAPI

Same routes, status codes, snake_case JSON and error `detail` strings. Two bugs in the original were fixed along the way: deleting a property with ledger entries now cascades (the original had no `ON DELETE CASCADE`), and the demo data uses expense categories the API actually accepts.
