import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test

plugins {
    // 버전 카탈로그(libs.versions.toml)를 통해 플러그인 버전 중앙 관리
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.protobuf) apply false
}

allprojects {
    group = "com.meditlink.poc.commerce"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    pluginManager.apply("java")

    // 학습 포인트: Java Toolchain
    // - 로컬 JAVA_HOME과 무관하게 빌드 JDK 버전을 강제할 수 있음
    // - 팀 전체가 동일 버전(여기서는 Java 25)로 빌드 재현 가능
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }
}
