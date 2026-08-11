package org.jetbrains.conf.bookify.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Charges a real external payment gateway over HTTP. Active only when
 * {@code bookify.payments.provider=production} (settings file or env var
 * {@code BOOKIFY_PAYMENTS_PROVIDER}), pointed at {@code bookify.payments.production.*}.
 */
@Component
@ConditionalOnProperty(prefix = "bookify.payments", name = "provider", havingValue = "production")
class ProductionPaymentProvider implements PaymentProvider {

    private static final Logger log = LoggerFactory.getLogger(ProductionPaymentProvider.class);

    private final RestClient restClient;

    ProductionPaymentProvider(RestClient.Builder restClientBuilder, PaymentProviderProperties properties) {
        PaymentProviderProperties.Production settings = properties.getProduction();
        this.restClient = restClientBuilder
                .baseUrl(settings.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + settings.getApiKey())
                .build();
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        try {
            ChargeResponse response = restClient.post()
                    .uri("/v1/charges")
                    .body(new ChargeRequest(request.memberId(), request.bookId(), request.amount(), "USD"))
                    .retrieve()
                    .body(ChargeResponse.class);

            if (response == null) {
                return new PaymentResult(false, null, "Empty response from payment gateway");
            }
            return new PaymentResult("succeeded".equalsIgnoreCase(response.status()), response.id(), response.status());
        } catch (RestClientException e) {
            log.error("Payment gateway call failed for member {}", request.memberId(), e);
            return new PaymentResult(false, null, "Payment gateway error: " + e.getMessage());
        }
    }

    private record ChargeRequest(UUID memberId, UUID bookId, BigDecimal amount, String currency) {
    }

    private record ChargeResponse(String id, String status) {
    }
}
