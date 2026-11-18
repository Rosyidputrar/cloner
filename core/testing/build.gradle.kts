plugins {
    id("kembar.android.library")
}

android {
    namespace = "io.github.rosyidputrar.kembar.core.testing"
}

dependencies {
    api(projects.core.common)
    api(projects.core.data)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit5.api)
}
