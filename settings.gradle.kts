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

include(
    "meditlink-commerce-common",
    "meditlink-commerce-core-service",
    "meditlink-commerce-integration-layer"
)
