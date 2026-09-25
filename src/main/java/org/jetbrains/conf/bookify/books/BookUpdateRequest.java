package org.jetbrains.conf.bookify.books;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

record BookUpdateRequest(@Nullable UUID id, String name, String isbn, Boolean available) {
}
