rootProject.name = "SpellArcade"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Core modules
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:game-engine")
include(":core:design-system")

// Feature modules
include(":feature:home")
include(":feature:games")
include(":feature:vocabulary")
include(":feature:parent-portal")
include(":feature:settings")
include(":feature:player-profile")

// Application modules
include(":composeApp")
// iosApp is an Xcode project; no Gradle include needed
