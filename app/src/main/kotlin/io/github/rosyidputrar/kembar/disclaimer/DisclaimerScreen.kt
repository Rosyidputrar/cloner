package io.github.rosyidputrar.kembar.disclaimer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.rosyidputrar.kembar.R
import io.github.rosyidputrar.kembar.core.ui.component.KembarPrimaryButton
import io.github.rosyidputrar.kembar.core.ui.component.ObserveEffects
import io.github.rosyidputrar.kembar.core.ui.component.readableWidth
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import io.github.rosyidputrar.kembar.navigation.DisclaimerDestination

fun NavGraphBuilder.disclaimerScreen(
    onAccepted: () -> Unit,
    onBackClick: () -> Unit,
) {
    composable<DisclaimerDestination> { entry ->
        val destination = entry.toRoute<DisclaimerDestination>()
        DisclaimerRoute(readOnly = destination.readOnly, onAccepted = onAccepted, onBackClick = onBackClick)
    }
}

internal enum class DisclaimerPoint(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
) {
    Compatibility(
        icon = Icons.Outlined.ReportProblem,
        titleRes = R.string.disclaimer_point_compat_title,
        bodyRes = R.string.disclaimer_point_compat_body,
    ),
    GoogleServices(
        icon = Icons.Outlined.NotificationsOff,
        titleRes = R.string.disclaimer_point_google_title,
        bodyRes = R.string.disclaimer_point_google_body,
    ),
    AccountRisk(
        icon = Icons.Outlined.AccountCircle,
        titleRes = R.string.disclaimer_point_account_title,
        bodyRes = R.string.disclaimer_point_account_body,
    ),
    TermsOfService(
        icon = Icons.Outlined.Gavel,
        titleRes = R.string.disclaimer_point_tos_title,
        bodyRes = R.string.disclaimer_point_tos_body,
    ),
    NoDataBackup(
        icon = Icons.Outlined.Backup,
        titleRes = R.string.disclaimer_point_data_title,
        bodyRes = R.string.disclaimer_point_data_body,
    ),
    PlaysFair(
        icon = Icons.Outlined.VerifiedUser,
        titleRes = R.string.disclaimer_point_honest_title,
        bodyRes = R.string.disclaimer_point_honest_body,
    ),
}

@Composable
private fun DisclaimerRoute(
    readOnly: Boolean,
    onAccepted: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: DisclaimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.disclaimer_save_failed)

    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            DisclaimerUiEffect.Accepted -> onAccepted()
            DisclaimerUiEffect.SaveFailed -> snackbarHostState.showSnackbar(saveFailedMessage)
        }
    }

    DisclaimerScreen(
        uiState = uiState,
        readOnly = readOnly,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DisclaimerScreen(
    uiState: DisclaimerUiState,
    readOnly: Boolean,
    onEvent: (DisclaimerUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val spacing = KembarTheme.spacing
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (readOnly) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.disclaimer_back),
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!readOnly) {
                AcceptBar(uiState = uiState, onEvent = onEvent)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.disclaimer_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.disclaimer_intro),
                    style = MaterialTheme.typography.bodyLarge,
                )
                DisclaimerPoint.entries.forEach { point -> PointItem(point) }
            }
        }
    }
}

@Composable
private fun PointItem(point: DisclaimerPoint) {
    Row(horizontalArrangement = Arrangement.spacedBy(KembarTheme.spacing.md)) {
        Icon(
            imageVector = point.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(KembarTheme.spacing.xs)) {
            Text(
                text = stringResource(point.titleRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(point.bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AcceptBar(uiState: DisclaimerUiState, onEvent: (DisclaimerUiEvent) -> Unit) {
    val spacing = KembarTheme.spacing
    Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .readableWidth()
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = spacing.minTouchTarget)
                        .toggleable(
                            value = uiState.understood,
                            role = Role.Checkbox,
                            onValueChange = { onEvent(DisclaimerUiEvent.UnderstoodChanged(it)) },
                        )
                        .testTag(DisclaimerTestTags.UNDERSTOOD),
                ) {
                    Checkbox(checked = uiState.understood, onCheckedChange = null)
                    Text(
                        text = stringResource(R.string.disclaimer_understood),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = spacing.sm),
                    )
                }
                KembarPrimaryButton(
                    text = stringResource(R.string.disclaimer_accept),
                    onClick = { onEvent(DisclaimerUiEvent.Accept) },
                    enabled = uiState.canAccept,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(DisclaimerTestTags.ACCEPT),
                )
            }
        }
    }
}

internal object DisclaimerTestTags {
    const val UNDERSTOOD = "disclaimer_understood"
    const val ACCEPT = "disclaimer_accept"
}

@PreviewLightDark
@Composable
private fun DisclaimerScreenPreview() {
    KembarTheme(dynamicColor = false) {
        DisclaimerScreen(
            uiState = DisclaimerUiState(understood = true),
            readOnly = false,
            onEvent = {},
            onBackClick = {},
        )
    }
}
