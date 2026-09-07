package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.books.BookDeleteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
     * Handle a query parameter, path variable or header whose value Spring could not convert to
     * the declared type - {@code ?bookId=not-a-uuid}, say. Unlike Spring's other request-binding
     * failures this one is a {@link org.springframework.beans.TypeMismatchException} rather than
     * an {@link ErrorResponse}, so the catch-all below would otherwise report it as a 500.
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class, produces = "application/json")
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Rejecting request with 400 Bad Request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid value for parameter '%s'".formatted(ex.getName())));
    }

    /**
     * Handle a request body that could not be parsed - malformed JSON, say. Like the type mismatch
     * above, this is not an {@link ErrorResponse}, so the catch-all below would report it as a 500.
     * The message stays generic on purpose: parser errors quote internal type names and fragments
     * of the body back at the caller.
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class, produces = "application/json")
    public ResponseEntity<Map<String, Object>> handleUnreadableRequestBody(HttpMessageNotReadableException ex) {
        log.warn("Rejecting request with 400 Bad Request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "Malformed request body"));
    }

    /**
     * Handle all other exceptions as internal server errors. Exception message and stack trace are
     * only included outside production - returning them to the client is an information-disclosure
     * risk (internal package/file names, line numbers, sometimes query text).
     */
    @ExceptionHandler(value = Exception.class, produces = "application/json")
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(Exception ex) {
        // Spring's own MVC exceptions - a missing request parameter, an unknown URL, an unsupported
        // method or media type - already carry the status they should be answered with. Letting
        // them fall through blames the server for what the caller got wrong, and buries genuine
        // failures among 500s in the error log.
        if (ex instanceof ErrorResponse errorResponse && errorResponse.getStatusCode().is4xxClientError()) {
            log.warn("Rejecting request with {}: {}", errorResponse.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(Map.of("error", Objects.requireNonNullElse(ex.getMessage(), "Bad request")));
        }
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
