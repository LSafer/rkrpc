plugins {
    `maven-publish`

    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

group = "net.lsafer"
version = "1.0.0"

tasks.wrapper {
    gradleVersion = "8.14"
}

repositories {
    mavenCentral()
}

subprojects {
    group = "net.lsafer.rkrpc"

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
