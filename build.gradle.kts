plugins {
    // Apply only the plugins that need to be on the classpath for the whole project.
    // Individual modules apply the plugins they need.
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Common configuration that applies to all subprojects can be added here if needed.
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
