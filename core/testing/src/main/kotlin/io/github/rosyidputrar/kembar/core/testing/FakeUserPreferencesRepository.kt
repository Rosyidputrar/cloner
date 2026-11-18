package io.github.rosyidputrar.kembar.core.testing

import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [UserPreferencesRepository] for ViewModel tests. */
class FakeUserPreferencesRepository(
    initial: UserPreferences = UserPreferences.Default,
) : UserPreferencesRepository {

    private val state = MutableStateFlow(initial)

    val current: UserPreferences
        get() = state.value

    override val userPreferences: Flow<UserPreferences> = state

    override suspend fun setOnboardingCompleted() {
        state.update { it.copy(onboardingCompleted = true) }
    }

    override suspend fun acceptDisclaimer(version: Int) {
        state.update { it.copy(acceptedDisclaimerVersion = maxOf(it.acceptedDisclaimerVersion, version)) }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.update { it.copy(themeMode = themeMode) }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        state.update { it.copy(dynamicColor = enabled) }
    }
}
