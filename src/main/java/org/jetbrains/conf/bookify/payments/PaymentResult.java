package org.jetbrains.conf.bookify.payments;

import org.jspecify.annotations.Nullable;

record PaymentResult(boolean successful, @Nullable String transactionId, String message) {
}
