package org.jetbrains.conf.bookify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    /**
     * Default rule set: LIBRARIAN can manage the catalogue and members.
     * Active unless the "strict-security" profile is on.
     */
    @Bean
    @Profile("!strict-security")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/members",
                                "/api/books"
                        ).hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/books/**"
                        ).hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/members/**",
                                "/api/books"
                        ).hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/members/active")
                        .hasRole("LIBRARIAN")
                        .anyRequest().anonymous()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * Stricter rule set for the "strict-security" profile: the same endpoints now require ADMIN
     * instead of LIBRARIAN, and anonymous access is no longer allowed by default. Reading
     * SecurityConfig alone doesn't tell you which of these two chains is actually enforced at
     * runtime - that depends on which profile is active, which is exactly what the security
     * inlay resolves by reading the live SecurityFilterChain instead of the source.
     * <p>
     * Activate with {@code -Dspring-boot.run.profiles=dev,strict-security} (or add
     * {@code strict-security} to {@code spring.profiles.active}).
     */
    @Bean
    @Profile("strict-security")
    SecurityFilterChain strictSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/members",
                                "/api/books"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/books/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/members/**",
                                "/api/books"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/members/active")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    UserDetailsManager userDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
