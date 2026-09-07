package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ErrorControllerAdvice} has a catch-all {@code Exception} handler, which used to swallow
 * Spring MVC's own exceptions and report every one of them as a 500 - blaming the server for
 * mistakes the caller made, and burying genuine failures in the error log. These check that the
 * common client errors keep the status they carry.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class ClientErrorStatusTest {

    private static final String LIBRARIAN_AUTH =
            "Basic " + Base64.getEncoder().encodeToString("testlibrarian:password".getBytes());

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void unmappedUrl_returnsNotFound() {
        assertThat(mockMvc.get().uri("/api/no-such-endpoint"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void unsupportedMethod_returnsMethodNotAllowed() {
        // /api/borrowings is mapped for GET only
        assertThat(mockMvc.delete().uri("/api/borrowings"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void malformedRequestBody_returnsBadRequestWithoutLeakingParserDetail() {
        assertThat(mockMvc.post()
                .uri("/api/books")
                .header("Authorization", LIBRARIAN_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.error")
                .isEqualTo("Malformed request body");
    }
}
