package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

private val MinButtonHeight = 48.dp

/** Main action of a screen. At most one per screen. */
@Composable
fun KembarPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = MinButtonHeight),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Secondary action next to a primary one. */
@Composable
fun KembarSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = MinButtonHeight),
    ) {
        ButtonContent(text = text, icon = icon)
    }
}

/** Low-emphasis action, e.g. "Lewati". */
@Composable
fun KembarTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = MinButtonHeight),
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            // Decorative: the button text already describes the action.
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text = text, textAlign = TextAlign.Center)
    }
}

@PreviewLightDark
@Composable
private fun ButtonsPreview() {
    KembarTheme(dynamicColor = false) {
        Row {
            KembarPrimaryButton(text = "Lanjut", onClick = {})
            KembarSecondaryButton(text = "Batal", onClick = {})
            KembarTextButton(text = "Lewati", onClick = {})
        }
    }
}
