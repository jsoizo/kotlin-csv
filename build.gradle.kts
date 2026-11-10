import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.jmh) apply false
}

group = "com.jsoizo"
version = "2.0.0"
val projectName = "kotlin-csv"
val isSnapshot = version.toString().endsWith("-SNAPSHOT")

kotlin {
    jvmToolchain(21)

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs {
            testTask {
                useMocha {
                    // Chunk-boundary tests in WriterIoPropertyTest take over 1 s on Node; the 2 s default flakes on CI.
                    timeout = "10s"
                }
            }
        }
    }

    macosArm64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.io.core)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.property)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        jvmMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit5)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Kotest 6.x JVM artifacts are built with Java 11 bytecode, while the library
// artifact itself still targets Java 8 for consumer compatibility.
tasks.named<KotlinJvmCompile>("compileTestKotlinJvm") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dokka {
    moduleName.set(projectName)
    dokkaSourceSets.named("commonMain") {
        includes.from("Module.md")
    }
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(rootDir)
            // Pin released docs to their tag so line links survive later edits on main
            remoteUrl("https://github.com/jsoizo/kotlin-csv/tree/${if (isSnapshot) "main" else version}")
            remoteLineSuffix.set("#L")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    val hasSigningKey = project.hasProperty("signing.keyId") || project.hasProperty("signingInMemoryKey")
    if (!isSnapshot && hasSigningKey) {
        signAllPublications()
    }

    coordinates(group.toString(), projectName, version.toString())

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
            sourcesJar = SourcesJar.Sources(),
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
                url.set("${repoHttpUrl}/blob/main/LICENSE")
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
