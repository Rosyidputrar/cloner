package io.github.rosyidputrar.kembar.feature.manager.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.rosyidputrar.kembar.feature.manager.HomeScreen
import kotlinx.serialization.Serializable

/** Beranda, the top-level start screen once onboarding and the disclaimer are done. */
@Serializable
data object HomeDestination

fun NavGraphBuilder.homeScreen() {
    composable<HomeDestination> {
        HomeScreen()
    }
}
