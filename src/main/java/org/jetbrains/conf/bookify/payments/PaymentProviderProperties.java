package org.jetbrains.conf.bookify.payments;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bookify.payments")
class PaymentProviderProperties {

    private Provider provider = Provider.MOCK;
    private Production production = new Production();

    Provider getProvider() {
        return provider;
    }

    void setProvider(Provider provider) {
        this.provider = provider;
    }

    Production getProduction() {
        return production;
    }

    void setProduction(Production production) {
        this.production = production;
    }

    enum Provider {
        MOCK, PRODUCTION
    }

    static class Production {

        private String baseUrl = "https://api.payment-gateway.example.com";
        private String apiKey = "";

        String getBaseUrl() {
            return baseUrl;
        }

        void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        String getApiKey() {
            return apiKey;
        }

        void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
