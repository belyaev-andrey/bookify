package org.jetbrains.conf.bookify.payments;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaymentProviderProperties.class)
class PaymentProviderConfiguration {
}
