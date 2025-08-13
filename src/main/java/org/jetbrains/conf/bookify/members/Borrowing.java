package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.books.Book;
import org.jetbrains.conf.bookify.employee.Employee;
import org.jetbrains.conf.bookify.employee.EmployeeId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a borrowing record of a book by a member.
 */
@Table("borrowing")
class Borrowing implements Persistable<UUID> {
    @Id
    private UUID id;
    @Column("book_id")
    private AggregateReference<Book,UUID> bookId;
    @Column("requested_book_id")
    private UUID requestedBookId;
    @Column("member_id")
    private AggregateReference<Member, UUID> memberId;
    @Column("borrow_date")
    private LocalDateTime borrowDate;
    @Column("return_date")
    private LocalDateTime returnDate;
    @Column("status")
    private BorrowingStatus status;
    private AggregateReference<Employee, EmployeeId> employeeId;

    @Transient
    private boolean isNew = true;

    Borrowing() {
        this.id = UUID.randomUUID();
        this.status = BorrowingStatus.PENDING;
    }

    @PersistenceCreator
    Borrowing(UUID id, UUID bookId, UUID requestedBookId,
              UUID memberId,
              LocalDateTime borrowDate, LocalDateTime returnDate,
              BorrowingStatus status,
              EmployeeId employeeId) {
        this.id = id;
        this.bookId = bookId == null ? null : AggregateReference.to(bookId);
        this.requestedBookId = requestedBookId;
        this.memberId = AggregateReference.to(memberId);
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
        this.employeeId = AggregateReference.to(employeeId);
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
        return bookId.getId();
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId == null ? null : AggregateReference.to(bookId);
    }

    public UUID getRequestedBookId() {
        return requestedBookId;
    }

    public void setRequestedBookId(UUID requestedBookId) {
        this.requestedBookId = requestedBookId;
    }

    public UUID getMemberId() {
        return memberId.getId();
    }

    public void setMemberId(UUID memberId) {
        this.memberId = AggregateReference.to(memberId);
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

    public EmployeeId getEmployee() {
        return employeeId.getId();
    }

    public void setEmployee(EmployeeId employeeId) {
        this.employeeId = AggregateReference.to(employeeId);
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
