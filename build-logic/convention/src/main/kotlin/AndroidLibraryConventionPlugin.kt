import com.android.build.api.dsl.LibraryExtension
import io.github.rosyidputrar.kembar.buildlogic.configureKotlinAndroid
import io.github.rosyidputrar.kembar.buildlogic.configureUnitTests
import io.github.rosyidputrar.kembar.buildlogic.libs
import io.github.rosyidputrar.kembar.buildlogic.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                testOptions.targetSdk = libs.versionInt("targetSdk")
                lint.targetSdk = libs.versionInt("targetSdk")
            }
            configureUnitTests()
        }
    }
}
