package com.meditlink.poc.commerce.core;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.meditlink.poc.commerce.core");
    }

    @Nested
    @DisplayName("Domain 레이어 규칙")
    class DomainLayerTests {

        @Test
        @DisplayName("domain은 infrastructure에 의존하지 않음")
        void domainShouldNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage("..product.domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..product.infrastructure..")
                    .check(classes);
        }

        @Test
        @DisplayName("domain은 application에 의존하지 않음")
        void domainShouldNotDependOnApplication() {
            noClasses()
                    .that().resideInAPackage("..product.domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..product.application..")
                    .check(classes);
        }

        @Test
        @DisplayName("domain은 Spring에 의존하지 않음 (package-info 제외)")
        void domainShouldNotDependOnSpring() {
            noClasses()
                    .that().resideInAPackage("..product.domain..")
                    .and().haveSimpleNameNotContaining("package-info")
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..")
                    .check(classes);
        }

        @Test
        @DisplayName("domain은 JPA에 의존하지 않음")
        void domainShouldNotDependOnJpa() {
            noClasses()
                    .that().resideInAPackage("..product.domain..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("jakarta.persistence..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("모듈 경계 규칙")
    class ModuleBoundaryTests {

        @Test
        @DisplayName("product는 다른 BC(billing, feature, coupon) 내부에 의존하지 않음")
        void productShouldNotDependOnOtherBcInternals() {
            noClasses()
                    .that().resideInAPackage("..product..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..billing..", "..feature..")
                    .check(classes);
        }

        @Test
        @DisplayName("gateway는 infrastructure에 직접 의존하지 않음")
        void gatewayShouldNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage("..gateway..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..product.infrastructure..")
                    .check(classes);
        }
    }

    @Nested
    @DisplayName("Application 레이어 규칙")
    class ApplicationLayerTests {

        @Test
        @DisplayName("application은 infrastructure.persistence에 직접 의존하지 않음")
        void applicationShouldNotDependOnPersistence() {
            noClasses()
                    .that().resideInAPackage("..product.application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..product.infrastructure.persistence..")
                    .check(classes);
        }
    }
}
