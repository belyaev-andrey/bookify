package org.jetbrains.conf.bookify.books;

import jakarta.persistence.EntityManager;
import org.jetbrains.conf.bookify.events.BookAvailabilityCheckedEvent;
import org.jetbrains.conf.bookify.events.BookBorrowRequestEvent;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
class BookService {

    static final String BOOKS_CACHE = "books";
    static final String ALL_BOOKS_CACHE = "allBooks";

    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    BookService(BookRepository bookRepository, ApplicationEventPublisher eventPublisher, EntityManager entityManager) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    /**
     * Add a book to the catalog.
     * The cache key {@code #result.id} is never null: {@code @GeneratedValue} assigns an id to new books on save.
     * @param book the book to add
     * @return the saved book
     */
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = BOOKS_CACHE, key = "#result.id"),
            evict = @CacheEvict(cacheNames = ALL_BOOKS_CACHE, allEntries = true)
    )
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Remove a book from the catalog
     * @param id the id of the book to remove
     * @throws BookDeleteException if the book has borrowing records (active or historical)
     */
    @Transactional
    @PreAuthorize("hasRole('LIBRARIAN')")
    @Caching(evict = {
            @CacheEvict(cacheNames = BOOKS_CACHE, key = "#id"),
            @CacheEvict(cacheNames = ALL_BOOKS_CACHE, allEntries = true)
    })
    public void removeBook(UUID id) {
        try {
            bookRepository.deleteById(id);
            entityManager.flush();
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            throw new BookDeleteException(id, e);
        }
    }

    /**
     * Search for books by name
     * @param name the name to search for
     * @return a list of books matching the search criteria
     */
    @Transactional(readOnly = true)
    List<Book> searchBooksByName(String name) {
        var sort = Sort.by(Sort.Direction.ASC, "name");
        return bookRepository.findByNameContainingIgnoreCase(name, sort);
    }

    /**
     * Get all books in the catalogue
     * @return a list of all books
     */
    @Transactional(readOnly = true)
    @Cacheable(ALL_BOOKS_CACHE)
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        bookRepository.findAll().forEach(books::add);
        return books;
    }
    /**
     * Mark a book as borrowed (unavailable)
     * @param id the id of the book
     * @return the updated book if found and available, empty otherwise
     */
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
    @Cacheable(cacheNames = BOOKS_CACHE, unless = "#result == null")
    public Optional<Book> findById(@Nullable UUID id) {
        return id == null ? Optional.empty() : bookRepository.findById(id);
    }

    /**
     * Event listener for when a book is borrowed.
     * Evicts here rather than on markBookAsBorrowed, whose self-invocation bypasses the cache proxy.
     * @param event the book borrowed event
     */
    @ApplicationModuleListener
    @Caching(evict = {
            @CacheEvict(cacheNames = BOOKS_CACHE, key = "#event.bookId()"),
            @CacheEvict(cacheNames = ALL_BOOKS_CACHE, allEntries = true)
    })
    public void handleBookBorrowedEvent(BookBorrowRequestEvent event) {
        Optional<Book> updatedBook = markBookAsBorrowed(event.bookId());
        eventPublisher.publishEvent(new BookAvailabilityCheckedEvent(event.bookId(), event.borrowId(), updatedBook.isPresent()));
    }

    /**
     * Event listener for when a book is returned.
     * Evicts here rather than on markBookAsReturned, whose self-invocation bypasses the cache proxy.
     * @param event the book returned event
     */
    @ApplicationModuleListener
    @Caching(evict = {
            @CacheEvict(cacheNames = BOOKS_CACHE, key = "#event.bookId()"),
            @CacheEvict(cacheNames = ALL_BOOKS_CACHE, allEntries = true)
    })
    public void handleBookReturnedEvent(BookReturnedEvent event) {
        markBookAsReturned(event.bookId());
    }

}
