package io.github.rosyidputrar.kembar.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** Type-safe access to `gradle/libs.versions.toml` from convention plugins. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.versionInt(alias: String): Int = findVersion(alias).get().requiredVersion.toInt()

fun VersionCatalog.versionString(alias: String): String = findVersion(alias).get().requiredVersion

fun VersionCatalog.library(alias: String) = findLibrary(alias).get()
