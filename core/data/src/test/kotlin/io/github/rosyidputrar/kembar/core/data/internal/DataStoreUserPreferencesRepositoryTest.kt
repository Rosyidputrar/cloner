package io.github.rosyidputrar.kembar.core.data.internal

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreUserPreferencesRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private fun TestScope.createRepository(): DataStoreUserPreferencesRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempDir, "test.preferences_pb") },
        )
        return DataStoreUserPreferencesRepository(dataStore)
    }

    @Test
    fun `fresh install emits defaults`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()

        assertEquals(UserPreferences.Default, repository.userPreferences.first())
        assertTrue(repository.userPreferences.first().needsDisclaimer)
    }

    @Test
    fun `completing onboarding is persisted`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()

        repository.setOnboardingCompleted()

        assertTrue(repository.userPreferences.first().onboardingCompleted)
    }

    @Test
    fun `accepting the current disclaimer clears needsDisclaimer`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()

        repository.acceptDisclaimer()

        val prefs = repository.userPreferences.first()
        assertEquals(UserPreferences.CURRENT_DISCLAIMER_VERSION, prefs.acceptedDisclaimerVersion)
        assertFalse(prefs.needsDisclaimer)
    }

    @Test
    fun `accepted disclaimer version never goes backwards`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()

        repository.acceptDisclaimer(version = 5)
        repository.acceptDisclaimer(version = 2)

        assertEquals(5, repository.userPreferences.first().acceptedDisclaimerVersion)
    }

    @Test
    fun `theme mode and dynamic color are persisted`() = runTest(UnconfinedTestDispatcher()) {
        val repository = createRepository()

        repository.setThemeMode(ThemeMode.Dark)
        repository.setDynamicColor(false)

        val prefs = repository.userPreferences.first()
        assertEquals(ThemeMode.Dark, prefs.themeMode)
        assertFalse(prefs.dynamicColor)
    }
}
