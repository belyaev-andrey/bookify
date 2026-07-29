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

    PaymentService(BookFineRateRepository fineRateRepository) {
        this.fineRateRepository = fineRateRepository;
    }

    @ApplicationModuleListener
    void handleAssignFine(AssignFineEvent event) {
        log.info("Processing fine for member {}, book {} — {} day(s) overdue",
                event.memberId(), event.bookId(), event.overdueInDays());

        Optional<BookFineRateEntity> rateOpt =
                fineRateRepository.findApplicableRate(event.bookId(), LocalDate.now());

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
