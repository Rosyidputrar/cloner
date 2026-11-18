package io.github.rosyidputrar.kembar.feature.settings.about

import io.github.rosyidputrar.kembar.core.common.app.AppBuildInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AboutViewModelTest {

    @Test
    fun `shows the app version name`() {
        val buildInfo = mockk<AppBuildInfo> {
            every { versionName } returns "1.2.3"
        }

        val viewModel = AboutViewModel(buildInfo)

        assertEquals(AboutUiState(versionName = "1.2.3"), viewModel.uiState.value)
    }
}
