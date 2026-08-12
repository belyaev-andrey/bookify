package org.jetbrains.conf.bookify.payments;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class PaymentsModuleTests {

    @Autowired
    private PaymentProvider paymentProvider;

    @Autowired
    private PaymentProviderProperties paymentProviderProperties;

    @Autowired
    private PaymentsAPI paymentsAPI;

    @Test
    void verifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        assertThat(modules.getModuleByName("payments")).isPresent();
    }

    @Test
    void paymentsApiIsExposedAsPublicApi() {
        assertThat(paymentsAPI).isNotNull();
    }

    @Test
    void usesMockPaymentProviderInTestProfile() {
        assertThat(paymentProvider).isInstanceOf(MockPaymentProvider.class);
    }

    @Test
    void bindsProviderPropertyFromConfiguration() {
        assertThat(paymentProviderProperties.getProvider()).isEqualTo(PaymentProviderProperties.Provider.MOCK);
    }
}
