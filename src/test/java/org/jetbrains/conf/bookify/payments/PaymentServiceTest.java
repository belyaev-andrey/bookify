package org.jetbrains.conf.bookify.payments;

import org.jetbrains.conf.bookify.events.AssignFineEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final BookFineRateRepository fineRateRepository = mock(BookFineRateRepository.class);
    private final PaymentProvider paymentProvider = mock(PaymentProvider.class);
    private final PaymentService paymentService = new PaymentService(fineRateRepository, paymentProvider);

    @Test
    void handleAssignFine_chargesRatePerDayTimesOverdueDays() {
        UUID bookId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        BookFineRateEntity rate = BookFineRateEntity.create(bookId, new BigDecimal("2.50"), LocalDate.now().minusDays(1));
        when(fineRateRepository.findApplicableRate(eq(bookId), any())).thenReturn(Optional.of(rate));
        when(paymentProvider.charge(any())).thenReturn(new PaymentResult(true, "tx-1", "ok"));

        paymentService.handleAssignFine(new AssignFineEvent(bookId, memberId, 3));

        ArgumentCaptor<PaymentRequest> captor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(paymentProvider).charge(captor.capture());
        assertThat(captor.getValue().memberId()).isEqualTo(memberId);
        assertThat(captor.getValue().bookId()).isEqualTo(bookId);
        assertThat(captor.getValue().amount()).isEqualByComparingTo("7.50");
    }

    @Test
    void handleAssignFine_skipsChargeWhenNoRateConfigured() {
        when(fineRateRepository.findApplicableRate(any(), any())).thenReturn(Optional.empty());

        paymentService.handleAssignFine(new AssignFineEvent(UUID.randomUUID(), UUID.randomUUID(), 2));

        verifyNoInteractions(paymentProvider);
    }
}
