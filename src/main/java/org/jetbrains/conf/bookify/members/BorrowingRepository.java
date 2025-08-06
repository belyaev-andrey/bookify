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
     * Find all active (not returned) borrowings for a specific member.
     * @param memberId the ID of the member
     * @return a list of active borrowings for the member
     */
    List<Borrowing> findByMemberIdAndReturnDateIsNull(UUID memberId);

    /**
     * Find the active (not returned) borrowing for a specific book, if any.
     * @param bookId the ID of the book
     * @return a list of active borrowings for the book (should be at most one)
     */
    List<Borrowing> findByBookIdAndReturnDateIsNull(UUID bookId);
}
