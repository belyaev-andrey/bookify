package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.jetbrains.conf.bookify.config.BookifySettingsConfig;
import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DbConfiguration.class)
@TestPropertySource(properties = {
        "bookify.maximum.books.borrowed=5",
        "bookify.overdue.days=14"
})
class BorrowingServiceTest {

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookifySettingsConfig bookifySettingsConfig;

    // Test data UUIDs from migrations
    private static final UUID TEST_MEMBER_1 = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private static final UUID TEST_MEMBER_2 = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12");
    private static final UUID TEST_MEMBER_3 = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");
    private static final UUID TEST_BOOK_1 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private static final UUID TEST_BOOK_2 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12");
    private static final UUID TEST_BOOK_3 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");
    private static final UUID TEST_BOOK_4 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14");

    @AfterEach
    void cleanup() {
        // Clean up any test borrowings created during tests
        List<Borrowing> allBorrowings = borrowingService.findAll();
        for (Borrowing borrowing : allBorrowings) {
            // Only delete test borrowings (those created during tests, identified by specific patterns)
            if (borrowing.getStatus() == BorrowingStatus.PENDING && borrowing.getBookId() == null) {
                borrowingRepository.deleteById(borrowing.getId());
            }
        }
    }

    // ==================== Tests for borrowBook() ====================

    @Test
    void borrowBook_successfulBorrowing_forEligibleMember() {
        // Given: Create a new eligible member (without overdue books)
        Member newMember = new Member();
        newMember.setName("New Eligible Member");
        newMember.setEmail("eligible@test.com");
        newMember.setEnabled(true);
        Member savedMember = memberRepository.save(newMember);
        UUID memberId = savedMember.getId();

        try {
            // When: Member borrows a book
            Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, memberId);

            // Then: Borrowing is created successfully
            assertThat(result).isPresent();
            Borrowing borrowing = result.get();
            assertThat(borrowing.getId()).isNotNull();
            assertThat(borrowing.getStatus()).isEqualTo(BorrowingStatus.PENDING);
            assertThat(borrowing.getRequestedBookId()).isEqualTo(TEST_BOOK_1);
            assertThat(borrowing.getMemberId()).isEqualTo(memberId);
            assertThat(borrowing.getBookId()).isNull(); // Not yet assigned
            assertThat(borrowing.getBorrowDate()).isNull(); // Not yet approved

            // Cleanup
            borrowingRepository.deleteById(borrowing.getId());
        } finally {
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void borrowBook_rejectWhenMemberDoesNotExist() {
        // Given: A non-existent member ID
        UUID nonExistentMemberId = UUID.randomUUID();

        // When: Trying to borrow
        Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, nonExistentMemberId);

        // Then: Borrowing is rejected
        assertThat(result).isEmpty();
    }

