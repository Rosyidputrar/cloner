package io.github.rosyidputrar.kembar.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.rosyidputrar.kembar.StartDestination
import io.github.rosyidputrar.kembar.disclaimer.disclaimerScreen
import io.github.rosyidputrar.kembar.feature.manager.navigation.HomeDestination
import io.github.rosyidputrar.kembar.feature.manager.navigation.homeScreen
import io.github.rosyidputrar.kembar.feature.settings.navigation.AboutDestination
import io.github.rosyidputrar.kembar.feature.settings.navigation.aboutScreen
import io.github.rosyidputrar.kembar.feature.settings.navigation.settingsScreen
import io.github.rosyidputrar.kembar.navigation.DisclaimerDestination
import io.github.rosyidputrar.kembar.navigation.OnboardingDestination
import io.github.rosyidputrar.kembar.navigation.TopLevelDestination
import io.github.rosyidputrar.kembar.onboarding.onboardingScreen

/**
 * App shell. Shows a bottom bar on phones, a navigation rail on tablets / unfolded
 * foldables, and no navigation at all on first-run and detail screens.
 */
@Composable
fun KembarApp(
    startDestination: StartDestination,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = backStackEntry?.destination.toTopLevelDestination()
    val adaptiveLayout = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val startRoute: Any = remember(startDestination) {
        when (startDestination) {
            StartDestination.Onboarding -> OnboardingDestination
            StartDestination.Disclaimer -> DisclaimerDestination(readOnly = false)
            StartDestination.Home -> HomeDestination
        }
    }

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = if (currentTopLevel != null) adaptiveLayout else NavigationSuiteType.None,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val selected = destination == currentTopLevel
                item(
                    selected = selected,
                    onClick = { navController.navigateToTopLevel(destination) },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            // The label below already names the destination for TalkBack.
                            contentDescription = null,
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                )
            }
        },
    ) {
        NavHost(navController = navController, startDestination = startRoute) {
            onboardingScreen(
                onFinished = {
                    navController.navigate(DisclaimerDestination(readOnly = false)) {
                        popUpTo<OnboardingDestination> { inclusive = true }
                    }
                },
            )
            disclaimerScreen(
                onAccepted = {
                    navController.navigate(HomeDestination) {
                        popUpTo<DisclaimerDestination> { inclusive = true }
                    }
                },
                onBackClick = navController::popBackStack,
            )
            homeScreen()
            settingsScreen(onAboutClick = { navController.navigate(AboutDestination) })
            aboutScreen(
                onBackClick = navController::popBackStack,
                onReadDisclaimerClick = { navController.navigate(DisclaimerDestination(readOnly = true)) },
            )
        }
    }
}

private fun NavDestination?.toTopLevelDestination(): TopLevelDestination? {
    val destination = this ?: return null
    return TopLevelDestination.entries.firstOrNull { topLevel ->
        destination.hierarchy.any { it.hasRoute(topLevel.routeClass) }
    }
}

/**
 * Beranda is always the root of the top-level back stack, so switching tabs pops back to it
 * (saving the tab's state) instead of piling up screens.
 */
private fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo<HomeDestination> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
