package org.jetbrains.conf.bookify;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

@Import(DbConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class BookifyApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(BookifyApplicationTests.class);

    @Test
    void contextLoads() {
        log.info("Testing Context Loaded");
    }

}
