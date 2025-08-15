package org.jetbrains.conf.bookify.books;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(DbConfiguration.class)
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
        Book next = bookRepository.findAll().iterator().next();
        next.setIsbn("sd");
        transactionTemplate.execute(status -> {
            Book merged = entityManager.merge(next);
            System.out.println(merged.getIsbn());
            return merged.getId();
        });
    }

}
