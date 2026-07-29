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
(`@EnableMethodSecurity` is on), demonstrating that unlocking the HTTP-level rule also satisfies the method-level one.

### Configuration

Business rules are externalized in `BookifySettingsConfig` (`@ConfigurationProperties(prefix = "bookify")`):

- `bookify.maximum.books.borrowed` — max active borrowings per member (default 5 in dev)
- `bookify.overdue.days` — days before a borrowing is considered overdue (default 14 in dev)

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
