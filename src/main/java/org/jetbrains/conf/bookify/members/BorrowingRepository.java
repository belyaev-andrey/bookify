package org.jetbrains.conf.bookify.members;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface BorrowingRepository extends ListCrudRepository<Borrowing, UUID> {

    @Override
    @EntityGraph(attributePaths = {"requestedBook", "book", "member"},
            type = EntityGraph.EntityGraphType.LOAD)
    Optional<Borrowing> findById(UUID id);

    /**
     * Find all borrowings for a specific member.
     * @param memberId the ID of the member
     * @return a list of borrowings for the member
     */
    List<Borrowing> findByMemberId(UUID memberId);

    /**
     * Find all active borrowings for a specific member: requests still in flight
     * ({@code PENDING}) plus books currently held ({@code APPROVED}).
     *
     * <p>{@code REJECTED} borrowings have to be excluded explicitly. They never receive a return
     * date, so filtering on {@code returnDate IS NULL} alone would keep counting them as active
     * for the rest of the member's life.
     *
     * @param memberId the ID of the member
     * @param excludedStatus the status that does not represent an active borrowing
     * @return a list of active borrowings for the member
     */
    List<Borrowing> findByMemberIdAndReturnDateIsNullAndStatusNot(UUID memberId, BorrowingStatus excludedStatus);

    /**
     * Check whether a member holds a book borrowed before the given moment, i.e. an overdue one.
     *
     * <p>Only {@code APPROVED} borrowings put a book in the member's hands: a {@code PENDING}
     * request has no borrow date yet, and a {@code REJECTED} one never gets one. Returning a book
     * moves its borrowing to {@code RETURNED}, so an approved borrowing is never a returned one.
     *
     * @param memberId the ID of the member
     * @param status the status of a borrowing whose book the member holds
     * @param borrowedBefore the moment before which a held book counts as overdue
     * @return true if the member holds such a book, false otherwise
     */
    boolean existsByMemberIdAndStatusAndBorrowDateBefore(UUID memberId, BorrowingStatus status,
                                                         LocalDateTime borrowedBefore);

    /**
     * Find the outstanding borrowing for a specific book, if any: one that was approved and has
     * not been returned yet. Only such a borrowing can be handed back.
     *
     * <p>The status is part of the query on purpose. {@code PENDING} and {@code REJECTED} rows
     * happen to be excluded already, because {@code book} is only ever set on the approved
     * branch of the availability check and so is null for both - but that is an invariant of one
     * call site, not something this query should quietly depend on.
     *
     * @param bookId the ID of the book
     * @param status the status a borrowing must have to be returnable
     * @return a list of outstanding borrowings for the book (should be at most one)
     */
    List<Borrowing> findByBookIdAndReturnDateIsNullAndStatus(UUID bookId, BorrowingStatus status);
}
