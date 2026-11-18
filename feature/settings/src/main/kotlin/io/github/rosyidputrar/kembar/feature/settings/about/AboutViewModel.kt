package io.github.rosyidputrar.kembar.feature.settings.about

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rosyidputrar.kembar.core.common.app.AppBuildInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AboutUiState(
    val versionName: String,
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    appBuildInfo: AppBuildInfo,
) : ViewModel() {
    val uiState: StateFlow<AboutUiState> =
        MutableStateFlow(AboutUiState(versionName = appBuildInfo.versionName)).asStateFlow()
}
