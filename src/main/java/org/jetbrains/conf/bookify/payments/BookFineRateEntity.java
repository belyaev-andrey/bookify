package org.jetbrains.conf.bookify.payments;

import jakarta.persistence.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "book_fine_rate")
@NullUnmarked
class BookFineRateEntity implements Persistable<@NonNull UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @Column(name = "price_per_day_overdue", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDayOverdue;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    protected BookFineRateEntity() {
    }

    BookFineRateEntity(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        if (bookId == null) throw new IllegalArgumentException("bookId cannot be null");
        if (pricePerDayOverdue == null || pricePerDayOverdue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("pricePerDayOverdue must be non-negative");
        }
        if (effectiveDate == null) throw new IllegalArgumentException("effectiveDate cannot be null");
        this.bookId = bookId;
        this.pricePerDayOverdue = pricePerDayOverdue;
        this.effectiveDate = effectiveDate;
    }

    static BookFineRateEntity create(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        return new BookFineRateEntity(bookId, pricePerDayOverdue, effectiveDate);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public BigDecimal getPricePerDayOverdue() {
        return pricePerDayOverdue;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }
}
