import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    `maven-publish`

    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.rpc)
}

kotlin {
    jvm()
    js()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs()
    sourceSets.commonMain.dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.test)

        implementation(libs.kotlinx.rpc.core)
        implementation(libs.kotlinx.rpc.krpc.core)
        implementation(libs.kotlinx.rpc.krpc.client)
        implementation(libs.kotlinx.rpc.krpc.server)

        implementation(libs.kotlinx.rpc.krpc.serialization.json)
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.rpc.krpc.ktor.client)
    }
}
