package org.jetbrains.conf.bookify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
@Import(DbConfiguration.class)
class ModulithTests {

    @Test
    void verifyModulithStructure() {
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        modules.verify();
    }

    @Test
    void createModulithDocumentation() {
        ApplicationModules modules = ApplicationModules.of(BookifyApplication.class);
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
