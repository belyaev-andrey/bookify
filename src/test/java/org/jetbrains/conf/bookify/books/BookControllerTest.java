package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbConfiguration.class)
class BookControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Test
    void testFetchAll() throws Exception {
        var booksRequestResult = mockMvc.get().uri("/api/books");
        assertThat(booksRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testFetchExistingById() throws Exception {
        var booksRequestResult = mockMvc.get().uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14");
        assertThat(booksRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }

    @Test
    void testFetchNonExistingById() throws Exception {
        var booksRequestResult = mockMvc.get().uri("/api/books/00000000-0000-0000-0000-000000000000");
        assertThat(booksRequestResult)
                .hasStatus(HttpStatus.NOT_FOUND);
    }

     @Test
     void testAddBook() throws Exception {
         // Add a book
         var addBookResult = mockMvc.post()
                 .uri("/api/books")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("{\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

         assertThat(addBookResult)
                 .hasStatus(HttpStatus.CREATED);
     }

    @Test
    void testUpdateExistingBook() throws Exception {
        // Update a book
        var updateBookResult = mockMvc.put()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11\",\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

        assertThat(updateBookResult)
                .hasStatus(HttpStatus.OK);

        var booksRequestResult = mockMvc.get().uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        assertThat(booksRequestResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson().convertTo(Book.class).extracting("name")
                .isEqualTo("Test Book");
    }


    @Test
    void testUpdateNonExistingBook() throws Exception {
        // Update a book
        var updateBookResult = mockMvc.put()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"Test Book\",\"isbn\":\"1234567890\"}");

        assertThat(updateBookResult)
                .hasStatus(HttpStatus.NOT_FOUND);
    }


    @Test
    void testRemoveBook() throws Exception {
        var removeBookResult = mockMvc.delete().uri("/api/books/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        assertThat(removeBookResult).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void testSearchBooksByName() throws Exception {
        // Search for books with "Lord" in the name (from initial data)
        var searchResult = mockMvc.get().uri("/api/books/search?name=Lord");
        assertThat(searchResult)
                .hasStatus(HttpStatus.OK)
                .bodyJson();
    }
}
