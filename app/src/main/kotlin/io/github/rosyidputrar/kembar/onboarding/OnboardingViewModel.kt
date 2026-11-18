package io.github.rosyidputrar.kembar.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface OnboardingUiEvent {
    /** "Mulai" on the last slide or "Lewati" on any slide. */
    data object Finish : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data object NavigateNext : OnboardingUiEffect
}

/** The current slide lives in the pager's own saveable state; this only records completion. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val effectChannel = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingUiEffect> = effectChannel.receiveAsFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.Finish -> viewModelScope.launch {
                markCompleted()
                effectChannel.send(OnboardingUiEffect.NavigateNext)
            }
        }
    }

    /** If saving fails the slides simply show again next launch; never block the user here. */
    @Suppress("SwallowedException")
    private suspend fun markCompleted() {
        try {
            userPreferencesRepository.setOnboardingCompleted()
        } catch (error: IOException) {
            // Intentionally ignored, see KDoc.
        }
    }
}
