package org.jetbrains.conf.bookify.books;

import jakarta.validation.constraints.NotBlank;

record BookRequest(@NotBlank String name, @NotBlank String isbn, Boolean available) {
}
