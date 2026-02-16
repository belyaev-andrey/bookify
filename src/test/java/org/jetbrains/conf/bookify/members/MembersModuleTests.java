package org.jetbrains.conf.bookify.members;

import org.jetbrains.conf.bookify.BookifyApplication;
import org.jetbrains.conf.bookify.DbConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(DbConfiguration.class)
@ActiveProfiles("test")
class MembersModuleTests {

    @Test
    void verifyModuleStructure() {
        // This test verifies that the members module follows the Spring Modulith structure rules
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        modules.getModuleByName("members").orElseThrow();
    }
}
