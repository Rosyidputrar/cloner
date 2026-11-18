package io.github.rosyidputrar.kembar.feature.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.rosyidputrar.kembar.core.ui.component.KembarSecondaryButton
import io.github.rosyidputrar.kembar.core.ui.component.readableWidth
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import io.github.rosyidputrar.kembar.feature.settings.R

@Composable
internal fun AboutRoute(
    onBackClick: () -> Unit,
    onReadDisclaimerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AboutScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onReadDisclaimerClick = onReadDisclaimerClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    uiState: AboutUiState,
    onBackClick: () -> Unit,
    onReadDisclaimerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_settings_about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_settings_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val spacing = KembarTheme.spacing
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
                    .padding(horizontal = spacing.screenHorizontal, vertical = spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.feature_settings_about_version, uiState.versionName),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.feature_settings_about_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
                AboutSection(
                    title = stringResource(R.string.feature_settings_about_privacy_title),
                    body = stringResource(R.string.feature_settings_about_privacy_body),
                )
                AboutSection(
                    title = stringResource(R.string.feature_settings_about_affiliation_title),
                    body = stringResource(R.string.feature_settings_about_affiliation_body),
                )
                KembarSecondaryButton(
                    text = stringResource(R.string.feature_settings_about_read_disclaimer),
                    onClick = onReadDisclaimerClick,
                )
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(KembarTheme.spacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    KembarTheme(dynamicColor = false) {
        AboutScreen(uiState = AboutUiState(versionName = "0.1.0"), onBackClick = {}, onReadDisclaimerClick = {})
    }
}
