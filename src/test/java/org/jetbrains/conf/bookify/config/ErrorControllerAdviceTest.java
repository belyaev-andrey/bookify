package org.jetbrains.conf.bookify.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorControllerAdviceTest {

    @Test
    void handleValidationExceptions_hidesInternalDetailsOutsideDevelopmentProfiles() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        ErrorControllerAdvice advice = new ErrorControllerAdvice(environment);

        ResponseEntity<Map<String, Object>> response =
                advice.handleValidationExceptions(new RuntimeException("sensitive internal detail"));

        assertThat(response.getBody())
                .containsExactly(Map.entry("error", "An unexpected error occurred"))
                .doesNotContainKey("stackTrace");
    }

    @Test
    void handleValidationExceptions_hidesInternalDetailsWhenNoProfileIsActive() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        ErrorControllerAdvice advice = new ErrorControllerAdvice(environment);

        ResponseEntity<Map<String, Object>> response =
                advice.handleValidationExceptions(new RuntimeException("sensitive internal detail"));

        assertThat(response.getBody()).doesNotContainKey("stackTrace");
    }

    @Test
    void handleValidationExceptions_includesDetailsInDevelopmentProfiles() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
        ErrorControllerAdvice advice = new ErrorControllerAdvice(environment);

        ResponseEntity<Map<String, Object>> response =
                advice.handleValidationExceptions(new RuntimeException("boom"));

        assertThat(response.getBody())
                .containsEntry("error", "boom")
                .containsKey("stackTrace");
    }

    @Test
    void handleValidationExceptions_fallsBackToGenericMessageWhenExceptionHasNoMessage() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {"test"});
        ErrorControllerAdvice advice = new ErrorControllerAdvice(environment);

        ResponseEntity<Map<String, Object>> response = advice.handleValidationExceptions(new RuntimeException());

        assertThat(response.getBody()).containsEntry("error", "Internal error");
    }
}
