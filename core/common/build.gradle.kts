plugins {
    id("kembar.android.library")
    id("kembar.hilt")
}

android {
    namespace = "io.github.rosyidputrar.kembar.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.android)
}
