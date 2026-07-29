package org.jetbrains.conf.bookify.payments;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Table("book_fine_rate")
class BookFineRateEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("book_id")
    private UUID bookId;
    @Column("price_per_day_overdue")
    private BigDecimal pricePerDayOverdue;
    @Column("effective_date")
    private LocalDate effectiveDate;

    @Transient
    private boolean isNew = true;

    BookFineRateEntity(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        if (bookId == null) throw new IllegalArgumentException("bookId cannot be null");
        if (pricePerDayOverdue == null || pricePerDayOverdue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("pricePerDayOverdue must be non-negative");
        }
        if (effectiveDate == null) throw new IllegalArgumentException("effectiveDate cannot be null");
        this.id = UUID.randomUUID();
        this.bookId = bookId;
        this.pricePerDayOverdue = pricePerDayOverdue;
        this.effectiveDate = effectiveDate;
    }

    @PersistenceCreator
    BookFineRateEntity(UUID id, UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        this.id = id;
        this.bookId = bookId;
        this.pricePerDayOverdue = pricePerDayOverdue;
        this.effectiveDate = effectiveDate;
        this.isNew = false;
    }

    static BookFineRateEntity create(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        return new BookFineRateEntity(bookId, pricePerDayOverdue, effectiveDate);
    }

    @Override
    public boolean isNew() {
        return isNew;
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
