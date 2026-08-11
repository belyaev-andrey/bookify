package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.books.BookDeleteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ControllerAdvice
class ErrorControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAdvice.class);
    private static final Set<String> DEVELOPMENT_PROFILES = Set.of("dev", "test");

    private final Environment environment;

    ErrorControllerAdvice(Environment environment) {
        this.environment = environment;
    }

    /**
     * Handle BookHasBorrowingsException - when attempting to delete a book with borrowing records.
     * Returns HTTP 409 CONFLICT with a meaningful error message.
     */
    @ExceptionHandler(value = BookDeleteException.class, produces = "application/json")
    public ResponseEntity<Map<String, String>> handleBookHasBorrowingsException(BookDeleteException ex) {
        log.warn("Attempt to delete has failed: {}", ex.getMessage());
        Throwable cause = ex.getCause();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Cannot delete book",
                        "message", "This book cannot be deleted: %s caused by %s".formatted(ex.getMessage(), cause == null ? "Unknown" : cause.getMessage()),
                        "bookId", ex.getBookId().toString()
                ));
    }

    /**
     * Handle method-security denials (e.g. a {@code @PreAuthorize} check that the caller's
     * authorities don't satisfy) as HTTP 403, same as an HTTP-layer authorization failure.
     * Without this, the catch-all handler below would turn it into a 500.
     */
    @ExceptionHandler(value = AccessDeniedException.class, produces = "application/json")
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Access denied", "message", ex.getMessage()));
    }

    /**
     * Handle all other exceptions as internal server errors. Exception message and stack trace are
     * only included outside production - returning them to the client is an information-disclosure
     * risk (internal package/file names, line numbers, sometimes query text).
     */
    @ExceptionHandler(value = Exception.class, produces = "application/json")
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(Exception ex) {
        log.error(ex.getMessage(), ex);
        if (isDevelopmentMode()) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", Objects.requireNonNullElse(ex.getMessage(), "Internal error"),
                            "stackTrace", ex.getStackTrace()));
        }
        return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred"));
    }

    private boolean isDevelopmentMode() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(DEVELOPMENT_PROFILES::contains);
    }
}
