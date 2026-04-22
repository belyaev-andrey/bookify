/*
 * Test
 */

/*
 * Test
 */

package org.jetbrains.conf.bookify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;

@Configuration
@Profile("aot")
public class AotConfiguration {
    @Bean
    JpaDialect dialect() {
        return new HibernateJpaDialect();
    }
}
