package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookBorrowRequestEvent;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class BookService {

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;

    BookService(BookRepository bookRepository, ApplicationEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Add a book to the catalogue
     * @param book the book to add
     * @return the saved book
     */
    @Transactional
    Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Remove a book from the catalogue
     * @param id the id of the book to remove
     */
    @Transactional
    void removeBook(UUID id) {
        bookRepository.deleteById(id);
    }

    /**
     * Search for books by name
     * @param name the name to search for
     * @return a list of books matching the search criteria
     */
    @Transactional(readOnly = true)
    List<Book> searchBooksByName(String name) {
        return bookRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get all books in the catalogue
     * @return a list of all books
     */
    @Transactional(readOnly = true)
    List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        bookRepository.findAll().forEach(books::add);
        return books;
    }
    /**
     * Mark a book as borrowed (unavailable)
     * @param id the id of the book
     * @return the updated book if found and available, empty otherwise
     */
    @Transactional
    Optional<Book> markBookAsBorrowed(UUID id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            if (book.isAvailable()) {
                book.setAvailable(false);
                return Optional.of(bookRepository.save(book));
            }
        }
        return Optional.empty();
    }

    /**
     * Mark a book as returned (available)
     * @param id the id of the book
     * @return the updated book if found, empty otherwise
     */
    @Transactional
    Optional<Book> markBookAsReturned(UUID id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setAvailable(true);
            return Optional.of(bookRepository.save(book));
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(@Nullable UUID id) {
        return bookRepository.findById(id);
    }

    /**
     * Event listener for when a book is borrowed.
     * @param event the book borrowed event
     */
    @ApplicationModuleListener
    void handleBookBorrowedEvent(BookBorrowRequestEvent event) {
        Optional<Book> updatedBook = markBookAsBorrowed(event.bookId());
        eventPublisher.publishEvent(new BookAvailabilityCheckedEvent(event.bookId(), event.borrowId(), updatedBook.isPresent()));
    }

    /**
     * Event listener for when a book is returned.
     * @param event the book returned event
     */
    @ApplicationModuleListener
    void handleBookReturnedEvent(BookReturnedEvent event) {
        markBookAsReturned(event.bookId());
    }

}
