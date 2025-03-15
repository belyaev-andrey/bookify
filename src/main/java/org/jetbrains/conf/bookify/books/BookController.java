package org.jetbrains.conf.bookify.books;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/books")
class BookController {

    private final BookService bookService;

    BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get all books in the catalogue
     * @return a list of all books
     */
    @GetMapping("")
    ResponseEntity<List<Book>> getAll() {
        List<Book> bookList = bookService.findAll();
        return new ResponseEntity<>(bookList, HttpStatus.OK);
    }

    /**
     * Add a book to the catalogue
     * @param book the book to add
     * @return the added book
     */
    @PostMapping("")
    ResponseEntity<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookService.addBook(book);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    /**
     * Remove a book from the catalogue
     * @param id the id of the book to remove
     * @return no content
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> removeBook(@PathVariable UUID id) {
        bookService.removeBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Search for books by name
     * @param name the name to search for
     * @return a list of books matching the search criteria
     */
    @GetMapping("/search")
    ResponseEntity<List<Book>> searchBooksByName(@RequestParam String name) {
        List<Book> books = bookService.searchBooksByName(name);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }
}
