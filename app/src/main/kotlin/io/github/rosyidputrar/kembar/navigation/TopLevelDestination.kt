package io.github.rosyidputrar.kembar.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.rosyidputrar.kembar.R
import io.github.rosyidputrar.kembar.feature.manager.navigation.HomeDestination
import io.github.rosyidputrar.kembar.feature.settings.navigation.SettingsDestination
import kotlin.reflect.KClass

/** Destinations shown in the bottom bar / navigation rail. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val labelRes: Int,
) {
    Home(
        route = HomeDestination,
        routeClass = HomeDestination::class,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        labelRes = R.string.nav_home,
    ),
    Settings(
        route = SettingsDestination,
        routeClass = SettingsDestination::class,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        labelRes = R.string.nav_settings,
    ),
}
