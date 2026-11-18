package io.github.rosyidputrar.kembar.disclaimer

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.github.rosyidputrar.kembar.core.data.model.UserPreferences
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import io.github.rosyidputrar.kembar.core.testing.FakeUserPreferencesRepository
import io.github.rosyidputrar.kembar.core.testing.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DisclaimerViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `accept is disabled until the user ticks the checkbox`() = runTest {
        val viewModel = DisclaimerViewModel(SavedStateHandle(), FakeUserPreferencesRepository())

        viewModel.uiState.test {
            assertFalse(awaitItem().canAccept)
            viewModel.onEvent(DisclaimerUiEvent.UnderstoodChanged(checked = true))
            assertTrue(awaitItem().canAccept)
        }
    }

    @Test
    fun `accept without ticking the checkbox does nothing`() = runTest {
        val repository = FakeUserPreferencesRepository()
        val viewModel = DisclaimerViewModel(SavedStateHandle(), repository)

        viewModel.onEvent(DisclaimerUiEvent.Accept)

        assertEquals(0, repository.current.acceptedDisclaimerVersion)
    }

    @Test
    fun `accepting stores the current version and emits Accepted`() = runTest {
        val repository = FakeUserPreferencesRepository()
        val viewModel = DisclaimerViewModel(SavedStateHandle(), repository)

        viewModel.effects.test {
            viewModel.onEvent(DisclaimerUiEvent.UnderstoodChanged(checked = true))
            viewModel.onEvent(DisclaimerUiEvent.Accept)

            assertEquals(DisclaimerUiEffect.Accepted, awaitItem())
            assertEquals(UserPreferences.CURRENT_DISCLAIMER_VERSION, repository.current.acceptedDisclaimerVersion)
        }
    }

    @Test
    fun `checkbox state survives process death through SavedStateHandle`() = runTest {
        val handle = SavedStateHandle(mapOf("understood" to true))
        val viewModel = DisclaimerViewModel(handle, FakeUserPreferencesRepository())

        assertTrue(viewModel.uiState.value.understood)
    }

    @Test
    fun `storage failure emits SaveFailed and allows retry`() = runTest {
        val repository = mockk<UserPreferencesRepository> {
            coEvery { userPreferences } returns flowOf(UserPreferences.Default)
            coEvery { acceptDisclaimer(any()) } throws IOException("disk full")
        }
        val viewModel = DisclaimerViewModel(SavedStateHandle(mapOf("understood" to true)), repository)

        viewModel.effects.test {
            viewModel.onEvent(DisclaimerUiEvent.Accept)

            assertEquals(DisclaimerUiEffect.SaveFailed, awaitItem())
            assertTrue(viewModel.uiState.value.canAccept)
            coVerify(exactly = 1) { repository.acceptDisclaimer(any()) }
        }
    }
}
