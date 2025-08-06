package org.jetbrains.conf.bookify.books;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface BookRepository extends CrudRepository<Book, UUID> {

    List<Book> findByNameContainingIgnoreCase(String name);
}
