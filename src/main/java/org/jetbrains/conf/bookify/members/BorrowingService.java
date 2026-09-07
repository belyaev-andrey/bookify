package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.config.BookifySettingsConfig;
import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookBorrowRequestEvent;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final MemberService memberService;
    private final ApplicationEventPublisher eventPublisher;
    private final BookifySettingsConfig bookifySettingsConfig;

    /**
     * Create a borrowing request for a member.
     * @param bookId the ID of the book to borrow
     * @param memberId the ID of the member borrowing the book
     * @return the created borrowing request
     * @throws BorrowNotAllowedException if the member is not eligible to borrow
     */
    @Transactional
    Borrowing borrowBook(UUID bookId, UUID memberId) {
        findIneligibilityReason(memberId).ifPresent(reason -> {
            throw new BorrowNotAllowedException(reason, memberId);
        });
        Borrowing borrowing = new Borrowing(null, null, bookId, memberId, null, null, BorrowingStatus.PENDING);
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);
        eventPublisher.publishEvent(new BookBorrowRequestEvent(bookId, savedBorrowing.getId()));
        return savedBorrowing;
    }

    /**
     * Event listener for when a book's availability is checked.
     * @param event the book availability checked event
     */
    @ApplicationModuleListener
    void handleBookAvailabilityCheckedEvent(BookAvailabilityCheckedEvent event) {
        Optional<Borrowing> borrowingOpt = borrowingRepository.findById(event.borrowingId());
        if (borrowingOpt.isEmpty()) {
            return;
        }

        Borrowing borrowing = borrowingOpt.get();

        // Check if the borrowing is still pending
        if (borrowing.getStatus() != BorrowingStatus.PENDING) {
            return;
        }

        if (event.available()) {
            // Book is available, approve the request
            borrowing.setBorrowDate(LocalDateTime.now());
            borrowing.setStatus(BorrowingStatus.APPROVED);
            borrowing.setBookId(event.bookId());
        } else {
            // Book is not available, reject the request
            borrowing.setStatus(BorrowingStatus.REJECTED);
        }

        borrowingRepository.save(borrowing);
    }

    /**
     * Get a borrowing request by ID.
     * @param borrowingId the ID of the borrowing request
     * @return the borrowing request if found, empty otherwise
     */
    @Transactional(readOnly = true)
    Optional<Borrowing> getBorrowingById(UUID borrowingId) {
        return borrowingRepository.findById(borrowingId);
    }

    /**
     * Return a borrowed book.
     * @param bookId the ID of the book to return
     * @param memberId the ID of the member returning the book
     * @return the updated borrowing record if successful, empty otherwise
     */
    @Transactional
    Optional<Borrowing> returnBook(UUID bookId, UUID memberId) {
        // Step 1: A member requests to return a book (implicit in method call)

        // Step 2: The Members module validates the borrowing record. Only an approved, unreturned
        // borrowing can be handed back - a request that is still pending or was rejected never put
        // the book in the member's hands.
        List<Borrowing> outstandingBorrowings =
                borrowingRepository.findByBookIdAndReturnDateIsNullAndStatus(bookId, BorrowingStatus.APPROVED);
        Optional<Borrowing> borrowingOpt = outstandingBorrowings.stream()
                .filter(b -> b.getMemberId().equals(memberId))
                .findFirst();

        if (borrowingOpt.isEmpty()) {
            return Optional.empty();
        }

        // Step 3: The Members module updates the member's borrowing history
        Borrowing borrowing = borrowingOpt.get();
        borrowing.setReturnDate(LocalDateTime.now());
        borrowing.setStatus(BorrowingStatus.RETURNED);
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        // Step 4: The Books module updates the book's availability (via event)
        eventPublisher.publishEvent(new BookReturnedEvent(bookId, memberId));

        return Optional.of(savedBorrowing);
    }

    /**
     * Get all borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of borrowings for the member
     */
    @Transactional(readOnly = true)
    List<Borrowing> getBorrowingsForMember(UUID memberId) {
        return borrowingRepository.findByMemberId(memberId);
    }

    /**
     * Get all active (not returned) borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of active borrowings for the member
     */
    @Transactional(readOnly = true)
    List<Borrowing> getActiveBorrowingsForMember(UUID memberId) {
        return borrowingRepository.findByMemberIdAndReturnDateIsNullAndStatusNot(memberId, BorrowingStatus.REJECTED);
    }

    /**
     * Check if a member is eligible to borrow books.
     * @param memberId the ID of the member
     * @return true if the member is eligible, false otherwise
     */
    @Transactional(readOnly = true)
    boolean isMemberEligibleToBorrow(UUID memberId) {
        return findIneligibilityReason(memberId).isEmpty();
    }

    /**
     * Work out why a member may not borrow a book.
     * @param memberId the ID of the member
     * @return the rule that rejects the member, or empty if the member is eligible
     */
    @Transactional(readOnly = true)
    Optional<BorrowNotAllowedException.Reason> findIneligibilityReason(UUID memberId) {
        // Check if member exists and is active
        Optional<Member> memberOpt = memberService.findById(memberId);
        if (memberOpt.isEmpty()) {
            return Optional.of(BorrowNotAllowedException.Reason.MEMBER_NOT_FOUND);
        }
        if (!memberOpt.get().isEnabled()) {
            return Optional.of(BorrowNotAllowedException.Reason.MEMBER_DISABLED);
        }

        // Check if the member has too many active borrowings
        List<Borrowing> activeBorrowings = getActiveBorrowingsForMember(memberId);
        if (activeBorrowings.size() >= bookifySettingsConfig.getMaximumBooksBorrowed()) {
            return Optional.of(BorrowNotAllowedException.Reason.BORROW_LIMIT_REACHED);
        }

        // Check if the member has any overdue books. A book is overdue once it has been held for
        // more than the configured number of days; a PENDING request has no borrow date yet, so it
        // is still in flight rather than overdue.
        LocalDateTime overdueBefore = LocalDateTime.now().minusDays(bookifySettingsConfig.getOverdueDays());
        boolean hasOverdueBooks = activeBorrowings.stream()
                .map(Borrowing::getBorrowDate)
                .filter(Objects::nonNull)
                .anyMatch(borrowDate -> borrowDate.isBefore(overdueBefore));
        return hasOverdueBooks
                ? Optional.of(BorrowNotAllowedException.Reason.HAS_OVERDUE_BOOKS)
                : Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Borrowing> findAll() {
        List<Borrowing> all = new ArrayList<>();
        borrowingRepository.findAll().forEach(all::add);
        return all;
    }

    BorrowingService(BorrowingRepository borrowingRepository, MemberService memberService, ApplicationEventPublisher eventPublisher, BookifySettingsConfig bookifySettingsConfig) {
        this.borrowingRepository = borrowingRepository;
        this.memberService = memberService;
        this.eventPublisher = eventPublisher;
        this.bookifySettingsConfig = bookifySettingsConfig;
    }

}
