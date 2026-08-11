package org.jetbrains.conf.bookify.payments;

interface PaymentProvider {

    PaymentResult charge(PaymentRequest request);
}
