package io.github.rosyidputrar.kembar

import app.cash.turbine.test
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.testing.FakeUserPreferencesRepository
import io.github.rosyidputrar.kembar.core.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class MainViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("startCases")
    fun `start destination follows first-run progress`(
        prefs: UserPreferences,
        expected: StartDestination,
    ) = runTest {
        val viewModel = MainViewModel(FakeUserPreferencesRepository(prefs))

        val state = viewModel.uiState.value as MainUiState.Ready
        assertEquals(expected, state.startDestination)
    }

    @Test
    fun `start destination stays fixed while theme changes flow through`() = runTest {
        val repository = FakeUserPreferencesRepository(UserPreferences.Default)
        val viewModel = MainViewModel(repository)

        viewModel.uiState.test {
            assertEquals(StartDestination.Onboarding, (awaitItem() as MainUiState.Ready).startDestination)

            // Completing onboarding mid-session must not move the graph's start destination.
            repository.setOnboardingCompleted()
            repository.setThemeMode(ThemeMode.Dark)
            val latest = awaitItem() as MainUiState.Ready
            assertEquals(StartDestination.Onboarding, latest.startDestination)
            assertEquals(ThemeMode.Dark, latest.themeMode)
        }
    }

    companion object {
        @JvmStatic
        fun startCases(): List<Arguments> = listOf(
            Arguments.of(UserPreferences.Default, StartDestination.Onboarding),
            Arguments.of(
                UserPreferences.Default.copy(onboardingCompleted = true),
                StartDestination.Disclaimer,
            ),
            Arguments.of(
                UserPreferences.Default.copy(
                    onboardingCompleted = true,
                    acceptedDisclaimerVersion = UserPreferences.CURRENT_DISCLAIMER_VERSION,
                ),
                StartDestination.Home,
            ),
        )
    }
}
