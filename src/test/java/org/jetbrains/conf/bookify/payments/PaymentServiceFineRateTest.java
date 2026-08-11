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
class PaymentServiceFineRateTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookFineRateRepository fineRateRepository;

    // Books seeded with fine-rate fixtures in V102__payment_test_data.sql
    private static final UUID BOOK_WITH_RATE_HISTORY = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"); // 0.50@2024-01-01, 0.75@2024-06-01, 1.00@2024-09-01
    private static final UUID BOOK_WITH_FUTURE_RATE = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12"); // 2.00@2030-01-01
    private static final UUID BOOK_WITHOUT_RATE = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13"); // no fine rate configured

    // ==================== Tests for setFineRate() ====================

    @Test
    void setFineRate_persistsNewRate() {
        UUID newBookId = UUID.randomUUID();

        BookFineRateEntity saved = paymentService.setFineRate(newBookId, new BigDecimal("0.50"), LocalDate.of(2024, 1, 1));

        try {
            assertThat(saved.getId()).isNotNull();
            assertThat(fineRateRepository.findById(saved.getId())).isPresent();
        } finally {
            fineRateRepository.deleteById(saved.getId());
        }
    }

    @Test
    void setFineRate_allowsMultipleRatesForSameBookWithDifferentEffectiveDates() {
        UUID newBookId = UUID.randomUUID();

        BookFineRateEntity earlyRate = paymentService.setFineRate(newBookId, new BigDecimal("0.50"), LocalDate.of(2024, 1, 1));
        BookFineRateEntity laterRate = paymentService.setFineRate(newBookId, new BigDecimal("0.75"), LocalDate.of(2024, 6, 1));

        try {
            assertThat(paymentService.getApplicableRate(newBookId, LocalDate.of(2024, 1, 1)))
                    .hasValueSatisfying(rate -> assertThat(rate.getPricePerDayOverdue()).isEqualByComparingTo("0.50"));
            assertThat(paymentService.getApplicableRate(newBookId, LocalDate.of(2024, 6, 1)))
                    .hasValueSatisfying(rate -> assertThat(rate.getPricePerDayOverdue()).isEqualByComparingTo("0.75"));
        } finally {
            fineRateRepository.deleteById(earlyRate.getId());
            fineRateRepository.deleteById(laterRate.getId());
        }
    }

    // ==================== Tests for getApplicableRate() ====================

    @Test
    void getApplicableRate_returnsRateWhenAsOfMatchesEffectiveDateExactly() {
        Optional<BookFineRateEntity> result = paymentService.getApplicableRate(BOOK_WITH_RATE_HISTORY, LocalDate.of(2024, 1, 1));

        assertThat(result).isPresent();
        assertThat(result.get().getPricePerDayOverdue()).isEqualByComparingTo("0.50");
    }

    @Test
    void getApplicableRate_returnsMostRecentRateNotAfterAsOfDate() {
        Optional<BookFineRateEntity> result = paymentService.getApplicableRate(BOOK_WITH_RATE_HISTORY, LocalDate.of(2024, 7, 15));

        assertThat(result).isPresent();
        assertThat(result.get().getPricePerDayOverdue()).isEqualByComparingTo("0.75");
    }

    @Test
    void getApplicableRate_ignoresRatesEffectiveInTheFuture() {
        Optional<BookFineRateEntity> result = paymentService.getApplicableRate(BOOK_WITH_FUTURE_RATE, LocalDate.of(2024, 1, 1));

        assertThat(result).isEmpty();
    }

    @Test
    void getApplicableRate_scopesLookupToRequestedBook() {
        // BOOK_WITH_RATE_HISTORY has a rate effective on this date, BOOK_WITHOUT_RATE must not see it
        Optional<BookFineRateEntity> result = paymentService.getApplicableRate(BOOK_WITHOUT_RATE, LocalDate.of(2024, 9, 1));

        assertThat(result).isEmpty();
    }
}
