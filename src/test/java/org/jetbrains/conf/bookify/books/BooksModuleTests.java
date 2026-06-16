package org.jetbrains.conf.bookify.books;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class BooksModuleTests {

    @Test
    void verifyModuleStructure() {
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        var module = modules.getModuleByName("books").orElseThrow();
        assertNotNull(module);
    }

}
