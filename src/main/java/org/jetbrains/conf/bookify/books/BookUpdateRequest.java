package org.jetbrains.conf.bookify.books;

import java.util.UUID;

record BookUpdateRequest(UUID id, String name, String isbn, Boolean available) {
}
