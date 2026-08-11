package org.jetbrains.conf.bookify.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Always approves charges without contacting any external system.
 * Active by default, and whenever {@code bookify.payments.provider=mock}.
 */
@Component
@ConditionalOnProperty(prefix = "bookify.payments", name = "provider", havingValue = "mock", matchIfMissing = true)
class MockPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentProvider.class);

    @Override
    public PaymentResult charge(PaymentRequest request) {
        String transactionId = "MOCK-" + UUID.randomUUID();
        log.info("[mock] Charged {} to member {} for book {} (transaction {})",
                request.amount(), request.memberId(), request.bookId(), transactionId);
        return new PaymentResult(true, transactionId, "Simulated payment approved");
    }
}
