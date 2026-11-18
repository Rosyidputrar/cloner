package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ControlPointDuplicate
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.rosyidputrar.kembar.core.ui.R
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

private val StateIconSize = 64.dp

/** Full-area loading state. The message is announced by TalkBack. */
@Composable
fun KembarLoadingState(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.core_ui_loading),
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KembarTheme.spacing.lg),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            CircularProgressIndicator()
            Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
    }
}

/** Nothing to show yet. Explains why and, if possible, offers the action that fixes it. */
@Composable
fun KembarEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ControlPointDuplicate,
    action: (@Composable () -> Unit)? = null,
) {
    StateLayout(
        icon = icon,
        iconTint = MaterialTheme.colorScheme.primary,
        title = title,
        body = body,
        modifier = modifier,
        action = action,
    )
}

/**
 * Something went wrong. [cause] says what happened in plain words, and [nextStep] says what
 * the user can do about it. Both are required so no error leaves the user stuck.
 */
@Composable
fun KembarErrorState(
    title: String,
    cause: String,
    nextStep: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    StateLayout(
        icon = Icons.Outlined.ErrorOutline,
        iconTint = MaterialTheme.colorScheme.error,
        title = title,
        body = "$cause\n\n$nextStep",
        modifier = modifier,
        action = onRetry?.let { retry ->
            { KembarPrimaryButton(text = stringResource(R.string.core_ui_retry), onClick = retry) }
        },
    )
}

@Composable
private fun StateLayout(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
    modifier: Modifier,
    action: (@Composable () -> Unit)?,
) {
    val spacing = KembarTheme.spacing
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.xl, vertical = spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.md),
            modifier = Modifier.readableWidth(),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(StateIconSize),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Box(modifier = Modifier.padding(top = spacing.sm)) { action() }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    KembarTheme(dynamicColor = false) {
        Surface {
            KembarEmptyState(
                title = "Belum ada clone",
                body = "Clone yang kamu buat bakal muncul di sini.",
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ErrorStatePreview() {
    KembarTheme(dynamicColor = false) {
        Surface {
            KembarErrorState(
                title = "Clone gagal dibuat",
                cause = "Ruang penyimpanan HP kamu kurang.",
                nextStep = "Kosongkan sekitar 300 MB, lalu coba lagi.",
                onRetry = {},
            )
        }
    }
}
