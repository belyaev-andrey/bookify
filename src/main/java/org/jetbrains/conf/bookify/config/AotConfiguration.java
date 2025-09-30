package org.jetbrains.conf.bookify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.MappingJdbcConverter;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

@Profile("aot")
@Configuration
public class AotConfiguration {

    @Bean
    JdbcConverter jdbcConverter(RelationalMappingContext context) {
        return new MappingJdbcConverter(context, (identifier, path) -> null);
    }

    @Bean
    JdbcDialect dialect() {
        return JdbcPostgresDialect.INSTANCE;
    }
}
