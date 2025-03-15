package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookBorrowRequestEvent;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

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
    public Book addBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Remove a book from the catalogue
     * @param id the id of the book to remove
     */
    @Transactional
    public void removeBook(UUID id) {
        bookRepository.deleteById(id);
    }

    /**
     * Search for books by name
     * @param name the name to search for
     * @return a list of books matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<Book> searchBooksByName(String name) {
        return bookRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get all books in the catalogue
     * @return a list of all books
     */
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        bookRepository.findAll().forEach(books::add);
        return books;
    }

    /**
     * Get a book by its id
     * @param id the id of the book
     * @return the book, if found
     */
    @Transactional(readOnly = true)
    public Optional<Book> findById(UUID id) {
        return bookRepository.findById(id);
    }

    /**
     * Check if a book is available for borrowing
     * @param id the id of the book
     * @return true if the book is available, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isBookAvailable(UUID id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        return bookOpt.map(Book::isAvailable).orElse(false);
    }

    /**
     * Mark a book as borrowed (unavailable)
     * @param id the id of the book
     * @return the updated book if found and available, empty otherwise
     */
    @Transactional
    public Optional<Book> markBookAsBorrowed(UUID id) {
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
    public Optional<Book> markBookAsReturned(UUID id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setAvailable(true);
            return Optional.of(bookRepository.save(book));
        }
        return Optional.empty();
    }

    /**
     * Event listener for when a book is borrowed.
     * @param event the book borrowed event
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookBorrowedEvent(BookBorrowRequestEvent event) {
        Optional<Book> foundBook = bookRepository.findById(event.bookId());
        if (foundBook.isPresent()) {
            markBookAsBorrowed(event.bookId());
        }
        eventPublisher.publishEvent(new BookAvailabilityCheckedEvent(event.bookId(), event.borrowId(), foundBook.isPresent()));
    }

    /**
     * Event listener for when a book is returned.
     * @param event the book returned event
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookReturnedEvent(BookReturnedEvent event) {
        markBookAsReturned(event.bookId());
    }
}
