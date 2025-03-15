package org.jetbrains.conf.bookify.members;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a borrowing record of a book by a member.
 */
@Table
class Borrowing implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID bookId;
    private UUID requestedBookId;
    private UUID memberId;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private BorrowingStatus status;

    @Transient
    private boolean isNew = true;

    Borrowing() {
        this.id = UUID.randomUUID();
        this.borrowDate = LocalDateTime.now();
        this.status = BorrowingStatus.PENDING;
    }

    @PersistenceCreator
    Borrowing(UUID id, UUID bookId, UUID requestedBookId, UUID memberId, LocalDateTime borrowDate, LocalDateTime returnDate, BorrowingStatus status) {
        this.id = id;
        this.bookId = bookId;
        this.requestedBookId = requestedBookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public UUID getRequestedBookId() {
        return requestedBookId;
    }

    public void setRequestedBookId(UUID requestedBookId) {
        this.requestedBookId = requestedBookId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * Checks if the book has been returned.
     * @return true if the book has been returned, false otherwise
     */
    public boolean isReturned() {
        return returnDate != null;
    }

    /**
     * Gets the status of the borrowing request.
     * @return the status of the borrowing request
     */
    public BorrowingStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the borrowing request.
     * @param status the status to set
     */
    public void setStatus(BorrowingStatus status) {
        this.status = status;
    }
}
