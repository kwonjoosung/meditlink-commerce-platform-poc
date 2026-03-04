// 멀티 모듈 학습 포인트:
// - pluginManagement: 플러그인 해석용 저장소
// - dependencyResolutionManagement: 프로젝트 의존성 해석용 저장소
// - include(...)로 단일 리포 안에서 서브 프로젝트 경계를 명확히 표현
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "meditlink-commerce-platform-poc"

// 공통 계약(common-proto) / 핵심 도메인(core) / 클라이언트 채널(client) 경계를 모듈로 분리
include(
    "meditlink-commerce-common-proto",
    "meditlink-commerce-core",
    "meditlink-commerce-client"
)
