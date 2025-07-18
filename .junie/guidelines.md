# Bookify Project Guidelines

## Table of Contents
- [Project Overview](#project-overview)
- [Architecture and Design Principles](#architecture-and-design-principles)
- [Code Organization and Package Structure](#code-organization-and-package-structure)
- [Coding Standards and Conventions](#coding-standards-and-conventions)
- [Testing Guidelines](#testing-guidelines)
- [Git Workflow and Commit Message Standards](#git-workflow-and-commit-message-standards)
- [Documentation Standards](#documentation-standards)
- [Build and Deployment Guidelines](#build-and-deployment-guidelines)

## Project Overview

Bookify is a library management system built with Spring Boot that allows users to manage books, members, and borrowing operations. The application provides functionality for:

- Managing books (adding, removing, searching)
- Managing library members
- Handling book borrowing and returns
- Tracking book availability

The system is designed with a modular architecture using Spring Modulith to ensure clear boundaries between different functional areas.

## Architecture and Design Principles

### Modular Architecture

Bookify follows a modular architecture using Spring Modulith. Each module represents a distinct functional area with clear responsibilities:

- **Books Module**: Manages the book catalog
- **Members Module**: Manages library members and borrowing operations
- **Events Module**: Defines events for inter-module communication
- **Config Module**: Manages application configuration

### Design Principles

1. **Separation of Concerns**: Each module has a clear, single responsibility
2. **Encapsulation**: Module internals are hidden using package-private access modifiers
3. **Domain-Driven Design**: The codebase is organized around business domains
4. **Event-Driven Communication**: Modules communicate through events to maintain loose coupling
5. **Repository Pattern**: Data access is abstracted through repositories
6. **Dependency Injection**: Dependencies are injected through constructors
7. **Immutability**: Service dependencies are declared as final fields

## Code Organization and Package Structure

### Package Structure

```
org.jetbrains.conf.bookify
├── BookifyApplication.java
├── books
│   ├── Book.java
│   ├── BookController.java
│   ├── BookRepository.java
│   ├── BookService.java
│   └── package-info.java
├── members
│   ├── Borrowing.java
│   ├── BorrowingController.java
│   ├── BorrowingRepository.java
│   ├── BorrowingService.java
│   ├── BorrowingStatus.java
│   ├── Member.java
│   ├── MemberController.java
│   ├── MemberRepository.java
│   ├── MemberService.java
│   └── package-info.java
├── events
│   ├── BookAvailabilityCheckedEvent.java
│   ├── BookBorrowRequestEvent.java
│   ├── BookReturnedEvent.java
│   └── package-info.java
└── config
    ├── BookifySettingsConfig.java
    └── package-info.java
```

### Module Structure

Each module follows a consistent structure:

- **Entity Classes**: Define the domain model (e.g., Book.java, Member.java)
- **Repository Interfaces**: Define data access operations (e.g., BookRepository.java)
- **Service Classes**: Implement business logic (e.g., BookService.java)
- **Controller Classes**: Define REST API endpoints (e.g., BookController.java)
- **package-info.java**: Documents the module's purpose and defines module boundaries

## Coding Standards and Conventions

### Java Version

- Use Java 21 features where appropriate
- Follow modern Java practices (e.g., var for local variables, records for DTOs)

### Naming Conventions

- **Classes**: PascalCase, descriptive nouns (e.g., `BookService`)
- **Methods**: camelCase, verb phrases describing actions (e.g., `addBook`)
- **Variables**: camelCase, descriptive nouns (e.g., `bookRepository`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_BOOKS_ALLOWED`)
- **Packages**: lowercase, domain-based naming (e.g., `org.jetbrains.conf.bookify.books`)

### Access Modifiers

- Use package-private (default) access for classes that should not be accessed outside their module
- Use public access only for classes that are part of the module's API
- Make fields private and provide accessors when needed
- Declare service dependencies as final fields

### Code Style

- Use 4 spaces for indentation
- Maximum line length of 120 characters
- Use blank lines to separate logical blocks of code
- Group related methods together
- Place opening braces on the same line as the declaration
- Always use braces for control structures, even for single-line blocks

### Exception Handling

- Use unchecked exceptions for programming errors
- Use checked exceptions for recoverable conditions
- Provide meaningful exception messages
- Document exceptions in JavaDoc
- Use Optional for methods that may not return a value

### Transactions

- Use @Transactional annotations on service methods
- Specify readOnly=true for methods that don't modify data
- Define appropriate propagation levels for event handlers

## Testing Guidelines

### Test Structure

- Use JUnit 5 (Jupiter) for all tests
- Use descriptive test method names that explain the scenario and expected outcome
- Follow the Arrange-Act-Assert pattern
- Each test should focus on a single functionality
- Use appropriate test annotations (@Test, @ParameterizedTest, etc.)

### Test Types

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test interactions between components
- **API Tests**: Test REST endpoints using MockMvc
- **Modulith Tests**: Test module boundaries and interactions

### Testing Best Practices

- Use AssertJ for fluent assertions
- Use meaningful test data
- Clean up test data after tests
- Use @SpringBootTest for integration tests
- Use TestContainers for database tests
- Mock external dependencies when appropriate

## Git Workflow and Commit Message Standards

### Branching Strategy

- **main**: Production-ready code
- **develop**: Integration branch for features
- **feature/xxx**: Feature branches
- **bugfix/xxx**: Bug fix branches
- **release/xxx**: Release preparation branches

### Commit Messages

Follow the Conventional Commits specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

Types:
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **perf**: Performance improvements
- **test**: Adding or correcting tests
- **chore**: Changes to the build process or auxiliary tools

Example:
```
feat(books): add ISBN validation for book creation

- Add validation for ISBN format
- Reject invalid ISBNs with appropriate error message

Closes #123
```

### Pull Requests

- Keep PRs focused on a single change
- Provide a clear description of the changes
- Reference related issues
- Ensure all tests pass before requesting review
- Address review comments promptly

## Documentation Standards

### Code Documentation

- Use JavaDoc for all public classes and methods
- Document package structure using package-info.java files
- Include purpose, parameters, return values, and exceptions in JavaDoc
- Keep comments up-to-date with code changes
- Use inline comments for complex logic

### JavaDoc Format

```java
/**
 * Brief description of the method.
 *
 * <p>Detailed description if needed.</p>
 *
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 */
```

### README and Project Documentation

- Maintain an up-to-date README.md with:
    - Project overview
    - Setup instructions
    - Usage examples
    - Contributing guidelines
- Document architecture decisions in a separate document
- Keep API documentation up-to-date

## Build and Deployment Guidelines

### Build Process

- Use Maven for building the application
- Follow the standard Maven lifecycle
- Use the Spring Boot Maven Plugin for building executable JARs
- Run tests as part of the build process

### Environment Configuration

- Use application.properties for configuration
- Use profiles for environment-specific configuration (dev, test, prod)
- Externalize sensitive configuration (credentials, API keys)
- Use @ConfigurationProperties for typed configuration

### Deployment

- Use Docker for containerization
- Use Docker Compose for local development
- Follow the 12-factor app methodology
- Implement health checks
- Configure appropriate logging
- Set up monitoring and alerting

### Database Management

- Use Flyway for database migrations
- Version migration scripts (V1__description.sql)
- Never modify existing migration scripts
- Test migrations before deployment