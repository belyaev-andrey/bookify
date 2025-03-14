package org.jetbrains.conf.bookify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(DbConfiguration.class)
@SpringBootTest
class BookifyApplicationTests {

    @Test
    void contextLoads() {
    }

}
