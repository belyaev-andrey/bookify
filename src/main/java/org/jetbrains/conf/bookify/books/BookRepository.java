package org.jetbrains.conf.bookify.books;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface BookRepository extends CrudRepository<Book, UUID> {

    List<Book> findByNameContainingIgnoreCase(String name);
}
