package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.books.BookService;
import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final ApplicationEventPublisher eventPublisher;

    BorrowingService(BorrowingRepository borrowingRepository, MemberService memberService, BookService bookService, ApplicationEventPublisher eventPublisher) {
        this.borrowingRepository = borrowingRepository;
        this.memberService = memberService;
        this.bookService = bookService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a borrowing request for a member.
     * @param bookId the ID of the book to borrow
     * @param memberId the ID of the member borrowing the book
     * @return the borrowing request if successful, empty otherwise
     */
    @Transactional
    public Optional<Borrowing> borrowBook(UUID bookId, UUID memberId) {
        // Step 1: A member requests to borrow a book (implicit in method call)

        // Step 2: The Members module validates the member's eligibility
        if (!isMemberEligibleToBorrow(memberId)) {
            return Optional.empty();
        }

        // Step 3: Create a pending borrowing request
        Borrowing borrowing = new Borrowing();
        borrowing.setMemberId(memberId);

        // Check if the book exists
        boolean bookExists = bookService.findById(bookId).isPresent();
        if (bookExists) {
            // If the book exists, set the bookId field
            borrowing.setBookId(bookId);
        } else {
            // If the book doesn't exist, set the requestedBookId field
            borrowing.setRequestedBookId(bookId);
        }

        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setStatus(BorrowingStatus.PENDING);
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        // Step 4: Send a message to the book service to check availability
        // This will always return false for non-existing books
        bookService.checkBookAvailabilityAndUpdate(bookId, savedBorrowing.getId());

        return Optional.of(savedBorrowing);
    }

    /**
     * Event listener for when a book's availability is checked.
     * @param event the book availability checked event
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookAvailabilityCheckedEvent(BookAvailabilityCheckedEvent event) {
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
            borrowing.setStatus(BorrowingStatus.APPROVED);

            // No need to publish BookBorrowedEvent here as the book is already marked as borrowed in BookService
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
    public Optional<Borrowing> getBorrowingById(UUID borrowingId) {
        return borrowingRepository.findById(borrowingId);
    }

    /**
     * Return a borrowed book.
     * @param bookId the ID of the book to return
     * @param memberId the ID of the member returning the book
     * @return the updated borrowing record if successful, empty otherwise
     */
    @Transactional
    public Optional<Borrowing> returnBook(UUID bookId, UUID memberId) {
        // Step 1: A member requests to return a book (implicit in method call)

        // Step 2: The Members module validates the borrowing record
        List<Borrowing> activeBorrowings = borrowingRepository.findByBookIdAndReturnDateIsNull(bookId);
        Optional<Borrowing> borrowingOpt = activeBorrowings.stream()
                .filter(b -> b.getMemberId().equals(memberId))
                .findFirst();

        if (borrowingOpt.isEmpty()) {
            return Optional.empty();
        }

        // Step 3: The Members module updates the member's borrowing history
        Borrowing borrowing = borrowingOpt.get();
        borrowing.setReturnDate(LocalDateTime.now());
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
    public List<Borrowing> getBorrowingsForMember(UUID memberId) {
        return borrowingRepository.findByMemberId(memberId);
    }

    /**
     * Get all active (not returned) borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of active borrowings for the member
     */
    @Transactional(readOnly = true)
    public List<Borrowing> getActiveBorrowingsForMember(UUID memberId) {
        return borrowingRepository.findByMemberIdAndReturnDateIsNull(memberId);
    }

    /**
     * Check if a member is eligible to borrow books.
     * @param memberId the ID of the member
     * @return true if the member is eligible, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isMemberEligibleToBorrow(UUID memberId) {
        // Check if member exists and is active
        Optional<Member> memberOpt = memberService.findById(memberId);
        if (memberOpt.isEmpty() || !memberOpt.get().isEnabled()) {
            return false;
        }

        // Check if the member has too many active borrowings (limit to 5)
        List<Borrowing> activeBorrowings = getActiveBorrowingsForMember(memberId);
        if (activeBorrowings.size() >= 5) {
            return false;
        }

        // Check if the member has any overdue books
        // A book is considered overdue if it has been borrowed for more than 14 days
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);
        boolean hasOverdueBooks = activeBorrowings.stream()
                .anyMatch(b -> b.getBorrowDate().isBefore(twoWeeksAgo));
        if (hasOverdueBooks) {
            return false;
        }

        return true;
    }
}
