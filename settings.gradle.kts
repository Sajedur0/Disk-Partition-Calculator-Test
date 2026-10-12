pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Lets Gradle download the JDK 17 toolchain pinned in app/build.gradle.kts when the
// machine that runs the build does not have one installed yet.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DiskPartitionCalculator"

include(":app")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    This project requires JDK 17 or newer, but Gradle is running on JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    Point Android Studio at JDK 17 (Settings > Build, Execution, Deployment > Build Tools >
    Gradle > Gradle JDK) or set JAVA_HOME to a JDK 17 installation.
    """.trimIndent()
}
