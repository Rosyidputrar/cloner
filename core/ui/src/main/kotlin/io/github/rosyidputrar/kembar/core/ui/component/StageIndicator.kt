package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.rosyidputrar.kembar.core.ui.R
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

enum class StageStatus {
    Pending,
    Running,
    Done,
    Failed,
}

@Immutable
data class StageItem(
    val label: String,
    val status: StageStatus,
)

private val StageIconSize = 24.dp

/**
 * Vertical list of pipeline stages (e.g. Analisis → Salin → Modifikasi → Tanda tangan → Pasang).
 * Each row is one TalkBack item: "Tahap 2 dari 5, Salin: Sedang berjalan".
 */
@Composable
fun StageIndicator(
    stages: List<StageItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(KembarTheme.spacing.xs),
    ) {
        stages.forEachIndexed { index, stage ->
            StageRow(stage = stage, position = index + 1, total = stages.size)
        }
    }
}

@Composable
private fun StageRow(stage: StageItem, position: Int, total: Int) {
    val statusLabel = stringResource(
        when (stage.status) {
            StageStatus.Pending -> R.string.core_ui_stage_pending
            StageStatus.Running -> R.string.core_ui_stage_running
            StageStatus.Done -> R.string.core_ui_stage_done
            StageStatus.Failed -> R.string.core_ui_stage_failed
        },
    )
    val description = stringResource(R.string.core_ui_stage_semantics, position, total, stage.label, statusLabel)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KembarTheme.spacing.md),
        modifier = Modifier
            .heightIn(min = KembarTheme.spacing.minTouchTarget)
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Box(modifier = Modifier.size(StageIconSize), contentAlignment = Alignment.Center) {
            StageIcon(stage.status)
        }
        Column {
            Text(
                text = stage.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (stage.status == StageStatus.Running) FontWeight.SemiBold else FontWeight.Normal,
                color = if (stage.status == StageStatus.Pending) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            if (stage.status == StageStatus.Running || stage.status == StageStatus.Failed) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stage.status == StageStatus.Failed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun StageIcon(status: StageStatus) {
    when (status) {
        StageStatus.Pending -> Icon(
            imageVector = Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
        )
        StageStatus.Running -> CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.5.dp,
        )
        StageStatus.Done -> Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = KembarTheme.extendedColors.success,
        )
        StageStatus.Failed -> Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@PreviewLightDark
@Composable
private fun StageIndicatorPreview() {
    KembarTheme(dynamicColor = false) {
        Surface {
            StageIndicator(
                stages = listOf(
                    StageItem("Analisis", StageStatus.Done),
                    StageItem("Salin", StageStatus.Done),
                    StageItem("Modifikasi", StageStatus.Running),
                    StageItem("Tanda tangan", StageStatus.Pending),
                    StageItem("Pasang", StageStatus.Pending),
                ),
            )
        }
    }
}
