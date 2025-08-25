package org.jetbrains.conf.bookify.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
class ErrorControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage(), "stackTrace", ex.getStackTrace()));
    }

}
