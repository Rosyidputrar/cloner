package io.github.rosyidputrar.kembar.core.data.repository

import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    /** Emits the current preferences and every later change. Never fails: read errors emit defaults. */
    val userPreferences: Flow<UserPreferences>

    suspend fun setOnboardingCompleted()

    suspend fun acceptDisclaimer(version: Int = UserPreferences.CURRENT_DISCLAIMER_VERSION)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setDynamicColor(enabled: Boolean)
}
