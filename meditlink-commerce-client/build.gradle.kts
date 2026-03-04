plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jib)
}

dependencies {
    implementation(project(":meditlink-commerce-common-proto"))

    // 외부 API 채널 계층
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // 상태 저장(요청 로그/오케스트레이션 상태)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)

    // core 호출용 gRPC client stub
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    testImplementation(libs.spring.boot.starter.test)
}

jib {
    from {
        image = "eclipse-temurin:25-jre"
    }
    to {
        image = "meditlink/commerce-client:local"
    }
    container {
        ports = listOf("8080")
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf("-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Seoul")
    }
}
