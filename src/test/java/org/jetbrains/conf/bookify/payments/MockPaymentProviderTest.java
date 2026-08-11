package org.jetbrains.conf.bookify.payments;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentProviderTest {

    private final MockPaymentProvider provider = new MockPaymentProvider();

    @Test
    void charge_alwaysSucceedsWithoutCallingAnyExternalSystem() {
        PaymentRequest request = new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("12.50"));

        PaymentResult result = provider.charge(request);

        assertThat(result.successful()).isTrue();
        assertThat(result.transactionId()).startsWith("MOCK-");
    }
}
