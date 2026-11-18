package io.github.rosyidputrar.kembar.disclaimer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisclaimerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun acceptButtonEnablesOnlyAfterTickingCheckbox() {
        var accepted = false
        composeRule.setContent {
            var state by remember { mutableStateOf(DisclaimerUiState()) }
            KembarTheme(dynamicColor = false) {
                DisclaimerScreen(
                    uiState = state,
                    readOnly = false,
                    onEvent = { event ->
                        when (event) {
                            is DisclaimerUiEvent.UnderstoodChanged -> state = state.copy(understood = event.checked)
                            DisclaimerUiEvent.Accept -> accepted = true
                        }
                    },
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(DisclaimerTestTags.ACCEPT).assertIsNotEnabled()
        composeRule.onNodeWithTag(DisclaimerTestTags.UNDERSTOOD).performClick()
        composeRule.onNodeWithTag(DisclaimerTestTags.ACCEPT).assertIsEnabled().performClick()

        assertTrue(accepted)
    }
}
