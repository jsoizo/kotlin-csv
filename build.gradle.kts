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


//publishing settings
//https://docs.gradle.org/current/userguide/publishing_maven.html
val sourcesJar = task<Jar>("sourcesJar") {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>("myLibrary") {
            artifactId = "kotlin-csv"
            from(components["java"])
            artifact(sourcesJar)
        }
    }
    repositories {
        maven {
            url = uri("file://Users/kenta/workspace/oss/kotlin-csv/out")
        }
    }
}
