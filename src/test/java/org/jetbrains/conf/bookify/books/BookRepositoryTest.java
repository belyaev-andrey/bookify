package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void bookWithoutIsbnIsRejectedByTheDatabase() {
        // The API rejects a missing ISBN before it gets this far; the NOT NULL constraint catches writes
        // that bypass it
        Book book = new Book(null, "Book Without ISBN", null, true);

        assertThatThrownBy(() -> bookRepository.save(book))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
