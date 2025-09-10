package org.jetbrains.conf.bookify.books;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table
public class Book implements Persistable<UUID> {

    @Id
    @Nullable private UUID id;
    private String name;
    private String isbn;
    private boolean available;

    @Transient
    private boolean isNew = true;

    public Book() {
    }

    @PersistenceCreator
    Book(@Nullable UUID id, String name, String isbn, boolean available) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = name;
        this.isbn = isbn;
        this.available = available;
        this.isNew = id == null;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public @Nullable UUID getId() {
        return id;
    }

    public void setId(@Nullable UUID id) {
        this.id = id;
        this.isNew = id == null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
