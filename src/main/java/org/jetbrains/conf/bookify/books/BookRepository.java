package org.jetbrains.conf.bookify.books;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface BookRepository extends CrudRepository<Book, UUID> {
    
    /**
     * Finds books whose name contains the given text, ignoring case.
     *
     * @param name partial name to search for; must not be {@code null}
     * @return a list of matching books; never {@code null}, possibly empty
     */
    List<Book> findByNameContainingIgnoreCase(String name);
}

@Configuration
class BookCallbackConfiguration {
    @Bean
    BeforeConvertCallback<Book> beforeConvertCallback() {
        return (book) -> {
            if (book.isNew() && book.getId() == null) {
                book.setId(UUID.randomUUID());
            }
            return book;
        };
    }
}