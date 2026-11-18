package io.github.rosyidputrar.kembar.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Content(
        val themeMode: ThemeMode,
        val dynamicColor: Boolean,
    ) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class ThemeModeSelected(val themeMode: ThemeMode) : SettingsUiEvent

    data class DynamicColorChanged(val enabled: Boolean) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userPreferencesRepository.userPreferences
        .map { prefs -> SettingsUiState.Content(themeMode = prefs.themeMode, dynamicColor = prefs.dynamicColor) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsUiState.Loading,
        )

    fun onEvent(event: SettingsUiEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUiEvent.ThemeModeSelected -> userPreferencesRepository.setThemeMode(event.themeMode)
                is SettingsUiEvent.DynamicColorChanged -> userPreferencesRepository.setDynamicColor(event.enabled)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
