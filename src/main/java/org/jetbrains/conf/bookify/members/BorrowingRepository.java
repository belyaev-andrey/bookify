package org.jetbrains.conf.bookify.members;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

interface BorrowingRepository extends CrudRepository<Borrowing, UUID> {
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
     * Find the outstanding borrowing for a specific book, if any: one that was approved and has
     * not been returned yet. Only such a borrowing can be handed back.
     *
     * <p>The status is part of the query on purpose. {@code PENDING} and {@code REJECTED} rows
     * happen to be excluded already, because {@code bookId} is only ever set on the approved
     * branch of the availability check and so is null for both - but that is an invariant of one
     * call site, not something this query should quietly depend on.
     *
     * @param bookId the ID of the book
     * @param status the status a borrowing must have to be returnable
     * @return a list of outstanding borrowings for the book (should be at most one)
     */
    List<Borrowing> findByBookIdAndReturnDateIsNullAndStatus(UUID bookId, BorrowingStatus status);
}
