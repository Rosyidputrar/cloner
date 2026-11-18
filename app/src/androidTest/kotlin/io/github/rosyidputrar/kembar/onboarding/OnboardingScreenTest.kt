package io.github.rosyidputrar.kembar.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.rosyidputrar.kembar.R
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun walkingThroughAllSlidesFinishesOnce() {
        var finished = 0
        composeRule.setContent {
            KembarTheme(dynamicColor = false) { OnboardingScreen(onFinish = { finished++ }) }
        }

        repeat(OnboardingSlide.entries.size - 1) {
            composeRule.onNodeWithTag(OnboardingTestTags.PRIMARY).performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithText(context.getString(R.string.onboarding_4_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.onboarding_start)).performClick()

        assertEquals(1, finished)
    }

    @Test
    fun skipFinishesImmediately() {
        var finished = 0
        composeRule.setContent {
            KembarTheme(dynamicColor = false) { OnboardingScreen(onFinish = { finished++ }) }
        }

        composeRule.onNodeWithTag(OnboardingTestTags.SKIP).performClick()

        assertEquals(1, finished)
    }
}
