package io.github.rosyidputrar.kembar.core.common.app

/**
 * Build metadata of the Kembar app itself. Implemented in `:app` from `BuildConfig`,
 * so feature modules can show it without depending on the application module.
 */
interface AppBuildInfo {
    val versionName: String
    val versionCode: Int
    val isDebug: Boolean
}
