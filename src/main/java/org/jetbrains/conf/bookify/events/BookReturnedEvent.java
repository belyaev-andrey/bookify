package org.jetbrains.conf.bookify.events;

import java.util.UUID;

/**
 * Event published when a book is returned by a member.
 */
public class BookReturnedEvent {
    private final UUID bookId;
    private final UUID memberId;

    public BookReturnedEvent(UUID bookId, UUID memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getMemberId() {
        return memberId;
    }
}