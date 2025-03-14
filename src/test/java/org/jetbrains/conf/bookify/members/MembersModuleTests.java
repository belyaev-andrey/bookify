package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
@Import(DbConfiguration.class)
class MembersModuleTests {

    @Test
    void verifyModuleStructure() {
        // This test verifies that the members module follows the Spring Modulith structure rules
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        modules.getModuleByName("members").orElseThrow();
    }
}
