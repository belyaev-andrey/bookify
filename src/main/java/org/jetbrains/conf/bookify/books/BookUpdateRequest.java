package org.jetbrains.conf.bookify.books;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

record BookUpdateRequest(@Nullable UUID id, @NotBlank String name, @NotBlank String isbn, Boolean available) {
}
