package org.jetbrains.conf.bookify.members;

import java.time.LocalDateTime;
import java.util.UUID;

record BorrowingResponse(
        UUID id,
        UUID bookId,
        UUID requestedBookId,
        UUID memberId,
        LocalDateTime borrowDate,
        LocalDateTime returnDate,
        BorrowingStatus status) {
}
