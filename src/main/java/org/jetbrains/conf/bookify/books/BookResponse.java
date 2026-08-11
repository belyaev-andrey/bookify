package org.jetbrains.conf.bookify.books;

import java.util.UUID;

record BookResponse(UUID id, String name, String isbn, boolean available) {
}
