package org.jetbrains.conf.bookify.books;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "book")
class Book implements Persistable<UUID> {

    @Id
    private UUID id;
    private String name;
    private String isbn;
    private boolean available;

    @Transient
    private boolean isNew = true;

    protected Book() {
        this.id = UUID.randomUUID();
        this.available = true;
    }

    @PersistenceCreator
    Book(UUID id, String name, String isbn, boolean available) {
        this.id = id;
        this.name = name;
        this.isbn = isbn;
        this.available = available;
        this.isNew = false;
    }

    // Constructor for backward compatibility
    Book(UUID id, String name, String isbn) {
        this(id, name, isbn, true);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
