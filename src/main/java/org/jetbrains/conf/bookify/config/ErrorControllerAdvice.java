package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.books.BookDeleteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
class ErrorControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAdvice.class);

    /**
     * Handle BookHasBorrowingsException - when attempting to delete a book with borrowing records.
     * Returns HTTP 409 CONFLICT with a meaningful error message.
     */
    @ExceptionHandler(value = BookDeleteException.class, produces = "application/json")
    public ResponseEntity<Map<String, String>> handleBookHasBorrowingsException(BookDeleteException ex) {
        log.warn("Attempt to delete has failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Cannot delete book",
                        "message", "This book cannot be deleted: %s caused by %s".formatted(ex.getMessage(), ex.getCause().getMessage()),
                        "bookId", ex.getBookId().toString()
                ));
    }

    /**
     * Handle all other exceptions as internal server errors.
     */
    @ExceptionHandler(value = Exception.class, produces = "application/json")
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage(), "stackTrace", ex.getStackTrace()));
    }

}
