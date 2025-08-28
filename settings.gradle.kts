rootProject.name = "rkrpc"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":rkrpc-core")
include(":rkrpc-ktor-server")
include(":rkrpc-ktor-client")
