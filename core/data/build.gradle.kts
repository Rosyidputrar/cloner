plugins {
    id("kembar.android.library")
    id("kembar.hilt")
}

android {
    namespace = "io.github.rosyidputrar.kembar.core.data"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.datastore.preferences)
}
