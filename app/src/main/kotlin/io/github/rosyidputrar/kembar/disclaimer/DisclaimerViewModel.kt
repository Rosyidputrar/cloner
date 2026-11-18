package io.github.rosyidputrar.kembar.disclaimer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rosyidputrar.kembar.core.data.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DisclaimerUiState(
    /** The "Aku sudah baca dan paham" checkbox. Survives process death. */
    val understood: Boolean = false,
    val saving: Boolean = false,
) {
    val canAccept: Boolean
        get() = understood && !saving
}

sealed interface DisclaimerUiEvent {
    data class UnderstoodChanged(val checked: Boolean) : DisclaimerUiEvent

    data object Accept : DisclaimerUiEvent
}

sealed interface DisclaimerUiEffect {
    data object Accepted : DisclaimerUiEffect

    data object SaveFailed : DisclaimerUiEffect
}

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val saving = MutableStateFlow(false)
    private val effectChannel = Channel<DisclaimerUiEffect>(Channel.BUFFERED)
    val effects: Flow<DisclaimerUiEffect> = effectChannel.receiveAsFlow()

    val uiState: StateFlow<DisclaimerUiState> = combine(
        savedStateHandle.getStateFlow(KEY_UNDERSTOOD, false),
        saving,
    ) { understood, isSaving ->
        DisclaimerUiState(understood = understood, saving = isSaving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DisclaimerUiState(understood = savedStateHandle[KEY_UNDERSTOOD] ?: false),
    )

    fun onEvent(event: DisclaimerUiEvent) {
        when (event) {
            is DisclaimerUiEvent.UnderstoodChanged -> savedStateHandle[KEY_UNDERSTOOD] = event.checked
            DisclaimerUiEvent.Accept -> accept()
        }
    }

    // The cause reaches the user as SaveFailed. It is not logged, because storage errors
    // can contain file paths.
    @Suppress("SwallowedException")
    private fun accept() {
        val understood = savedStateHandle[KEY_UNDERSTOOD] ?: false
        if (!understood || saving.value) return
        viewModelScope.launch {
            saving.value = true
            try {
                userPreferencesRepository.acceptDisclaimer()
                effectChannel.send(DisclaimerUiEffect.Accepted)
            } catch (error: IOException) {
                effectChannel.send(DisclaimerUiEffect.SaveFailed)
            } finally {
                saving.value = false
            }
        }
    }

    private companion object {
        const val KEY_UNDERSTOOD = "understood"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
