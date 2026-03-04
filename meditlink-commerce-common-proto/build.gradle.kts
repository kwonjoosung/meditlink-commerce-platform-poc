plugins {
    `java-library`
    alias(libs.plugins.protobuf)
}

dependencies {
    // common 모듈은 gRPC contract + generated stub을 배포하는 역할
    // core/integration이 모두 같은 메시지/서비스 타입을 공유하게 만듦
    api(libs.protobuf.java)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api(libs.javax.annotation.api)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.32.0"
    }

    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.76.0"
        }
    }

    generateProtoTasks {
        all().configureEach {
            plugins {
                create("grpc")
            }
        }
    }
}

sourceSets {
    main {
        java {
            // proto로부터 생성된 Java 코드를 컴파일 소스셋에 포함
            srcDirs(
                "build/generated/source/proto/main/java",
                "build/generated/source/proto/main/grpc"
            )
        }
    }
}
