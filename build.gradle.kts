plugins {
    kotlin("multiplatform") version "1.3.50"
    id("org.jetbrains.dokka").version("0.9.18")
    jacoco
    `maven-publish`
    signing
}

group = "com.github.doyaaaaaken"
version = "0.6.1"

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")
    }
}

repositories {
    jcenter()
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
//            compileKotlinTask // get the Kotlin task 'compileKotlinJvm'
//            output // get the main compilation output
        }

//        compilations["test"].runtimeDependencyFiles // get the test runtime classpath
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            }
        }
        jvm().compilations["test"].defaultSourceSet {
            dependencies {
                implementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
            }
        }
    }
}

val jvmTest by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

////publishing settings
////https://docs.gradle.org/current/userguide/publishing_maven.html
//val sourcesJar = task<Jar>("sourcesJar") {
//    from(sourceSets.main.get().allSource)
//    archiveClassifier.set("sources")
//}
//val dokkaJar = task<Jar>("dokkaJar") {
//    group = JavaBasePlugin.DOCUMENTATION_GROUP
//    archiveClassifier.set("javadoc")
//}
//
//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            artifactId = "kotlin-csv"
//            from(components["java"])
//            artifacts {
//                artifact(sourcesJar)
//                artifact(dokkaJar)
//            }
//            pom {
//                name.set("kotlin-csv")
//                description.set("Kotlin CSV Reader/Writer")
//                url.set("https://github.com/doyaaaaaken/kotlin-csv")
//
//                organization {
//                    name.set("com.github.doyaaaaaken")
//                    url.set("https://github.com/doyaaaaaken")
//                }
//                licenses {
//                    license {
//                        name.set("Apache License 2.0")
//                        url.set("https://github.com/doyaaaaaken/kotlin-csv/blob/master/LICENSE")
//                    }
//                }
//                scm {
//                    url.set("https://github.com/doyaaaaaken/kotlin-csv")
//                    connection.set("scm:git:git://github.com/doyaaaaaken/kotlin-csv.git")
//                    developerConnection.set("https://github.com/doyaaaaaken/kotlin-csv")
//                }
//                developers {
//                    developer {
//                        name.set("doyaaaaaken")
//                    }
//                }
//            }
//        }
//    }
//    repositories {
//        maven {
//            credentials {
//                val nexusUsername: String? by project
//                val nexusPassword: String? by project
//                username = nexusUsername
//                password = nexusPassword
//            }
//
//            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
//            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
//            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
//        }
//    }
//}
//
//signing {
//    sign(publishing.publications["mavenJava"])
//}
//
//tasks.withType<JacocoReport> {
//    reports {
//        xml.isEnabled = true
//        xml.destination = File("$buildDir/reports/jacoco/report.xml")
//        html.isEnabled = false
//    }
//}
