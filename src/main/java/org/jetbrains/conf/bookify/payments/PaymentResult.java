package org.jetbrains.conf.bookify.payments;

record PaymentResult(boolean successful, String transactionId, String message) {
}
