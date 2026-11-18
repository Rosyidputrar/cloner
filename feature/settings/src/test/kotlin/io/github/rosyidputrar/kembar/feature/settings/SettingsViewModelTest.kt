package io.github.rosyidputrar.kembar.feature.settings

import app.cash.turbine.test
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.testing.FakeUserPreferencesRepository
import io.github.rosyidputrar.kembar.core.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val repository = FakeUserPreferencesRepository(
        UserPreferences.Default.copy(themeMode = ThemeMode.System, dynamicColor = true),
    )

    @Test
    fun `state reflects stored preferences`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.uiState.test {
            assertEquals(SettingsUiState.Content(ThemeMode.System, dynamicColor = true), awaitItem())
        }
    }

    @Test
    fun `selecting a theme persists it and updates state`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(SettingsUiEvent.ThemeModeSelected(ThemeMode.Dark))

            assertEquals(SettingsUiState.Content(ThemeMode.Dark, dynamicColor = true), awaitItem())
            assertEquals(ThemeMode.Dark, repository.current.themeMode)
        }
    }

    @Test
    fun `turning off dynamic color persists it`() = runTest {
        val viewModel = SettingsViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(SettingsUiEvent.DynamicColorChanged(enabled = false))

            assertEquals(SettingsUiState.Content(ThemeMode.System, dynamicColor = false), awaitItem())
        }
    }
}
