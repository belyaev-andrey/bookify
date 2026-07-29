package org.jetbrains.conf.bookify.payments;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API for the Payments module.
 * Use this class to interact with the payments module from other modules.
 */
@Component
public class PaymentsAPI {

    private final PaymentService paymentService;

    PaymentsAPI(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Configure or update a fine rate for a book.
     *
     * @param bookId             the ID of the book
     * @param pricePerDayOverdue the fine charged per day of overdue
     * @param effectiveDate      the date from which this rate is effective
     */
    public void setFineRate(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        paymentService.setFineRate(bookId, pricePerDayOverdue, effectiveDate);
    }

    /**
     * Returns the fine rate applicable for a book on a given date.
     *
     * @param bookId the ID of the book
     * @param asOf   the date to evaluate the rate for
     * @return the price per overdue day, or empty if no rate is configured
     */
    public Optional<BigDecimal> getApplicableRate(UUID bookId, LocalDate asOf) {
        return paymentService.getApplicableRate(bookId, asOf)
                .map(BookFineRateEntity::getPricePerDayOverdue);
    }
}
