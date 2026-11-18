package io.github.rosyidputrar.kembar.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.rosyidputrar.kembar.feature.settings.SettingsRoute
import io.github.rosyidputrar.kembar.feature.settings.about.AboutRoute
import kotlinx.serialization.Serializable

/** Pengaturan, a top-level destination. */
@Serializable
data object SettingsDestination

/** Tentang Kembar, opened from Pengaturan. */
@Serializable
data object AboutDestination

fun NavGraphBuilder.settingsScreen(onAboutClick: () -> Unit) {
    composable<SettingsDestination> {
        SettingsRoute(onAboutClick = onAboutClick)
    }
}

fun NavGraphBuilder.aboutScreen(
    onBackClick: () -> Unit,
    onReadDisclaimerClick: () -> Unit,
) {
    composable<AboutDestination> {
        AboutRoute(onBackClick = onBackClick, onReadDisclaimerClick = onReadDisclaimerClick)
    }
}
