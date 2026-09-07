package org.jetbrains.conf.bookify.members;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Translates borrowing failures into HTTP responses.
 *
 * <p>This lives in {@code members} rather than alongside the global advice in {@code config},
 * because {@code members} already depends on {@code config} for its settings and handling
 * {@link BorrowNotAllowedException} there would close that into a module cycle.
 *
 * <p>The order matters: the global advice in {@code config} has a catch-all {@code Exception}
 * handler, and Spring picks the first advice bean (in order) that can handle the exception. An
 * unordered {@code @ControllerAdvice} sits at {@link Ordered#LOWEST_PRECEDENCE}, so without an
 * explicit order the catch-all could turn these into 500s.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class BorrowingErrorControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(BorrowingErrorControllerAdvice.class);

    /**
     * Handle a rejected borrow request. A missing member is a genuine 404; every other reason
     * means the request was understood but conflicts with the member's current state, so it maps
     * to 409 rather than a bare "not found" the caller cannot tell apart from a mistyped URL.
     * Either way the body names the rule that rejected the request.
     */
    @ExceptionHandler(value = BorrowNotAllowedException.class, produces = "application/json")
    public ResponseEntity<Map<String, String>> handleBorrowNotAllowedException(BorrowNotAllowedException ex) {
        log.warn("Borrow request rejected: {}", ex.getMessage());
        HttpStatus status = (ex.getReason() == BorrowNotAllowedException.Reason.MEMBER_NOT_FOUND)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(Map.of(
                        "error", "Borrowing not allowed",
                        "reason", ex.getReason().name(),
                        "message", ex.getMessage(),
                        "memberId", ex.getMemberId().toString()
                ));
    }
}
