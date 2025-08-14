package org.jetbrains.conf.bookify.members;

/**
 * Represents the status of a borrowing request.
 */
enum BorrowingStatus {
    /**
     * The borrowing request is pending approval.
     */
    PENDING,
    
    /**
     * The borrowing request has been approved.
     */
    APPROVED,
    
    /**
     * The borrowing request has been rejected.
     */
    REJECTED,

    RETURNED
}