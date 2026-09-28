package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class BooksModuleTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void verifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        var module = modules.getModuleByName("books").orElseThrow();
        assertNotNull(module);
    }

    @Test
    void shouldAddBook() {
        // The books module can add a book, and the book is stored
        Book book = new Book();
        book.setName("Test Book");
        book.setIsbn("1234567890");
        book.setAvailable(true);
        Book saved = bookService.saveBook(book);
        UUID bookId = Objects.requireNonNull(saved.getId());

        try {
            // Read through the repository: findById would answer from the cache that saveBook filled
            assertThat(bookRepository.findById(bookId))
                    .get()
                    .extracting(Book::getName, Book::getIsbn, Book::isAvailable)
                    .containsExactly("Test Book", "1234567890", true);
        } finally {
            bookRepository.deleteById(bookId);
        }
    }
}
