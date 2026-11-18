package io.github.rosyidputrar.kembar.onboarding

import app.cash.turbine.test
import io.github.rosyidputrar.kembar.core.testing.FakeUserPreferencesRepository
import io.github.rosyidputrar.kembar.core.testing.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class OnboardingViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `finishing marks onboarding completed and navigates on`() = runTest {
        val repository = FakeUserPreferencesRepository()
        val viewModel = OnboardingViewModel(repository)

        viewModel.effects.test {
            viewModel.onEvent(OnboardingUiEvent.Finish)

            assertEquals(OnboardingUiEffect.NavigateNext, awaitItem())
            assertTrue(repository.current.onboardingCompleted)
        }
    }
}
