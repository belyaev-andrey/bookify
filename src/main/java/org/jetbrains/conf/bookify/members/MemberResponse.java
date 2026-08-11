package org.jetbrains.conf.bookify.members;

import java.util.UUID;

record MemberResponse(UUID id, String name, String email, boolean enabled) {
}
