rootProject.name = "kotlin-csv"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":benchmark:shared", ":benchmark:v1", ":benchmark:v2", ":benchmark:parity")
