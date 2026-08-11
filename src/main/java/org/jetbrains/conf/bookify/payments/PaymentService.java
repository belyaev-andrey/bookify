package org.jetbrains.conf.bookify.payments;

import org.jetbrains.conf.bookify.events.AssignFineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final BookFineRateRepository fineRateRepository;
    private final PaymentProvider paymentProvider;

    PaymentService(BookFineRateRepository fineRateRepository, PaymentProvider paymentProvider) {
        this.fineRateRepository = fineRateRepository;
        this.paymentProvider = paymentProvider;
    }

    @ApplicationModuleListener
    void handleAssignFine(AssignFineEvent event) {
        log.info("Processing fine for member {}, book {} — {} day(s) overdue",
                event.memberId(), event.bookId(), event.overdueInDays());

        fineRateRepository.findApplicableRate(event.bookId(), LocalDate.now())
                .ifPresentOrElse(
                        rate -> chargeFine(event, rate),
                        () -> log.warn("No fine rate configured for book {}; skipping charge", event.bookId()));
    }

    private void chargeFine(AssignFineEvent event, BookFineRateEntity rate) {
        BigDecimal amount = rate.getPricePerDayOverdue().multiply(BigDecimal.valueOf(event.overdueInDays()));
        PaymentResult result = paymentProvider.charge(new PaymentRequest(event.memberId(), event.bookId(), amount));
        if (result.successful()) {
            log.info("Charged {} to member {} (transaction {})", amount, event.memberId(), result.transactionId());
        } else {
            log.warn("Failed to charge {} to member {}: {}", amount, event.memberId(), result.message());
        }
    }

    @Transactional
    public BookFineRateEntity setFineRate(UUID bookId, BigDecimal pricePerDayOverdue, LocalDate effectiveDate) {
        return fineRateRepository.save(
                BookFineRateEntity.create(bookId, pricePerDayOverdue, effectiveDate));
    }

    @Transactional(readOnly = true)
    public Optional<BookFineRateEntity> getApplicableRate(UUID bookId, LocalDate asOf) {
        return fineRateRepository.findApplicableRate(bookId, asOf);
    }
}
