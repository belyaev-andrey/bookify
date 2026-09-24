package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.jetbrains.conf.bookify.events.BookReturnedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.jetbrains.conf.bookify.books.BookService.ALL_BOOKS_CACHE;
import static org.jetbrains.conf.bookify.books.BookService.BOOKS_CACHE;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class BookCachingTest {

    private static final UUID SEEDED_BOOK_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13");

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CacheManager cacheManager;

    // Also after each test: its cleanup deletes through the repository, which bypasses eviction
    @BeforeEach
    @AfterEach
    void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cache(name).clear());
    }

    @Test
    void findByIdIsServedFromCache() {
        Book first = bookService.findById(SEEDED_BOOK_ID).orElseThrow();
        Book second = bookService.findById(SEEDED_BOOK_ID).orElseThrow();

        assertThat(second).isSameAs(first);
        assertThat(cache(BOOKS_CACHE).get(SEEDED_BOOK_ID)).isNotNull();
    }

    @Test
    void findByIdDoesNotCacheMissingBook() {
        UUID missingId = UUID.randomUUID();

        assertThat(bookService.findById(missingId)).isEmpty();
        assertThat(cache(BOOKS_CACHE).get(missingId)).isNull();
    }

    @Test
    void saveBookRefreshesCachedBookAndEvictsList() {
        Book saved = bookService.saveBook(new Book(null, "Cache Test Book", "9780000000001", true));
        UUID bookId = Objects.requireNonNull(saved.getId());

        try {
            bookService.findById(bookId);
            bookService.findAll();
            assertThat(cache(ALL_BOOKS_CACHE).get(SimpleKey.EMPTY)).isNotNull();

            bookService.saveBook(new Book(bookId, "Renamed Cache Test Book", "9780000000001", true));

            Book cached = cache(BOOKS_CACHE).get(bookId, Book.class);
            assertThat(cached).isNotNull();
            assertThat(cached.getName()).isEqualTo("Renamed Cache Test Book");
            assertThat(cache(ALL_BOOKS_CACHE).get(SimpleKey.EMPTY)).isNull();
        } finally {
            bookRepository.deleteById(bookId);
        }
    }

    @Test
    void returnListenerEvictsCachedBook() {
        Book book = bookRepository.save(new Book(null, "Cache Test Book", "9780000000002", false));
        UUID bookId = Objects.requireNonNull(book.getId());

        try {
            assertThat(bookService.findById(bookId).orElseThrow().isAvailable()).isFalse();

            bookService.handleBookReturnedEvent(new BookReturnedEvent(bookId, UUID.randomUUID()));

            // The listener is @Async; its eviction is the last thing it does, after its transaction commits
            await().atMost(Duration.ofSeconds(5)).until(() -> cache(BOOKS_CACHE).get(bookId) == null);

            assertThat(bookService.findById(bookId).orElseThrow().isAvailable()).isTrue();
        } finally {
            bookRepository.deleteById(bookId);
        }
    }

    private Cache cache(String name) {
        return Objects.requireNonNull(cacheManager.getCache(name));
    }
}
