package io.github.rosyidputrar.kembar.buildlogic

import com.android.build.api.dsl.CommonExtension
import de.mannodermaus.gradle.plugins.junit5.dsl.AndroidJUnitPlatformExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Shared Android + Kotlin settings for application and library modules. */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = libs.versionInt("compileSdk")

        defaultConfig {
            minSdk = libs.versionInt("minSdk")
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        lint {
            abortOnError = true
            checkReleaseBuilds = true
            // Indonesian is the default locale; see tools:locale in strings.xml.
            disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            allWarningsAsErrors.set(false)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
            )
        }
    }
}

/** JUnit 5 (via android-junit5), MockK, Turbine and coroutine test utilities for local unit tests. */
internal fun Project.configureUnitTests() {
    pluginManager.apply("de.mannodermaus.android-junit5")
    extensions.configure<AndroidJUnitPlatformExtension> {
        // Instrumented (on-device) tests stay on JUnit 4, which the Compose test rule needs.
        instrumentationTests {
            enabled.set(false)
        }
    }
    dependencies {
        add("testImplementation", libs.library("junit5-api"))
        add("testImplementation", libs.library("junit5-params"))
        add("testRuntimeOnly", libs.library("junit5-engine"))
        add("testImplementation", libs.library("mockk"))
        add("testImplementation", libs.library("turbine"))
        add("testImplementation", libs.library("kotlinx-coroutines-test"))
    }
}
