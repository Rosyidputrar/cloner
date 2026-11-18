import com.android.build.api.dsl.ApplicationExtension
import io.github.rosyidputrar.kembar.buildlogic.configureKotlinAndroid
import io.github.rosyidputrar.kembar.buildlogic.configureUnitTests
import io.github.rosyidputrar.kembar.buildlogic.libs
import io.github.rosyidputrar.kembar.buildlogic.versionInt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.versionInt("targetSdk")
            }
            configureUnitTests()
        }
    }
}
