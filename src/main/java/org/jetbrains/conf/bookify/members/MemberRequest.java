package org.jetbrains.conf.bookify.members;

import jakarta.validation.constraints.NotBlank;

record MemberRequest(@NotBlank String name, @NotBlank String email, @NotBlank String password) {
}
