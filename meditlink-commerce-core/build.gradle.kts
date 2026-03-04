plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jib)
}

dependencies {
    implementation(project(":meditlink-commerce-common-proto"))

    // HTTP(REST) + Validation + Actuator
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Hexagonal에서 persistence adapter를 구현하기 위한 JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // 학습 포인트: Spring Modulith
    // - 모듈 경계 문서화 + 구조 검증 테스트를 위해 사용
    implementation(libs.spring.modulith.starter.core)

    // core는 gRPC server endpoint를 직접 제공
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    // 스키마 버전 관리 (DDL)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.starter.test)
}

jib {
    from {
        image = "eclipse-temurin:25-jre"
    }
    to {
        image = "meditlink/commerce-core:local"
    }
    container {
        ports = listOf("8081", "9090")
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf("-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Seoul")
    }
}
