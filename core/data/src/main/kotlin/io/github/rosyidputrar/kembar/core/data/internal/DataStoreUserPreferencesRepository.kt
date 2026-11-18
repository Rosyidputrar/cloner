package io.github.rosyidputrar.kembar.core.data.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class DataStoreUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            // A corrupted or unreadable file must not crash the app; fall back to defaults.
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs -> prefs.toUserPreferences() }
        .distinctUntilChanged()

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.OnboardingCompleted] = true }
    }

    override suspend fun acceptDisclaimer(version: Int) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.AcceptedDisclaimerVersion] ?: 0
            prefs[Keys.AcceptedDisclaimerVersion] = maxOf(current, version)
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = themeMode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DynamicColor] = enabled }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences.Default
        return UserPreferences(
            onboardingCompleted = this[Keys.OnboardingCompleted] ?: defaults.onboardingCompleted,
            acceptedDisclaimerVersion = this[Keys.AcceptedDisclaimerVersion] ?: defaults.acceptedDisclaimerVersion,
            themeMode = this[Keys.ThemeMode]
                ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                ?: defaults.themeMode,
            dynamicColor = this[Keys.DynamicColor] ?: defaults.dynamicColor,
        )
    }

    private object Keys {
        val OnboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val AcceptedDisclaimerVersion = intPreferencesKey("disclaimer_accepted_version")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
    }
}
