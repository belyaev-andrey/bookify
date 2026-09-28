package org.jetbrains.conf.bookify.books;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
@NullUnmarked
public class EntityManagerManualTransactionMgmntTest {

    @Autowired
    private BookRepository bookRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    public void testDetachedEntity() {
        // Work on a book of its own: the seed data is shared by every test class
        Book book = new Book();
        book.setName("Detached Book");
        book.setIsbn("1111111111");
        book.setAvailable(true);
        UUID bookId = bookRepository.save(book).getId();

        try {
            // Loaded outside any transaction, the book is detached; a change to it is only written once it is
            // merged back in a transaction
            Book detached = bookRepository.findById(bookId).orElseThrow();
            detached.setIsbn("2222222222");
            transactionTemplate.execute(status -> entityManager.merge(detached));

            assertThat(bookRepository.findById(bookId))
                    .get()
                    .extracting(Book::getIsbn)
                    .isEqualTo("2222222222");
        } finally {
            bookRepository.deleteById(bookId);
        }
    }

}
