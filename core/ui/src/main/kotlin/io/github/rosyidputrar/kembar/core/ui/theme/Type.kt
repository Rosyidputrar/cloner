package io.github.rosyidputrar.kembar.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Material 3 type scale on the system font. Sizes are in sp, so they follow the user's font
 * scale (tested up to 200%); layouts must never fix text heights.
 */

private fun style(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

internal val KembarTypography = Typography(
    displayLarge = style(FontWeight.Normal, size = 57, lineHeight = 64, letterSpacing = -0.25),
    displayMedium = style(FontWeight.Normal, size = 45, lineHeight = 52),
    displaySmall = style(FontWeight.Normal, size = 36, lineHeight = 44),
    headlineLarge = style(FontWeight.SemiBold, size = 32, lineHeight = 40),
    headlineMedium = style(FontWeight.SemiBold, size = 28, lineHeight = 36),
    headlineSmall = style(FontWeight.SemiBold, size = 24, lineHeight = 32),
    titleLarge = style(FontWeight.Medium, size = 22, lineHeight = 28),
    titleMedium = style(FontWeight.Medium, size = 16, lineHeight = 24, letterSpacing = 0.15),
    titleSmall = style(FontWeight.Medium, size = 14, lineHeight = 20, letterSpacing = 0.1),
    bodyLarge = style(FontWeight.Normal, size = 16, lineHeight = 24, letterSpacing = 0.5),
    bodyMedium = style(FontWeight.Normal, size = 14, lineHeight = 20, letterSpacing = 0.25),
    bodySmall = style(FontWeight.Normal, size = 12, lineHeight = 16, letterSpacing = 0.4),
    labelLarge = style(FontWeight.Medium, size = 14, lineHeight = 20, letterSpacing = 0.1),
    labelMedium = style(FontWeight.Medium, size = 12, lineHeight = 16, letterSpacing = 0.5),
    labelSmall = style(FontWeight.Medium, size = 11, lineHeight = 16, letterSpacing = 0.5),
)
