package io.github.rosyidputrar.kembar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Where the navigation graph starts. Decided once per process from stored progress. */
enum class StartDestination {
    Onboarding,
    Disclaimer,
    Home,
}

sealed interface MainUiState {
    /** Preferences not read yet; the splash screen stays visible. */
    data object Loading : MainUiState

    data class Ready(
        val startDestination: StartDestination,
        val themeMode: ThemeMode,
        val dynamicColor: Boolean,
    ) : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // The start destination is fixed after the first read: changing it later would rebuild
    // the navigation graph while the user is on it. Theme changes still flow through.
    private val startDestination = MutableStateFlow<StartDestination?>(null)

    val uiState: StateFlow<MainUiState> = combine(
        startDestination.filterNotNull(),
        userPreferencesRepository.userPreferences,
    ) { start, prefs ->
        MainUiState.Ready(startDestination = start, themeMode = prefs.themeMode, dynamicColor = prefs.dynamicColor)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainUiState.Loading,
    )

    init {
        viewModelScope.launch {
            startDestination.value = userPreferencesRepository.userPreferences.first().toStartDestination()
        }
    }

    private fun UserPreferences.toStartDestination(): StartDestination = when {
        !onboardingCompleted -> StartDestination.Onboarding
        needsDisclaimer -> StartDestination.Disclaimer
        else -> StartDestination.Home
    }
}
