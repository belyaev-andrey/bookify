package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the stricter rule set by activating the "strict-security" profile through
 * {@code @ActiveProfiles} rather than through {@code spring.profiles.active} or a run configuration.
 * Only this test's context has the profile on, so {@link SecurityConfig#strictSecurityFilterChain}
 * is the single {@link SecurityFilterChain} candidate autowired here, while every other test (which
 * activates just "test") gets {@link SecurityConfig#securityFilterChain} instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "strict-security"})
@Import(DbConfiguration.class)
class StrictSecurityTest {

    private static final String LIBRARIAN_AUTH = "Basic " + Base64.getEncoder().encodeToString("testlibrarian:password".getBytes());
    private static final String ADMIN_AUTH = "Basic " + Base64.getEncoder().encodeToString("testadmin:password".getBytes());

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private ApplicationContext context;

    @Test
    void strictFilterChainIsTheOnlyOneRegistered() {
        assertThat(securityFilterChain).isSameAs(context.getBean("strictSecurityFilterChain"));
        assertThat(context.containsBean("securityFilterChain")).isFalse();
    }

    @Test
    void getBooks_withoutAuth_returnsUnauthorized() {
        // Anonymous under the default chain; the strict chain requires authentication for everything.
        var result = mockMvc.get().uri("/api/books");

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postBooks_withLibrarianRole_isForbidden() {
        var result = mockMvc.post()
                .uri("/api/books")
                .header("Authorization", LIBRARIAN_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Strict Security Book\",\"isbn\":\"8888888888\"}");

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void postBooks_withAdminRole_succeeds() {
        var result = mockMvc.post()
                .uri("/api/books")
                .header("Authorization", ADMIN_AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Strict Security Book\",\"isbn\":\"8888888889\"}");

        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void getMembersActive_withLibrarianRole_isForbidden() {
        var result = mockMvc.get()
                .uri("/api/members/active")
                .header("Authorization", LIBRARIAN_AUTH);

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void getMembersActive_withAdminRole_succeeds() {
        var result = mockMvc.get()
                .uri("/api/members/active")
                .header("Authorization", ADMIN_AUTH);

        assertThat(result).hasStatus(HttpStatus.OK);
    }
}
