package org.jetbrains.conf.bookify.members;

import jakarta.persistence.*;
import org.jetbrains.conf.bookify.books.Book;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a borrowing record of a book by a member.
 */
@Entity
@Table(name = "borrowing")
@NullUnmarked
class Borrowing implements Persistable<@NonNull UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "requested_book_id")
    private Book requestedBook;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status = BorrowingStatus.PENDING;

    public Borrowing() {
    }

    @PersistenceCreator
    Borrowing(@Nullable UUID id, @Nullable Book book, @Nullable Book requestedBook, Member member, @Nullable LocalDateTime borrowDate, @Nullable LocalDateTime returnDate, BorrowingStatus status) {
        this.id = id;
        this.book = book;
        this.requestedBook = requestedBook;
        this.member = member;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Book getRequestedBook() {
        return requestedBook;
    }

    public void setRequestedBook(Book requestedBook) {
        this.requestedBook = requestedBook;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
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
