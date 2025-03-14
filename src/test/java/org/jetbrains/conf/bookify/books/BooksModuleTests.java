package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
@Import(DbConfiguration.class)
class BooksModuleTests {

    @Autowired
    private BookService bookService;

    @Test
    void verifyModuleStructure() {
        // This test verifies that the books module follows the Spring Modulith structure rules
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        modules.getModuleByName("books").orElseThrow();
    }

    @Test
    void shouldAddBook() {
        // This test verifies that the books module can add a book
        // It's a placeholder for a more comprehensive test
        Book book = new Book();
        book.setName("Test Book");
        book.setIsbn("1234567890");
        bookService.addBook(book);
    }
}
