# Bookify Application with Spring Modulith

This project demonstrates the use of Spring Modulith to create a modular application structure for a book management system.

## Application Architecture

### Spring Data Repositories AOT implementation

To run the application with AOT enabled, do the following:
* In the `spring.properties` file set `spring.aot.enabled=false`
* Build the application
```shell
./mvnw clean verify
```
* Set `spring.aot.enabled` to `true`
* Run the application
```shell
set SPRING_PROFILES_ACTIVE=aot
./mvnw spring-boot:run     
```
The application should display in the console:
```shell
Starting AOT-processed BookifyApplication using Java 26...
```
⚠️ Spring Boot Docker Compose integration DOES NOT WORK in AOT mode


### Modules

The application is organized into the following modules:

1. **Books Module** (`org.jetbrains.conf.bookify.books`)
   - Responsible for managing books in the system
   - Provides functionality for adding, removing, and searching for books
   - Defined with `@ApplicationModule` annotation in `package-info.java`

2. **Members Module** (`org.jetbrains.conf.bookify.members`)
   - Responsible for managing members of the service
   - Handles member registration, profile management, and authentication
   - Defined with `@ApplicationModule` annotation in `package-info.java`
   - Has an allowed dependency on the Books module

### Module Structure

Each module follows these Spring Modulith best practices:

1. **Clear Module Boundaries**
   - Each module is contained in its own package
   - Module boundaries are explicitly defined using `package-info.java` files
   - Dependencies between modules are explicitly declared

2. **Internal Structure**
   - Each module has a clear internal structure with appropriate classes
   - Entity classes (e.g., `Book`, `Member`) define the domain model
   - Repository classes handle data access
   - Service classes implement business logic
   - Controller classes expose REST APIs

### Data Models

#### Books Module
- **Book Entity**
  - `id` (UUID): Primary key
  - `name` (String): Name of the book
  - `isbn` (String): ISBN of the book

#### Members Module
- **Member Entity**
  - `id` (UUID): Primary key
  - `name` (String): Name of the member
  - `email` (String): Email address of the member
  - `password` (String): Password for authentication
  - `enabled` (boolean): Whether the member account is active

### REST API Endpoints

#### Books Module
- `GET /api/books`: Get all books in the catalogue
- `POST /api/books`: Add a book to the catalogue
- `DELETE /api/books/{id}`: Remove a book from the catalogue
- `GET /api/books/search?name={name}`: Search for books by name

#### Members Module
- `GET /api/members`: Get all members
- `GET /api/members/active`: Get all active members
- `POST /api/members`: Add a new member
- `PUT /api/members/{id}/disable`: Disable a member
- `GET /api/members/search?name={name}&email={email}`: Search for members by name or email
- `GET /api/members/{id}`: Get a member by id

### Database Structure

The application uses a PostgreSQL database with the following tables:

1. **book**
   - `id` (UUID, primary key)
   - `name` (varchar)
   - `isbn` (varchar)

2. **member**
   - `id` (UUID, primary key)
   - `name` (varchar)
   - `email` (varchar)
   - `password` (varchar)
   - `enabled` (boolean, default true)

Database migrations are managed using Flyway, with migration scripts in the `src/main/resources/db/migration` directory.

### Docker Setup

The application includes a Docker Compose configuration for running the PostgreSQL database:

```yaml
services:
  postgres:
    image: 'postgres:17-alpine'
    environment:
      - 'POSTGRES_DB=bookify'
      - 'POSTGRES_PASSWORD=secret'
      - 'POSTGRES_USER=bookify'
    ports:
      - '5432'
```
Spring Boot Docker Compose integration DOES NOT WORK in AOT mode

## Testing with Spring Modulith

The project includes the following tests:

1. **Application Structure Tests** (`ModulithTests.java`)
   - Verifies that the overall application structure follows Spring Modulith conventions
   - Generates documentation of the application's modular structure

2. **Module-Specific Tests**
   - `BooksModuleTests.java` - Verifies the structure and functionality of the Books module
   - `MembersModuleTests.java` - Verifies the structure of the Members module
   - `BookControllerTest.java` - Tests the REST API for the Books module
   - `MemberControllerTest.java` - Tests the REST API for the Members module

## Benefits of Spring Modulith

Using Spring Modulith in this application provides several benefits:

1. **Improved Maintainability**
   - Clear module boundaries make the codebase easier to understand and maintain
   - Changes to one module have minimal impact on other modules

2. **Better Testability**
   - Modules can be tested in isolation
   - Integration tests can focus on specific module interactions

3. **Enhanced Documentation**
   - Spring Modulith automatically generates documentation of the application's structure
   - Module boundaries and dependencies are explicitly documented

4. **Evolutionary Architecture**
   - Modules can evolve independently
   - The application can be refactored or extended more easily
