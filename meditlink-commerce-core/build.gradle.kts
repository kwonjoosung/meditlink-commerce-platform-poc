plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jib)
}

// 기본값은 Apple Silicon 로컬 실행을 위한 arm64
// Linux 배포 이미지는 -PjibTargetArch=amd64 로 오버라이드
val jibTargetArch = providers.gradleProperty("jibTargetArch").orElse("arm64").get()

dependencies {
    implementation(project(":meditlink-commerce-common-proto"))

    // HTTP(REST) + Validation + Actuator
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)

    // JPA (persistence 레이어)
    implementation(libs.spring.boot.starter.data.jpa)

    // JSONB 매핑 (Hypersistence Utils)
    implementation(libs.hypersistence.utils)

    // Jackson (Rule Engine JSON 역직렬화)
    implementation(libs.jackson.databind)

    // Spring Modulith (모듈 경계 검증)
    implementation(libs.spring.modulith.starter.core)

    // Kafka (비동기 이벤트 발행)
    implementation(libs.spring.kafka)

    // Stripe 동기화
    implementation(libs.stripe.java)

    // gRPC server (기존 유지)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)

    // 스키마 버전 관리 (Liquibase)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.archunit.junit5)
}

jib {
    // Java 25 클래스 파일 파싱 이슈 회피를 위해 packaged 모드 사용
    // (Boot JAR를 그대로 컨테이너에 적재)
    containerizingMode = "packaged"

    from {
        image = "eclipse-temurin:25-jre"
        platforms {
            platform {
                os = "linux"
                architecture = jibTargetArch
            }
        }
    }
    to {
        image = "meditlink/commerce-core:local"
    }
    container {
        mainClass = "com.meditlink.poc.commerce.core.CoreServiceApplication"
        ports = listOf("8081", "9090")
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf("-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Seoul")
    }
}
