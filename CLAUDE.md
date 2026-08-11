# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run (dev profile — starts Docker Compose automatically)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BookControllerTest

# Run a single test method
./mvnw test -Dtest=BookControllerTest#testAddBook

# Build with AOT processing (the aot Maven profile must be explicitly activated)
./mvnw clean verify -Paot
```

Tests require Docker (Testcontainers spins up `postgres:17-alpine`). The dev profile starts the database via Spring
Boot's Docker Compose integration (`compose.yaml`).

## Architecture

This is a **Spring Modulith** modular monolith. The root package is `org.jetbrains.conf.bookify`, and Spring Modulith
enforces module boundaries at compile time — the `ModulithTests.verifyModulithStructure` test will fail if cross-module
access rules are violated.

### Modules

| Package    | Responsibility                                        |
|------------|-------------------------------------------------------|
| `books`    | Book catalog, availability tracking                   |
| `members`  | Members, borrowing records, borrow/return lifecycle   |
| `payments` | Fine rate management, overdue fine processing         |
| `events`   | Shared event record types (the only cross-module API) |
| `config`   | Security, AOT, global settings                        |

**Modules communicate exclusively via Spring Application Events** defined in the `events` package. Direct cross-module
bean injection is not allowed by Modulith. Use `@ApplicationModuleListener` for event consumers.

**Persistence**: this branch (`main`) uses Spring Data JDBC — entities implement `Persistable<UUID>` with a
`@PersistenceCreator` constructor, repositories extend `CrudRepository`/`ListCrudRepository` with native-SQL `@Query`
methods. The `spring-data-jpa` branch uses Spring Data JPA/Hibernate instead (`@Entity`, `JpaRepository`, JPQL
`@Query`). The persistence-layer shape of entities/repositories is not copy-paste compatible between the two branches.

### Borrow/Return Flow

1. `BorrowingService.borrowBook()` — creates a `PENDING` borrowing, publishes `BookBorrowRequestEvent`
2. `BookService.handleBookBorrowedEvent()` — checks availability, publishes `BookAvailabilityCheckedEvent`
3. `BorrowingService.handleBookAvailabilityCheckedEvent()` — transitions to `APPROVED` (sets `book` + `borrowDate`) or
   `REJECTED`
4. `BorrowingService.returnBook()` — sets `RETURNED` + `returnDate`, publishes `BookReturnedEvent`
5. `BookService.handleBookReturnedEvent()` — marks book available again

The `Borrowing` entity has two book references: `requestedBook` (always set, the originally requested book) and `book`
(only set on `APPROVED`, the actually borrowed book).

### Security

HTTP Basic Auth. `LIBRARIAN` role is required for `POST /api/members`, `POST /api/books`, `DELETE /api/books/**`,
`PUT /api/members/**`, `PUT /api/books`, and `GET /api/members/active`. All other endpoints are anonymous. Users are
stored in the database and managed via `JdbcUserDetailsManager`.

Two `SecurityFilterChain` beans exist in `SecurityConfig`: the default one above is active unless the
`strict-security` Spring profile is on, in which case a stricter variant requires `ADMIN` instead of `LIBRARIAN` for
the same endpoints — reading the source alone doesn't tell you which is enforced at runtime; that depends on the
active profile. `BookService.removeBook` is additionally guarded by `@PreAuthorize("hasRole('LIBRARIAN')")`
(`@EnableMethodSecurity` is on), demonstrating that unlocking the HTTP-level rule also satisfies the method-level one,
since both layers require the same role.

`MemberService.disableMember` demonstrates the opposite case: it's guarded by
`@PreAuthorize("hasRole('SUPERVISOR')")`, a role that never appears in `SecurityConfig`. A librarian passes the
HTTP-level matcher for `PUT /api/members/**` (which only checks `LIBRARIAN`) but is still denied at the method layer,
since `SUPERVISOR` isn't part of the authorities the HTTP rule grants. There's no local account with `SUPERVISOR`, and
the inlay has no visibility into method-security annotations, so simple unlock (which only grants what the HTTP layer
requires) doesn't help either — reaching this method requires "Unlock with custom authorities" with a role list that
includes `SUPERVISOR` alongside `LIBRARIAN`. `AccessDeniedException` (thrown by a denied `@PreAuthorize` check) is
mapped to HTTP 403 by `ErrorControllerAdvice`, ahead of its catch-all `Exception` handler.

`MemberController.getAllActive` demonstrates a role actually changing *execution flow*, not just gating access:
the HTTP-level matcher for `GET /api/members/active` only ever checks `LIBRARIAN`, but the handler itself inspects
the injected `Authentication` and takes a different branch if `ROLE_ADMIN` is also present — `memberService.findAll()`
(every member, disabled included) instead of `memberService.findAllActive()`. Testing this by unlocking with custom
authorities `LIBRARIAN, ADMIN` (instead of a plain `LIBRARIAN` unlock) changes the response body, not just the status
code — one seeded member (`Alice Cooper`) is disabled specifically so the difference is visible.

### Configuration

Business rules are externalized in `@ConfigurationProperties` classes — see "Configuration properties" under
Conventions for the binding rule every `bookify.*` property must follow.

- `BookifySettingsConfig` (`@ConfigurationProperties(prefix = "bookify")`, public, in `config`, registered via
  `BookifyApplication`'s `@EnableConfigurationProperties({...})`):
  - `bookify.maximum.books.borrowed` — max active borrowings per member (default 5 in dev)
  - `bookify.overdue.days` — days before a borrowing is considered overdue (default 14 in dev)
- `PaymentProviderProperties` (`@ConfigurationProperties(prefix = "bookify.payments")`, package-private, in
  `payments`, registered locally via `PaymentProviderConfiguration`'s `@EnableConfigurationProperties` — not added to
  `BookifyApplication`, since `payments`' `allowedDependencies = {"events"}` blocks it from referencing `config`):
  - `bookify.payments.provider` — `mock` (default) or `production`; also selects the active `PaymentProvider` bean
    via `@ConditionalOnProperty` on `MockPaymentProvider`/`ProductionPaymentProvider`. Settable via env var
    `BOOKIFY_PAYMENTS_PROVIDER`.
  - `bookify.payments.production.base-url` / `bookify.payments.production.api-key` — used by
    `ProductionPaymentProvider`'s HTTP client to call the external payment gateway

### Database

Flyway manages schema migrations in `src/main/resources/db/migration/`. The dev profile also loads seed data from
`src/main/resources/data/`. Tests use `src/test/resources/test-data/`.

Spring Modulith's event outbox uses `event_publication` and `event_publication_archive` tables, auto-created via
`spring.modulith.events.jdbc.schema-initialization.enabled`. It's explicitly set in `application-test.properties` (so
tests always have the tables); it's commented out in `application-dev.properties` — check it before relying on
durable event publication outside tests. The `spring-data-jpa` branch additionally ships an explicit Flyway migration
for the same tables (`V11__modulith_events.sql`).

### Testing

- `DbConfiguration` provides the shared Testcontainers `PostgreSQLContainer` bean via `@ServiceConnection`
- All integration tests use `@ActiveProfiles("test")` and `@Import(DbConfiguration.class)`
- Controller tests use `MockMvcTester` (Spring Boot 4 WebMVC test API), not `MockMvc` directly
- The test librarian credentials are `testlibrarian:password` (loaded from test seed data)

## Conventions

### Configuration properties

Every `bookify.*` property must be bound to a field on a `@ConfigurationProperties` class — never read ad hoc via
`@Value("${bookify...}")` or `Environment.getProperty(...)`. This includes properties that only exist to select
between conditional beans (e.g. a `@ConditionalOnProperty` toggle): give them a matching bound field too, even though
the condition itself reads the raw `Environment` value independently, so the property stays typed and discoverable
in one place instead of only living as a string literal on an annotation.

Where a property is bound depends on which module owns it:

- Cross-cutting settings (used by more than one module, or with no natural module owner) go on
  `config.BookifySettingsConfig`, registered in `BookifyApplication`'s `@EnableConfigurationProperties({...})`.
- Module-specific settings get their own package-private `@ConfigurationProperties` class inside that module (e.g.
  `payments.PaymentProviderProperties`), registered via a local package-private `@Configuration` class with
  `@EnableConfigurationProperties(...)` in the same package — not added to `BookifyApplication` — whenever the
  module's `allowedDependencies` don't permit referencing `config` directly.

### Module encapsulation

Classes that are internal to a module must use **package-private** (default) access — not `public`. Only types that
other modules need to reference (primarily the event records in `events`) should be `public`. This is how Modulith
enforces boundaries at the Java level.

### Dependency injection

Service dependencies are always injected via **constructor**, declared as `final` fields.

### Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`
Scopes match module names: `books`, `members`, `payments`, `events`, `config`

Example: `feat(books): add ISBN validation for book creation`

### Branching

`main` → production, `develop` → integration, `feature/xxx`, `bugfix/xxx`, `release/xxx`

### Database migrations

Never modify existing migration scripts. Always add new versioned scripts (`V<n>__description.sql`).
