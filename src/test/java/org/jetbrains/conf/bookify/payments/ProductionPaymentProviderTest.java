package org.jetbrains.conf.bookify.payments;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProductionPaymentProviderTest {

    private PaymentProviderProperties propertiesFor(String baseUrl) {
        PaymentProviderProperties properties = new PaymentProviderProperties();
        properties.getProduction().setBaseUrl(baseUrl);
        properties.getProduction().setApiKey("test-api-key");
        return properties;
    }

    @Test
    void charge_returnsSuccessfulResultWhenGatewayApprovesCharge() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ProductionPaymentProvider provider =
                new ProductionPaymentProvider(builder, propertiesFor("https://gateway.test"));

        server.expect(requestTo("https://gateway.test/v1/charges"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"ch_123","status":"succeeded"}
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.charge(
                new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00")));

        assertThat(result.successful()).isTrue();
        assertThat(result.transactionId()).isEqualTo("ch_123");
        server.verify();
    }

    @Test
    void charge_returnsFailedResultWhenGatewayDeclinesCharge() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ProductionPaymentProvider provider =
                new ProductionPaymentProvider(builder, propertiesFor("https://gateway.test"));

        server.expect(requestTo("https://gateway.test/v1/charges"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"id":"ch_456","status":"declined"}
                        """, MediaType.APPLICATION_JSON));

        PaymentResult result = provider.charge(
                new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00")));

        assertThat(result.successful()).isFalse();
        assertThat(result.transactionId()).isEqualTo("ch_456");
        server.verify();
    }

    @Test
    void charge_returnsFailedResultWhenGatewayIsUnreachable() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ProductionPaymentProvider provider =
                new ProductionPaymentProvider(builder, propertiesFor("https://gateway.test"));

        server.expect(requestTo("https://gateway.test/v1/charges"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        PaymentResult result = provider.charge(
                new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00")));

        assertThat(result.successful()).isFalse();
        assertThat(result.transactionId()).isNull();
        server.verify();
    }
}
