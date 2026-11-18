package io.github.rosyidputrar.kembar.core.data.model

/** How the app chooses between light and dark colors. */
enum class ThemeMode {
    System,
    Light,
    Dark,
}

/** Settings the user controls, plus first-run progress. */
data class UserPreferences(
    val onboardingCompleted: Boolean,
    val acceptedDisclaimerVersion: Int,
    val themeMode: ThemeMode,
    val dynamicColor: Boolean,
) {
    /** True when the user still has to accept the current disclaimer text. */
    val needsDisclaimer: Boolean
        get() = acceptedDisclaimerVersion < CURRENT_DISCLAIMER_VERSION

    companion object {
        /**
         * Bump this whenever the disclaimer text changes in a way users must re-read.
         * Users who accepted an older version are asked again.
         */
        const val CURRENT_DISCLAIMER_VERSION: Int = 1

        val Default = UserPreferences(
            onboardingCompleted = false,
            acceptedDisclaimerVersion = 0,
            themeMode = ThemeMode.System,
            dynamicColor = true,
        )
    }
}
