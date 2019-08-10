plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.41")
    `maven-publish`
}

group = "org.doyaaaaaken.kotlincsv"
version = "0.1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

publishing {
    publications {
        create<MavenPublication>("myLibrary") {
            artifactId = "kotlin-csv"
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("file://Users/kenta/workspace/oss/kotlin-csv")
        }
    }
}
