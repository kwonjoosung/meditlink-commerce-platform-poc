package com.meditlink.poc.commerce.core;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    void verifiesModuleStructure() {
        ApplicationModules.of(CoreServiceApplication.class).verify();
    }
}
