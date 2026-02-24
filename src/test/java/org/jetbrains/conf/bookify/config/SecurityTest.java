package org.jetbrains.conf.bookify.config;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Nested;
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

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class SecurityTest {

    private static final String LIBRARIAN_AUTH = "Basic " + Base64.getEncoder().encodeToString("testlibrarian:password".getBytes());
    private static final String INVALID_AUTH = "Basic " + Base64.getEncoder().encodeToString("invalid:wrongpassword".getBytes());

    @Autowired
    private MockMvcTester mockMvc;

    @Nested
    class UnauthorizedAccess {

        @Test
        void postBooks_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.post()
                    .uri("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void putBooks_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.put()
                    .uri("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":\"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void deleteBooks_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.delete().uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void postMembers_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.post()
                    .uri("/api/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test Member\",\"email\":\"test@example.com\",\"password\":\"password123\"}");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void putMembersDisable_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.put().uri("/api/members/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/disable");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void getMembersActive_withoutAuth_returnsUnauthorized() {
            var result = mockMvc.get().uri("/api/members/active");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void postBooks_withInvalidCredentials_returnsUnauthorized() {
            var result = mockMvc.post()
                    .uri("/api/books")
                    .header("Authorization", INVALID_AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class AnonymousAccessAllowed {

        @Test
        void getBooks_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/books");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void getBookById_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void searchBooks_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/books/search?name=Lord");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void getMembers_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/members");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void getMemberById_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/members/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void searchMembers_withoutAuth_returnsOk() {
            var result = mockMvc.get().uri("/api/members/search?name=John");

            assertThat(result).hasStatus(HttpStatus.OK);
        }
    }

    @Nested
    class LibrarianAccess {

        @Test
        void postBooks_withLibrarianRole_succeeds() {
            var result = mockMvc.post()
                    .uri("/api/books")
                    .header("Authorization", LIBRARIAN_AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Security Test Book\",\"isbn\":\"9999999999\"}");

            assertThat(result).hasStatus(HttpStatus.CREATED);
        }

        @Test
        void putBooks_withLibrarianRole_succeeds() {
            var result = mockMvc.put()
                    .uri("/api/books")
                    .header("Authorization", LIBRARIAN_AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":\"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\"name\":\"Updated Book\",\"isbn\":\"9780618640157\"}");

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void deleteBooks_withLibrarianRole_attemptSucceeds() {
            // Note: Returns CONFLICT if book has borrowings, but authorization passes
            var result = mockMvc.delete()
                    .uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
                    .header("Authorization", LIBRARIAN_AUTH);

            // Authorization succeeds - expect CONFLICT (409) due to borrowing constraints
            assertThat(result).hasStatus(HttpStatus.CONFLICT);
        }

        @Test
        void postMembers_withLibrarianRole_succeeds() {
            var result = mockMvc.post()
                    .uri("/api/members")
                    .header("Authorization", LIBRARIAN_AUTH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Security Test Member\",\"email\":\"security@test.com\",\"password\":\"password123\"}");

            assertThat(result).hasStatus(HttpStatus.CREATED);
        }

        @Test
        void putMembersDisable_withLibrarianRole_succeeds() {
            var result = mockMvc.put()
                    .uri("/api/members/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12/disable")
                    .header("Authorization", LIBRARIAN_AUTH);

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void getMembersActive_withLibrarianRole_succeeds() {
            var result = mockMvc.get()
                    .uri("/api/members/active")
                    .header("Authorization", LIBRARIAN_AUTH);

            assertThat(result).hasStatus(HttpStatus.OK);
        }
    }
}