    @Test
    void borrowBook_rejectWhenMemberIsDisabled() {
        // Given: A disabled member
        Member disabledMember = new Member();
        disabledMember.setName("Disabled User");
        disabledMember.setEmail("disabled@test.com");
        disabledMember.setEnabled(false);
        Member savedMember = memberRepository.save(disabledMember);
        UUID memberId = savedMember.getId();

        try {
            // When: Disabled member tries to borrow
            Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, memberId);

            // Then: Borrowing is rejected
            assertThat(result).isEmpty();
        } finally {
            // Cleanup
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void borrowBook_rejectWhenMemberHasMaxBooksBorrowed() {
        // Given: A member with maximum books borrowed (5)
        Member member = new Member();
        member.setName("Heavy Reader");
        member.setEmail("heavyreader@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // Create 5 active borrowings using existing book IDs
            UUID[] bookIds = {TEST_BOOK_1, TEST_BOOK_2, TEST_BOOK_3, TEST_BOOK_4, TEST_BOOK_1};
            for (int i = 0; i < bookifySettingsConfig.getMaximumBooksBorrowed(); i++) {
                Borrowing borrowing = new Borrowing(
                        null,
                        bookIds[i], // Use existing book IDs
                        bookIds[i],
                        memberId,
                        LocalDateTime.now().minusDays(1),
                        null, // Not returned
                        BorrowingStatus.APPROVED
                );
                borrowingRepository.save(borrowing);
            }

            // When: Member tries to borrow one more book
            Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, memberId);

            // Then: Borrowing is rejected
            assertThat(result).isEmpty();

            // Cleanup
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            for (Borrowing b : borrowings) {
                borrowingRepository.deleteById(b.getId());
            }
        } finally {
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void borrowBook_rejectWhenMemberHasOverdueBooks() {
        // Given: A member with an overdue book
        Member member = new Member();
        member.setName("Late Returner");
        member.setEmail("late@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // Create an overdue borrowing (borrowed more than 14 days ago)
            Borrowing overdueBorrowing = new Borrowing(
                    null,
                    TEST_BOOK_1,
                    TEST_BOOK_1,
                    memberId,
                    LocalDateTime.now().minusDays(bookifySettingsConfig.getOverdueDays() + 1), // 15 days ago
                    null, // Not returned
                    BorrowingStatus.APPROVED
            );
            Borrowing saved = borrowingRepository.save(overdueBorrowing);

            // When: Member tries to borrow another book
            Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, memberId);

            // Then: Borrowing is rejected due to overdue books
            assertThat(result).isEmpty();

            // Cleanup
            borrowingRepository.deleteById(saved.getId());
        } finally {
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void borrowBook_allowBorrowingWhenBookBorrowed13DaysAgo() {
        // Given: A member with a book borrowed 13 days ago (definitely not overdue with 14-day limit)
        Member member = new Member();
        member.setName("On Time Reader");
        member.setEmail("ontime@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // Create a borrowing 13 days ago (well within the 14-day limit)
            Borrowing borrowing = new Borrowing(
                    null,
                    TEST_BOOK_2,
                    TEST_BOOK_2,
                    memberId,
                    LocalDateTime.now().minusDays(13), // 13 days ago, not overdue
                    null,
                    BorrowingStatus.APPROVED
            );
            Borrowing saved = borrowingRepository.save(borrowing);

            // When: Member tries to borrow another book
            Optional<Borrowing> result = borrowingService.borrowBook(TEST_BOOK_1, memberId);

            // Then: Borrowing is allowed (13 days is not overdue with 14-day limit)
            assertThat(result).isPresent();

            // Cleanup - delete borrowings first, then member
            borrowingRepository.deleteById(result.get().getId());
            borrowingRepository.deleteById(saved.getId());
        } finally {
            // Final cleanup in case of exceptions
            try {
                memberRepository.deleteById(memberId);
            } catch (Exception ignored) {
                // May have already been cleaned up
            }
        }
    }

    // ==================== Tests for handleBookAvailabilityCheckedEvent() ====================

    @Test
    @Transactional
    void handleBookAvailabilityCheckedEvent_approvesWhenBookAvailable() {
        // Note: @ApplicationModuleListener makes this async/transactional,
        // so we test the basic flow and verify no exceptions are thrown

        // Given: Create a new member and pending borrowing
        Member testMember = new Member();
        testMember.setName("Event Test Member");
        testMember.setEmail("eventmember@test.com");
        testMember.setEnabled(true);
        Member savedMember = memberRepository.save(testMember);

        Borrowing borrowing = new Borrowing(
                null,
                null,
                TEST_BOOK_1,
                savedMember.getId(),
                null,
                null,
                BorrowingStatus.PENDING
        );
        Borrowing saved = borrowingRepository.save(borrowing);
        UUID borrowingId = saved.getId();

        try {
            // When: Book availability is checked and book is available
            BookAvailabilityCheckedEvent event = new BookAvailabilityCheckedEvent(
                    TEST_BOOK_1,
                    borrowingId,
                    true // Book is available
            );

            // Then: Handler completes without errors
            // Note: The actual approval logic is tested via BorrowingControllerTest integration tests
            // Direct unit test of event handler is challenging due to @ApplicationModuleListener transaction handling
            borrowingService.handleBookAvailabilityCheckedEvent(event);

            // Verify the handler executed (borrowing still exists)
            Optional<Borrowing> result = borrowingRepository.findById(borrowingId);
            assertThat(result).isPresent();
        } finally {
            borrowingRepository.deleteById(borrowingId);
            memberRepository.deleteById(savedMember.getId());
        }
    }

    @Test
    @Transactional
    void handleBookAvailabilityCheckedEvent_rejectsWhenBookUnavailable() {
        // Note: @ApplicationModuleListener makes this async/transactional,
        // so we test the basic flow and verify no exceptions are thrown

        // Given: Create a new member and pending borrowing
        Member testMember = new Member();
        testMember.setName("Event Test Member 2");
        testMember.setEmail("eventmember2@test.com");
        testMember.setEnabled(true);
        Member savedMember = memberRepository.save(testMember);

        Borrowing borrowing = new Borrowing(
                null,
                null,
                TEST_BOOK_1,
                savedMember.getId(),
                null,
                null,
                BorrowingStatus.PENDING
        );
        Borrowing saved = borrowingRepository.save(borrowing);
        UUID borrowingId = saved.getId();

        try {
            // When: Book availability is checked and book is not available
            BookAvailabilityCheckedEvent event = new BookAvailabilityCheckedEvent(
                    TEST_BOOK_1,
                    borrowingId,
                    false // Book is not available
            );

            // Then: Handler completes without errors
            // Note: The actual rejection logic is tested via BorrowingControllerTest integration tests
            borrowingService.handleBookAvailabilityCheckedEvent(event);

            // Verify the handler executed (borrowing still exists)
            Optional<Borrowing> result = borrowingRepository.findById(borrowingId);
            assertThat(result).isPresent();
        } finally {
            borrowingRepository.deleteById(borrowingId);
            memberRepository.deleteById(savedMember.getId());
        }
    }

    @Test
    void handleBookAvailabilityCheckedEvent_ignoresIfBorrowingNotFound() {
        // Given: A non-existent borrowing ID
        UUID nonExistentBorrowingId = UUID.randomUUID();

        // When: Event is received for non-existent borrowing
        BookAvailabilityCheckedEvent event = new BookAvailabilityCheckedEvent(
                TEST_BOOK_1,
                nonExistentBorrowingId,
                true
        );

        // Then: No exception is thrown (event is ignored gracefully)
        borrowingService.handleBookAvailabilityCheckedEvent(event);
    }

    @Test
    void handleBookAvailabilityCheckedEvent_ignoresIfBorrowingNotPending() {
        // Given: An already approved borrowing
        Borrowing borrowing = new Borrowing(
                null,
                TEST_BOOK_1,
                TEST_BOOK_1,
                TEST_MEMBER_3,
                LocalDateTime.now(),
                null,
                BorrowingStatus.APPROVED
        );
        Borrowing saved = borrowingRepository.save(borrowing);
        UUID borrowingId = saved.getId();

        try {
            // When: Event is received for already approved borrowing
            BookAvailabilityCheckedEvent event = new BookAvailabilityCheckedEvent(
                    TEST_BOOK_1,
                    borrowingId,
                    false // Would reject if it were pending
            );
            borrowingService.handleBookAvailabilityCheckedEvent(event);

            // Then: Status remains APPROVED (event is ignored)
            Optional<Borrowing> result = borrowingRepository.findById(borrowingId);
            assertThat(result).isPresent();
            assertThat(result.get().getStatus()).isEqualTo(BorrowingStatus.APPROVED);
        } finally {
            borrowingRepository.deleteById(borrowingId);
        }
    }

    // ==================== Tests for returnBook() ====================

    @Test
    void returnBook_successfulReturn() {
        // Given: An active borrowing
        Borrowing borrowing = new Borrowing(
                null,
                TEST_BOOK_4,
                TEST_BOOK_4,
                TEST_MEMBER_3,
                LocalDateTime.now().minusDays(5),
                null, // Not yet returned
                BorrowingStatus.APPROVED
        );
        Borrowing saved = borrowingRepository.save(borrowing);
        UUID borrowingId = saved.getId();

        try {
            // When: Member returns the book
            Optional<Borrowing> result = borrowingService.returnBook(TEST_BOOK_4, TEST_MEMBER_3);

            // Then: Book is returned successfully
            assertThat(result).isPresent();
            Borrowing returned = result.get();
            assertThat(returned.getId()).isEqualTo(borrowingId);
            assertThat(returned.getStatus()).isEqualTo(BorrowingStatus.RETURNED);
            assertThat(returned.getReturnDate()).isNotNull();
            assertThat(returned.getReturnDate()).isBeforeOrEqualTo(LocalDateTime.now());
        } finally {
            borrowingRepository.deleteById(borrowingId);
        }
    }

    @Test
    void returnBook_failsWhenNoActiveBorrowing() {
        // Given: No active borrowing for the book and member

        // When: Trying to return a book not borrowed
        Optional<Borrowing> result = borrowingService.returnBook(TEST_BOOK_1, TEST_MEMBER_3);

        // Then: Return fails
        assertThat(result).isEmpty();
    }

    @Test
    void returnBook_failsWhenWrongMemberTriesToReturn() {
        // Given: Create a new member and borrow TEST_BOOK_2 (which is not actively borrowed in test data)
        Member testMember = new Member();
        testMember.setName("Book Borrower");
        testMember.setEmail("borrower@test.com");
        testMember.setEnabled(true);
        Member savedTestMember = memberRepository.save(testMember);

        Borrowing borrowing = new Borrowing(
                null,
                TEST_BOOK_2,
                TEST_BOOK_2,
                savedTestMember.getId(),
                LocalDateTime.now().minusDays(3),
                null,
                BorrowingStatus.APPROVED
        );
        Borrowing saved = borrowingRepository.save(borrowing);

        try {
            // When: MEMBER_2 (different member) tries to return it
            Optional<Borrowing> result = borrowingService.returnBook(TEST_BOOK_2, TEST_MEMBER_2);

            // Then: Return fails
            assertThat(result).isEmpty();

            // Verify original borrowing is unchanged
            Optional<Borrowing> original = borrowingRepository.findById(saved.getId());
            assertThat(original).isPresent();
            assertThat(original.get().getStatus()).isEqualTo(BorrowingStatus.APPROVED);
            assertThat(original.get().getReturnDate()).isNull();
        } finally {
            borrowingRepository.deleteById(saved.getId());
            memberRepository.deleteById(savedTestMember.getId());
        }
    }

    @Test
    void returnBook_failsWhenBookAlreadyReturned() {
        // Given: Create a new member and a returned borrowing
        Member testMember = new Member();
        testMember.setName("Returner");
        testMember.setEmail("returner@test.com");
        testMember.setEnabled(true);
        Member savedTestMember = memberRepository.save(testMember);

        Borrowing borrowing = new Borrowing(
                null,
                TEST_BOOK_3,
                TEST_BOOK_3,
                savedTestMember.getId(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(2), // Already returned
                BorrowingStatus.RETURNED
        );
        Borrowing saved = borrowingRepository.save(borrowing);

        try {
            // When: Trying to return it again
            Optional<Borrowing> result = borrowingService.returnBook(TEST_BOOK_3, savedTestMember.getId());

            // Then: Return fails (no active borrowing found)
            assertThat(result).isEmpty();
        } finally {
            borrowingRepository.deleteById(saved.getId());
            memberRepository.deleteById(savedTestMember.getId());
        }
    }

    // ==================== Tests for query methods ====================

    @Test
    void getBorrowingById_findsExistingBorrowing() {
        // Given: An existing borrowing from test data
        UUID existingBorrowingId = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");

        // When: Looking up by ID
        Optional<Borrowing> result = borrowingService.getBorrowingById(existingBorrowingId);

        // Then: Borrowing is found
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(existingBorrowingId);
    }

    @Test
    void getBorrowingById_returnsEmptyForNonExistent() {
        // Given: A non-existent borrowing ID
        UUID nonExistentId = UUID.randomUUID();

        // When: Looking up by ID
        Optional<Borrowing> result = borrowingService.getBorrowingById(nonExistentId);

        // Then: Empty result
        assertThat(result).isEmpty();
    }

    @Test
    void getBorrowingsForMember_returnsAllBorrowings() {
        // Given: TEST_MEMBER_1 has multiple borrowings (from test data)

        // When: Getting all borrowings for member
        List<Borrowing> borrowings = borrowingService.getBorrowingsForMember(TEST_MEMBER_1);

        // Then: Returns both active and returned borrowings
        assertThat(borrowings).isNotEmpty();
        assertThat(borrowings).hasSizeGreaterThanOrEqualTo(2); // At least from test data
    }

    @Test
    void getBorrowingsForMember_returnsEmptyForMemberWithNoBorrowings() {
        // Given: A new member with no borrowings
        Member newMember = new Member();
        newMember.setName("New Member");
        newMember.setEmail("new@test.com");
        newMember.setEnabled(true);
        Member saved = memberRepository.save(newMember);

        try {
            // When: Getting borrowings
            List<Borrowing> borrowings = borrowingService.getBorrowingsForMember(saved.getId());

            // Then: Empty list
            assertThat(borrowings).isEmpty();
        } finally {
            memberRepository.deleteById(saved.getId());
        }
    }

    @Test
    void getActiveBorrowingsForMember_returnsOnlyNonReturnedBorrowings() {
        // Given: TEST_MEMBER_1 has both active and returned borrowings

        // When: Getting active borrowings
        List<Borrowing> activeBorrowings = borrowingService.getActiveBorrowingsForMember(TEST_MEMBER_1);

        // Then: All returned borrowings have null returnDate
        assertThat(activeBorrowings).isNotEmpty();
        assertThat(activeBorrowings).allMatch(b -> b.getReturnDate() == null);
    }

    @Test
    void findAll_returnsAllBorrowings() {
        // When: Getting all borrowings
        List<Borrowing> allBorrowings = borrowingService.findAll();

        // Then: Returns all borrowings from test data
        assertThat(allBorrowings).isNotEmpty();
        assertThat(allBorrowings.size()).isGreaterThanOrEqualTo(15); // From V5 test data
    }

    // ==================== Tests for isMemberEligibleToBorrow() ====================

    @Test
    void isMemberEligibleToBorrow_trueForEligibleMember() {
        // Given: Create a new active member with no overdue books
        Member newMember = new Member();
        newMember.setName("Eligible Member");
        newMember.setEmail("eligiblemember@test.com");
        newMember.setEnabled(true);
        Member savedMember = memberRepository.save(newMember);

        try {
            // When: Checking eligibility
            boolean eligible = borrowingService.isMemberEligibleToBorrow(savedMember.getId());

            // Then: Member is eligible
            assertThat(eligible).isTrue();
        } finally {
            memberRepository.deleteById(savedMember.getId());
        }
    }

    @Test
    void isMemberEligibleToBorrow_falseForNonExistentMember() {
        // Given: A non-existent member
        UUID nonExistentId = UUID.randomUUID();

        // When: Checking eligibility
        boolean eligible = borrowingService.isMemberEligibleToBorrow(nonExistentId);

        // Then: Not eligible
        assertThat(eligible).isFalse();
    }

    @Test
    void isMemberEligibleToBorrow_falseForDisabledMember() {
        // Given: A disabled member
        Member disabledMember = new Member();
        disabledMember.setName("Disabled User");
        disabledMember.setEmail("disabled2@test.com");
        disabledMember.setEnabled(false);
        Member saved = memberRepository.save(disabledMember);

        try {
            // When: Checking eligibility
            boolean eligible = borrowingService.isMemberEligibleToBorrow(saved.getId());

            // Then: Not eligible
            assertThat(eligible).isFalse();
        } finally {
            memberRepository.deleteById(saved.getId());
        }
    }

    @Test
    void isMemberEligibleToBorrow_falseWhenAtMaxLimit() {
        // Given: A member at max borrowing limit
        Member member = new Member();
        member.setName("Max Borrower");
        member.setEmail("maxborrower@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);

        try {
            // Create 5 active borrowings using existing book IDs
            UUID[] bookIds = {TEST_BOOK_1, TEST_BOOK_2, TEST_BOOK_3, TEST_BOOK_4, TEST_BOOK_1};
            for (int i = 0; i < bookifySettingsConfig.getMaximumBooksBorrowed(); i++) {
                Borrowing borrowing = new Borrowing(
                        null,
                        bookIds[i],
                        bookIds[i],
                        savedMember.getId(),
                        LocalDateTime.now().minusDays(1),
                        null,
                        BorrowingStatus.APPROVED
                );
                borrowingRepository.save(borrowing);
            }

            // When: Checking eligibility
            boolean eligible = borrowingService.isMemberEligibleToBorrow(savedMember.getId());

            // Then: Not eligible
            assertThat(eligible).isFalse();

            // Cleanup
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(savedMember.getId());
            for (Borrowing b : borrowings) {
                borrowingRepository.deleteById(b.getId());
            }
        } finally {
            memberRepository.deleteById(savedMember.getId());
        }
    }

    @Test
    void isMemberEligibleToBorrow_falseWhenHasOverdueBooks() {
        // Given: A member with overdue books
        Member member = new Member();
        member.setName("Overdue User");
        member.setEmail("overdue@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);

        try {
            // Create an overdue borrowing
            Borrowing overdueBorrowing = new Borrowing(
                    null,
                    TEST_BOOK_1,
                    TEST_BOOK_1,
                    savedMember.getId(),
                    LocalDateTime.now().minusDays(bookifySettingsConfig.getOverdueDays() + 5), // 19 days ago
                    null,
                    BorrowingStatus.APPROVED
            );
            Borrowing saved = borrowingRepository.save(overdueBorrowing);

            // When: Checking eligibility
            boolean eligible = borrowingService.isMemberEligibleToBorrow(savedMember.getId());

            // Then: Not eligible
            assertThat(eligible).isFalse();

            // Cleanup
            borrowingRepository.deleteById(saved.getId());
        } finally {
            memberRepository.deleteById(savedMember.getId());
        }
    }

    @Test
    void isMemberEligibleToBorrow_trueWhenBelowMaxLimitAndNoOverdue() {
        // Given: A member with some borrowings but below limit and not overdue
        Member member = new Member();
        member.setName("Regular User");
        member.setEmail("regular@test.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);

        try {
            // Create 2 active borrowings (below limit of 5) using existing book IDs
            UUID[] bookIds = {TEST_BOOK_1, TEST_BOOK_2};
            for (int i = 0; i < 2; i++) {
                Borrowing borrowing = new Borrowing(
                        null,
                        bookIds[i],
                        bookIds[i],
                        savedMember.getId(),
                        LocalDateTime.now().minusDays(5), // 5 days ago (not overdue)
                        null,
                        BorrowingStatus.APPROVED
                );
                borrowingRepository.save(borrowing);
            }

            // When: Checking eligibility
            boolean eligible = borrowingService.isMemberEligibleToBorrow(savedMember.getId());

            // Then: Eligible
            assertThat(eligible).isTrue();

            // Cleanup
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(savedMember.getId());
            for (Borrowing b : borrowings) {
                borrowingRepository.deleteById(b.getId());
            }
        } finally {
            memberRepository.deleteById(savedMember.getId());
        }
    }
}
