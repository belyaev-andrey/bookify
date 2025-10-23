package org.jetbrains.conf.bookify.books;

import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.UUID;

interface BookRepository extends ListCrudRepository<Book, UUID> {

    List<Book> findByNameContainingIgnoreCase(String name);
}
