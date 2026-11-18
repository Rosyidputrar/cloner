package io.github.rosyidputrar.kembar.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors Material 3 doesn't define. They stay fixed under dynamic color, so the clone
 * badge and status colors keep the same meaning on every wallpaper.
 */
@Immutable
data class ExtendedColors(
    /** Background of the "Kembar n" badge drawn on every clone icon. */
    val cloneBadge: Color,
    val onCloneBadge: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    cloneBadge = Color(0xFF8B3A86),
    onCloneBadge = Color(0xFFFFFFFF),
    success = Color(0xFF2E6C2F),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFB0F2A7),
    onSuccessContainer = Color(0xFF002203),
    warning = Color(0xFF7A5900),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFFDEA2),
    onWarningContainer = Color(0xFF261900),
)

internal val DarkExtendedColors = ExtendedColors(
    cloneBadge = Color(0xFFFFAAF3),
    onCloneBadge = Color(0xFF560C55),
    success = Color(0xFF95D58D),
    onSuccess = Color(0xFF003A06),
    successContainer = Color(0xFF13531A),
    onSuccessContainer = Color(0xFFB0F2A7),
    warning = Color(0xFFF5BF48),
    onWarning = Color(0xFF402D00),
    warningContainer = Color(0xFF5C4200),
    onWarningContainer = Color(0xFFFFDEA2),
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
