package org.jetbrains.conf.bookify.events;

import java.util.UUID;

/**
 * Event published when a book's availability is checked.
 */
public record BookAvailabilityCheckedEvent(UUID bookId, UUID borrowingId, boolean available) {
}