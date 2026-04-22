/*
 * Test
 */

/*
 * Test
 */

package org.jetbrains.conf.bookify.members;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/borrowings")
class BorrowingController {

    private final BorrowingService borrowingService;
    private final BorrowingMapper borrowingMapper;

    BorrowingController(BorrowingService borrowingService, BorrowingMapper borrowingMapper) {
        this.borrowingService = borrowingService;
        this.borrowingMapper = borrowingMapper;
    }

    /**
     * Retrieves a list of all borrowing records.
     *
     * @return a ResponseEntity containing a list of all borrowings
     */
    @GetMapping(value = "")
    public ResponseEntity<List<BorrowingResponse>> getAll() {
        return ResponseEntity.ok(borrowingMapper.toResponseList(borrowingService.findAll()));
    }
    
    /**
     * Create a borrowing request for a member.
     * @param bookId the ID of the book to borrow
     * @param memberId the ID of the member borrowing the book
     * @return the borrowing request if successful, 404 otherwise
     */
    @PostMapping(value = "/borrow", produces = "application/json")
    ResponseEntity<Object> borrowBook(@RequestParam UUID bookId, @RequestParam UUID memberId) {
        return borrowingService.borrowBook(bookId, memberId)
                .map(b -> ResponseEntity.created(URI.create("/api/borrowings/%s".formatted(b.getId()))).build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a borrowing request by ID.
     * @param borrowingId the ID of the borrowing request
     * @return the borrowing request if found, 404 otherwise
     */
    @GetMapping("/{borrowingId}")
    ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable UUID borrowingId) {
        return borrowingService.getBorrowingById(borrowingId)
                .map(borrowingMapper::toResponse)
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
    ResponseEntity<Object> returnBook(@RequestParam UUID bookId, @RequestParam UUID memberId) {
        return borrowingService.returnBook(bookId, memberId)
                .map(b -> ResponseEntity.accepted().build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of borrowings for the member
     */
    @GetMapping("/member/{memberId}")
    List<BorrowingResponse> getBorrowingsForMember(@PathVariable UUID memberId) {
        return borrowingMapper.toResponseList(borrowingService.getBorrowingsForMember(memberId));
    }

    /**
     * Get all active (not returned) borrowings for a member.
     * @param memberId the ID of the member
     * @return a list of active borrowings for the member
     */
    @GetMapping("/member/{memberId}/active")
    List<BorrowingResponse> getActiveBorrowingsForMember(@PathVariable UUID memberId) {
        return borrowingMapper.toResponseList(borrowingService.getActiveBorrowingsForMember(memberId));
    }
}
