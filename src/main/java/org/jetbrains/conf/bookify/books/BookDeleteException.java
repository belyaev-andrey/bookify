package org.jetbrains.conf.bookify.books;

import java.util.UUID;

public class BookDeleteException extends RuntimeException {

    private final UUID bookId;

    BookDeleteException(UUID bookId, Throwable cause) {
        super("Cannot delete book %s: %s".formatted(bookId, cause.getMessage()), cause);
        this.bookId = bookId;
    }

    public UUID getBookId() {
        return bookId;
    }
}
