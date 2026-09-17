package org.jetbrains.conf.bookify.books;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Marks the book catalogue as ready during startup.
 *
 * <p>Other beans in this module need this to have run, but hold no reference to it, so they order
 * themselves after it with {@code @DependsOn} instead of injecting it.</p>
 */
class BookCatalogWarmup {

    private static final Logger log = LoggerFactory.getLogger(BookCatalogWarmup.class);

    @PostConstruct
    void warmUp() {
        log.info("Book catalogue is ready");
    }
}

@Configuration
class BookCatalogWarmupConfiguration {

    /**
     * Registers the warm-up under its primary name plus the {@code catalogWarmup} alias, so
     * {@code @DependsOn} can refer to it by either spelling.
     */
    @Bean(name = {"bookCatalogWarmup", "catalogWarmup"})
    BookCatalogWarmup bookCatalogWarmup() {
        return new BookCatalogWarmup();
    }
}
