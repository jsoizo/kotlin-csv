import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish)
}

group = "com.jsoizo"
version = "2.0.0-SNAPSHOT"
val projectName = "kotlin-csv"

buildscript {
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js {
        browser {
        }
        nodejs {
        }
    }
    sourceSets {
        commonMain {}
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(libs.bundles.kotest)
            }
        }
        js().compilations["main"].defaultSourceSet {
            dependencies {
            }
        }
        js().compilations["test"].defaultSourceSet {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()

    val isSnapshot = version.toString().endsWith("-SNAPSHOT")
    val hasSigningKey = project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")
    if (!isSnapshot && hasSigningKey) {
        signAllPublications()
    }

    coordinates(group.toString(), projectName, version.toString())

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
            androidVariantsToPublish = listOf("debug", "release"),
        )
    )

    val repo = "github.com/jsoizo/${projectName}"
    val repoHttpUrl = "https://${repo}"
    val repoGitUrl = "git://${repo}.git"

    pom {
        name = projectName
        description = "Pure Kotlin CSV reader and writer"
        inceptionYear = "2019"
        url = repoHttpUrl
        organization {
            name.set("com.jsoizo")
            url.set("https://github.com/jsoizo")
        }
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("${repoHttpUrl}/blob/master/LICENSE")
            }
        }
        scm {
            url.set(repoHttpUrl)
            connection.set("scm:git:${repoGitUrl}")
            developerConnection.set(repoHttpUrl)
        }
        developers {
            developer {
                name.set("jsoizo")
            }
        }
    }
}