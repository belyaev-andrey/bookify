package org.jetbrains.conf.bookify.payments;

import java.math.BigDecimal;
import java.util.UUID;

record PaymentRequest(UUID memberId, UUID bookId, BigDecimal amount) {
}
