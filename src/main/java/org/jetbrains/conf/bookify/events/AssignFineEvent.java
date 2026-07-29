package org.jetbrains.conf.bookify.events;

import java.util.UUID;

/**
 * Event published when a fine needs to be assigned for an overdue book.
 */
public record AssignFineEvent(UUID bookId, UUID memberId, int overdueInDays) {
}
