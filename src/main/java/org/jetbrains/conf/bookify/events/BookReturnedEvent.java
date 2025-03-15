package org.jetbrains.conf.bookify.events;

import java.util.UUID;

/**
 * Event published when a book is returned by a member.
 */
public record BookReturnedEvent(UUID bookId, UUID memberId) {
}