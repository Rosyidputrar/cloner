package io.github.rosyidputrar.kembar.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Material 3 motion tokens: durations in milliseconds and the standard easing curves. */
object KembarMotion {
    const val DURATION_SHORT: Int = 150
    const val DURATION_MEDIUM: Int = 300
    const val DURATION_LONG: Int = 500

    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EasingEmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
}
