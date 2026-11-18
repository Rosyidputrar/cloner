package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rosyidputrar.kembar.core.ui.R
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

/**
 * Pill label "Kembar n". Shown next to every clone name so a clone is never mistaken for the
 * original app. Uses fixed (non-dynamic) colors so it looks the same on every wallpaper.
 */
@Composable
fun CloneBadge(
    slot: Int,
    modifier: Modifier = Modifier,
) {
    val colors = KembarTheme.extendedColors
    val description = stringResource(R.string.core_ui_clone_badge_description, slot)
    Text(
        text = stringResource(R.string.core_ui_clone_badge, slot),
        style = MaterialTheme.typography.labelMedium,
        color = colors.onCloneBadge,
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .background(color = colors.cloneBadge, shape = CircleShape)
            .padding(horizontal = KembarTheme.spacing.sm, vertical = KembarTheme.spacing.xxs),
    )
}

/**
 * App icon with a numbered clone marker in the bottom-end corner.
 * [appLabel] is used for the TalkBack description ("WhatsApp, clone ke-2").
 */
@Composable
fun BadgedAppIcon(
    icon: Painter,
    appLabel: String,
    slot: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val colors = KembarTheme.extendedColors
    val description = stringResource(R.string.core_ui_clone_icon_description, appLabel, slot)
    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = description },
    ) {
        Image(painter = icon, contentDescription = null, modifier = Modifier.size(size))
        Text(
            text = slot.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onCloneBadge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .sizeIn(minWidth = size * BADGE_RATIO, minHeight = size * BADGE_RATIO)
                .border(width = 1.5.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                .background(color = colors.cloneBadge, shape = CircleShape)
                .padding(horizontal = 2.dp),
        )
    }
}

private const val BADGE_RATIO = 0.42f

@PreviewLightDark
@Composable
private fun CloneBadgePreview() {
    KembarTheme(dynamicColor = false) {
        Surface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgedAppIcon(
                    icon = rememberVectorPainter(Icons.Filled.Android),
                    appLabel = "Contoh",
                    slot = 2,
                    modifier = Modifier.padding(KembarTheme.spacing.sm),
                )
                CloneBadge(slot = 2)
            }
        }
    }
}
