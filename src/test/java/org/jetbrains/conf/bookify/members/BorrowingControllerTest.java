package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbConfiguration.class)
class BorrowingControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    // Test UUID for a book that exists in the initial data
    private static final UUID TEST_BOOK_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @Test
    void testBorrowingWorkflow() throws Exception {
        // 1. Create a test member
        Member member = new Member();
        member.setName("Test Member");
        member.setEmail("test@example.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // 2. Create a borrowing request (PENDING)
            var borrowRequestResult = mockMvc.post()
                    .uri("/api/borrowings/borrow?bookId=" + TEST_BOOK_ID + "&memberId=" + memberId);

            assertThat(borrowRequestResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // Get the borrowing ID from the repository
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            assertThat(borrowings).isNotEmpty();
            UUID borrowingId = borrowings.get(0).getId();

            // Verify the borrowing status is initially PENDING
            Borrowing borrowing = borrowingRepository.findById(borrowingId).orElseThrow();
            assertThat(borrowing.getStatus()).isIn(BorrowingStatus.PENDING, BorrowingStatus.APPROVED);

            // Wait for the book availability check to complete and update the status
            // Use polling with timeout instead of Thread.sleep
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 seconds timeout
            BorrowingStatus status = BorrowingStatus.PENDING;

            while (status == BorrowingStatus.PENDING && System.currentTimeMillis() - startTime < timeout) {
                Thread.sleep(100); // Small sleep to avoid hammering the database
                borrowing = borrowingRepository.findById(borrowingId).orElseThrow();
                status = borrowing.getStatus();
            }

            // Verify the borrowing status is now APPROVED (assuming the book is available in the test database)
            assertThat(status).isEqualTo(BorrowingStatus.APPROVED);

            // 4. Get the borrowing by ID
            var getBorrowingResult = mockMvc.get()
                    .uri("/api/borrowings/" + borrowingId);

            assertThat(getBorrowingResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // 5. Get all borrowings for the member
            var getMemberBorrowingsResult = mockMvc.get()
                    .uri("/api/borrowings/member/" + memberId);

            assertThat(getMemberBorrowingsResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // 6. Get active borrowings for the member
            var getActiveBorrowingsResult = mockMvc.get()
                    .uri("/api/borrowings/member/" + memberId + "/active");

            assertThat(getActiveBorrowingsResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // 7. Return the book
            var returnBookResult = mockMvc.post()
                    .uri("/api/borrowings/return?bookId=" + TEST_BOOK_ID + "&memberId=" + memberId);

            assertThat(returnBookResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // Verify the book has been returned
            borrowing = borrowingRepository.findById(borrowingId).orElseThrow();
            assertThat(borrowing.isReturned()).isTrue();
        } finally {
            // Clean up
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            for (Borrowing b : borrowings) {
                borrowingRepository.deleteById(b.getId());
            }
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void testBorrowingRejection() throws Exception {
        // 1. Create a test member
        Member member = new Member();
        member.setName("Test Member 2");
        member.setEmail("test2@example.com");
        member.setEnabled(true);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // Use a non-existent book ID to simulate a book that is not available
            UUID nonExistentBookId = UUID.randomUUID();

            // 2. Create a borrowing request for a non-existent book
            var borrowRequestResult = mockMvc.post()
                    .uri("/api/borrowings/borrow?bookId=" + nonExistentBookId + "&memberId=" + memberId);

            assertThat(borrowRequestResult)
                    .hasStatus(HttpStatus.OK)
                    .bodyJson();

            // Get the borrowing ID from the repository
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            assertThat(borrowings).isNotEmpty();
            UUID borrowingId = borrowings.get(0).getId();

            Borrowing borrowing = borrowingRepository.findById(borrowingId).orElseThrow();
            assertThat(borrowing.getStatus()).isIn(BorrowingStatus.REJECTED, BorrowingStatus.PENDING);

            // Wait for the book availability check to complete and update the status
            // Use polling with timeout instead of Thread.sleep
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // 5 seconds timeout
            BorrowingStatus status = BorrowingStatus.PENDING;

            while (status == BorrowingStatus.PENDING && System.currentTimeMillis() - startTime < timeout) {
                Thread.sleep(100); // Small sleep to avoid hammering the database
                borrowing = borrowingRepository.findById(borrowingId).orElseThrow();
                status = borrowing.getStatus();
            }

            // Verify the borrowing status is now REJECTED (since the book doesn't exist)
            assertThat(status).isEqualTo(BorrowingStatus.REJECTED);

            // Verify that the requestedBookId field is set to the non-existent book ID and the bookId field is null
            assertThat(borrowing.getRequestedBookId()).isEqualTo(nonExistentBookId);
            assertThat(borrowing.getBookId()).isNull();
        } finally {
            // Clean up
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            for (Borrowing b : borrowings) {
                borrowingRepository.deleteById(b.getId());
            }
            memberRepository.deleteById(memberId);
        }
    }

    @Test
    void testIneligibleMemberCannotBorrow() throws Exception {
        // 1. Create a disabled member
        Member member = new Member();
        member.setName("Disabled Member");
        member.setEmail("disabled@example.com");
        member.setEnabled(false);
        Member savedMember = memberRepository.save(member);
        UUID memberId = savedMember.getId();

        try {
            // 2. Try to create a borrowing request
            var borrowRequestResult = mockMvc.post()
                    .uri("/api/borrowings/borrow?bookId=" + TEST_BOOK_ID + "&memberId=" + memberId);

            // Should fail with 404 Not Found
            assertThat(borrowRequestResult)
                    .hasStatus(HttpStatus.NOT_FOUND);

            // Verify no borrowing was created
            List<Borrowing> borrowings = borrowingRepository.findByMemberId(memberId);
            assertThat(borrowings).isEmpty();
        } finally {
            // Clean up
            memberRepository.deleteById(memberId);
        }
    }
}
