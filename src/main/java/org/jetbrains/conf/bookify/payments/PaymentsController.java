package org.jetbrains.conf.bookify.payments;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
class PaymentsController {

    private final PaymentsAPI paymentsAPI;

    PaymentsController(PaymentsAPI paymentsAPI) {
        this.paymentsAPI = paymentsAPI;
    }

    @GetMapping("/fine-rate")
    ResponseEntity<BigDecimal> getApplicableRate(
            @RequestParam UUID bookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return paymentsAPI.getApplicableRate(bookId, date)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
