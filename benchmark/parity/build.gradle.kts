plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    testImplementation(project(":"))
    testImplementation(project(":benchmark:shared"))
    testImplementation(libs.kotlincsv.v1.jvm)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test.junit5)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
