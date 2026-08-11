package org.jetbrains.conf.bookify.payments;

import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class PaymentsAPITest {

    @Autowired
    private PaymentsAPI paymentsAPI;

    @Autowired
    private BookFineRateRepository fineRateRepository;

    // Books seeded with fine-rate fixtures in V102__payment_test_data.sql
    private static final UUID BOOK_WITH_RATE_HISTORY = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"); // 0.50@2024-01-01, 0.75@2024-06-01, 1.00@2024-09-01
    private static final UUID BOOK_WITHOUT_RATE = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13"); // no fine rate configured

    @Test
    void setFineRate_persistsRateRetrievableThroughApi() {
        UUID newBookId = UUID.randomUUID();

        paymentsAPI.setFineRate(newBookId, new BigDecimal("0.50"), LocalDate.of(2024, 1, 1));

        try {
            Optional<BigDecimal> result = paymentsAPI.getApplicableRate(newBookId, LocalDate.of(2024, 1, 1));

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualByComparingTo("0.50");
        } finally {
            fineRateRepository.findApplicableRate(newBookId, LocalDate.of(2024, 1, 1))
                    .ifPresent(rate -> fineRateRepository.deleteById(rate.getId()));
        }
    }

    @Test
    void getApplicableRate_returnsEmptyWhenNoRateConfigured() {
        Optional<BigDecimal> result = paymentsAPI.getApplicableRate(BOOK_WITHOUT_RATE, LocalDate.of(2024, 1, 1));

        assertThat(result).isEmpty();
    }

    @Test
    void getApplicableRate_returnsOnlyThePriceNotTheWholeEntity() {
        Optional<BigDecimal> result = paymentsAPI.getApplicableRate(BOOK_WITH_RATE_HISTORY, LocalDate.of(2024, 12, 31));

        assertThat(result).contains(new BigDecimal("1.00"));
    }
}
