pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kembar"

include(":app")
include(":core:common")
include(":core:data")
include(":core:testing")
include(":core:ui")
include(":feature:manager")
include(":feature:settings")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
