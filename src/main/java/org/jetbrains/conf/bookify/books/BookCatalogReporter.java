package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookBorrowRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Records demand the catalogue could not meet.
 *
 * <p>{@link BookAvailabilityCheckedEvent} carries only a book id and a flag, so a rejected borrow
 * request tells the members module nothing about <em>which</em> title was wanted. This module owns
 * that information, so it names the book, turning a rejection into a signal about which titles need
 * more copies.</p>
 *
 * <p>Its two dependencies are deliberately of different kinds: {@code bookCatalogWarmup} is an
 * ordering dependency declared with {@link DependsOn} and never handed to this bean, while
 * {@code bookRepository} is injected. A bean inspector should report the first under "Depends on"
 * and the second under "Injects".</p>
 */
@Component
@DependsOn("bookCatalogWarmup")
class BookCatalogReporter {

    private static final Logger log = LoggerFactory.getLogger(BookCatalogReporter.class);

    private final BookRepository bookRepository;

    BookCatalogReporter(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener
    void reportUnmetDemand(BookAvailabilityCheckedEvent event) {
        if (event.available()) {
            return;
        }
        bookRepository.findById(event.bookId()).ifPresent(book -> log.info(
                "Borrow request {} could not be met: '{}' (ISBN {}) is on loan",
                event.borrowingId(), book.getName(), book.getIsbn()));
    }
}

/**
 * Flags borrow requests that name a book the catalogue does not hold.
 *
 * <p>{@link BookService#handleBookBorrowedEvent} reports an unknown book the same way it reports one
 * that is merely on loan — as unavailable — so a request for a book that was never catalogued is
 * otherwise indistinguishable from ordinary contention. Checking the catalogue when the request
 * arrives restores that distinction.</p>
 *
 * <p>This bean covers the two cases that cannot be resolved from the container's dependency map
 * alone. {@code catalogWarmup} is an <em>alias</em>, so it only matches a bean after being resolved
 * to the canonical name {@code bookCatalogWarmup}. And {@code bookRepository} is both declared here
 * and injected, which the container records as a single dependency edge, so it is reported as a
 * declared one.</p>
 */
@Component
@DependsOn({"catalogWarmup", "bookRepository"})
class BookCatalogAuditor {

    private static final Logger log = LoggerFactory.getLogger(BookCatalogAuditor.class);

    private final BookRepository bookRepository;

    BookCatalogAuditor(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener
    void auditBorrowRequest(BookBorrowRequestEvent event) {
        if (bookRepository.findById(event.bookId()).isEmpty()) {
            log.warn("Borrow request {} names book {}, which is not in the catalogue",
                    event.borrowId(), event.bookId());
        }
    }
}
