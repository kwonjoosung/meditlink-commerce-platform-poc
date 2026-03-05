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
        image = "meditlink/commerce-client:local"
    }
    container {
        mainClass = "com.meditlink.poc.commerce.integration.IntegrationLayerApplication"
        ports = listOf("8080")
        creationTime = "USE_CURRENT_TIMESTAMP"
        jvmFlags = listOf("-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Seoul")
    }
}
