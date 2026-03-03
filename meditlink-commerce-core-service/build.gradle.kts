plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":meditlink-commerce-common"))

    // HTTP(REST) + Validation + Actuator
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // Hexagonal에서 persistence adapter를 구현하기 위한 JPA
    implementation(libs.spring.boot.starter.data.jpa)

    // 학습 포인트: Spring Modulith
    // - 모듈 경계 문서화 + 구조 검증 테스트를 위해 사용
    implementation(libs.spring.modulith.starter.core)

    // core-service는 gRPC server endpoint를 직접 제공
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    // 스키마 버전 관리 (DDL)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.starter.test)
}
