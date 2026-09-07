package org.jetbrains.conf.bookify.members;

import java.util.UUID;

/**
 * Thrown when a member is not allowed to borrow a book. The {@link Reason} records which
 * eligibility rule rejected the request, so the API can answer with a status code and a message
 * the caller can act on instead of a bare 404 that is indistinguishable from a mistyped URL.
 */
class BorrowNotAllowedException extends RuntimeException {

    /**
     * The eligibility rule that rejected a borrow request.
     */
    enum Reason {
        /** No member exists with the given ID. */
        MEMBER_NOT_FOUND("No member exists with ID %s"),
        /** The member exists, but their account is disabled. */
        MEMBER_DISABLED("Member %s is disabled"),
        /** The member already holds the maximum number of books allowed at once. */
        BORROW_LIMIT_REACHED("Member %s has reached the maximum number of borrowed books"),
        /** The member is still holding at least one book past its due date. */
        HAS_OVERDUE_BOOKS("Member %s has overdue books");

        private final String messageTemplate;

        Reason(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }

        String describe(UUID memberId) {
            return messageTemplate.formatted(memberId);
        }
    }

    private final Reason reason;
    private final UUID memberId;

    BorrowNotAllowedException(Reason reason, UUID memberId) {
        super(reason.describe(memberId));
        this.reason = reason;
        this.memberId = memberId;
    }

    public Reason getReason() {
        return reason;
    }

    public UUID getMemberId() {
        return memberId;
    }
}
