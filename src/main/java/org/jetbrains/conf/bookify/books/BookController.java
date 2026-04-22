/*
 * Test
 */

package org.jetbrains.conf.bookify.books;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/books")
class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    /**
     * Get all books in the catalogue
     *
     * @return a list of all books
     */
    @GetMapping("")
    ResponseEntity<List<BookResponse>> getAll() {
        List<Book> bookList = bookService.findAll();
        return new ResponseEntity<>(bookMapper.toResponseList(bookList), HttpStatus.OK);
    }

    /**
     * Add a book to the catalogue
     *
     * @param request the book to add
     * @return the added book
     */
    @PostMapping("")
    ResponseEntity<Object> addBook(@RequestBody BookRequest request) {
        Book savedBook = bookService.saveBook(bookMapper.toEntity(request));
        return ResponseEntity.created(URI.create("/api/books/%s".formatted(savedBook.getId()))).build();
    }

    @PutMapping("")
    public ResponseEntity<BookResponse> updateBook(@RequestBody BookUpdateRequest request) {
        return bookService.findById(request.id())
                .map(b -> ResponseEntity.ok(bookMapper.toResponse(bookService.saveBook(bookMapper.toEntity(request)))))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Remove a book from the catalogue
     *
     * @param id the id of the book to remove
     * @return no content
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> removeBook(@PathVariable UUID id) {
        bookService.removeBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable UUID id) {
        return bookService.findById(id)
                .map(bookMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search for books by name
     *
     * @param name the name to search for
     * @return a list of books matching the search criteria
     */
    @GetMapping("/search")
    ResponseEntity<List<BookResponse>> searchBooksByName(@RequestParam String name) {
        List<Book> books = bookService.searchBooksByName(name);
        return new ResponseEntity<>(bookMapper.toResponseList(books), HttpStatus.OK);
    }
}
