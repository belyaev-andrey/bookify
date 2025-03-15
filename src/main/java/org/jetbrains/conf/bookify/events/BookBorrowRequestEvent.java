package org.jetbrains.conf.bookify.events;

import java.util.UUID;

/**
 * Event published when a book is borrowed by a member.
 */
public record BookBorrowRequestEvent(UUID bookId, UUID borrowId) {
}