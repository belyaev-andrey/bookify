package org.jetbrains.conf.bookify.members;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrowings")
class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    /**
     * Borrow a book for a member.
     * @param bookId the ID of the book to borrow
     * @param memberId the ID of the member borrowing the book
     * @return the borrowing record if successful, 404 otherwise
     */
    @PostMapping("/borrow")
    public ResponseEntity<Borrowing> borrowBook(@RequestParam UUID bookId, @RequestParam UUID memberId) {
        return borrowingService.borrowBook(bookId, memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Return a borrowed book.
     * @param bookId the ID of the book to return
     * @param memberId the ID of the member returning the book
     * @return the updated borrowing record if successful, 404 otherwise
     */
    @PostMapping("/return")
    public ResponseEntity<Borrowing> returnBook(@RequestParam UUID bookId, @RequestParam UUID memberId) {
        return borrowingService.returnBook(bookId, memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of borrowings for the member
     */
    @GetMapping("/member/{memberId}")
    public List<Borrowing> getBorrowingsForMember(@PathVariable UUID memberId) {
        return borrowingService.getBorrowingsForMember(memberId);
    }

    /**
     * Get all active (not returned) borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of active borrowings for the member
     */
    @GetMapping("/member/{memberId}/active")
    public List<Borrowing> getActiveBorrowingsForMember(@PathVariable UUID memberId) {
        return borrowingService.getActiveBorrowingsForMember(memberId);
    }
}