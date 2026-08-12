package org.jetbrains.conf.bookify.payments;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BookFineRateEntityTest {

    private static final UUID BOOK_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    @Test
    void create_setsFieldsAndIsTransient() {
        BookFineRateEntity entity = BookFineRateEntity.create(BOOK_ID, new BigDecimal("0.50"), LocalDate.of(2024, 1, 1));

        assertThat(entity.getId()).isNull();
        assertThat(entity.getBookId()).isEqualTo(BOOK_ID);
        assertThat(entity.getPricePerDayOverdue()).isEqualByComparingTo("0.50");
        assertThat(entity.getEffectiveDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void create_allowsZeroPrice() {
        BookFineRateEntity entity = BookFineRateEntity.create(BOOK_ID, BigDecimal.ZERO, LocalDate.of(2024, 1, 1));

        assertThat(entity.getPricePerDayOverdue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void create_rejectsNullBookId() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BookFineRateEntity.create(null, new BigDecimal("0.50"), LocalDate.of(2024, 1, 1)))
                .withMessageContaining("bookId");
    }

    @Test
    void create_rejectsNullPrice() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BookFineRateEntity.create(BOOK_ID, null, LocalDate.of(2024, 1, 1)))
                .withMessageContaining("pricePerDayOverdue");
    }

    @Test
    void create_rejectsNegativePrice() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BookFineRateEntity.create(BOOK_ID, new BigDecimal("-0.01"), LocalDate.of(2024, 1, 1)))
                .withMessageContaining("pricePerDayOverdue");
    }

    @Test
    void create_rejectsNullEffectiveDate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> BookFineRateEntity.create(BOOK_ID, new BigDecimal("0.50"), null))
                .withMessageContaining("effectiveDate");
    }
}
