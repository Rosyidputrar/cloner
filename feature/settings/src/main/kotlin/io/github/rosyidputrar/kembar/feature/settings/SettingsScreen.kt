package io.github.rosyidputrar.kembar.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.rosyidputrar.kembar.core.data.model.ThemeMode
import io.github.rosyidputrar.kembar.core.ui.component.KembarLoadingState
import io.github.rosyidputrar.kembar.core.ui.component.SettingsNavigationItem
import io.github.rosyidputrar.kembar.core.ui.component.SettingsRadioItem
import io.github.rosyidputrar.kembar.core.ui.component.SettingsSectionHeader
import io.github.rosyidputrar.kembar.core.ui.component.SettingsSwitchItem
import io.github.rosyidputrar.kembar.core.ui.component.readableWidth
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import io.github.rosyidputrar.kembar.core.ui.theme.isDynamicColorSupported

@Composable
internal fun SettingsRoute(
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        uiState = uiState,
        dynamicColorSupported = isDynamicColorSupported(),
        onEvent = viewModel::onEvent,
        onAboutClick = onAboutClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    dynamicColorSupported: Boolean,
    onEvent: (SettingsUiEvent) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_settings_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            SettingsUiState.Loading -> KembarLoadingState(modifier = Modifier.padding(innerPadding))
            is SettingsUiState.Content -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                SettingsContent(
                    state = uiState,
                    dynamicColorSupported = dynamicColorSupported,
                    onEvent = onEvent,
                    onAboutClick = onAboutClick,
                    modifier = Modifier.readableWidth(),
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Content,
    dynamicColorSupported: Boolean,
    onEvent: (SettingsUiEvent) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = KembarTheme.spacing.xl)) {
        SettingsSectionHeader(title = stringResource(R.string.feature_settings_section_appearance))
        Column(modifier = Modifier.selectableGroup()) {
            ThemeOption.entries.forEach { option ->
                SettingsRadioItem(
                    title = stringResource(option.titleRes),
                    summary = option.summaryRes?.let { stringResource(it) },
                    selected = state.themeMode == option.mode,
                    onClick = { onEvent(SettingsUiEvent.ThemeModeSelected(option.mode)) },
                )
            }
        }
        if (dynamicColorSupported) {
            SettingsSwitchItem(
                title = stringResource(R.string.feature_settings_dynamic_color),
                summary = stringResource(R.string.feature_settings_dynamic_color_summary),
                icon = Icons.Outlined.Palette,
                checked = state.dynamicColor,
                onCheckedChange = { onEvent(SettingsUiEvent.DynamicColorChanged(it)) },
            )
        }

        SettingsSectionHeader(title = stringResource(R.string.feature_settings_section_info))
        SettingsNavigationItem(
            title = stringResource(R.string.feature_settings_about),
            summary = stringResource(R.string.feature_settings_about_summary),
            icon = Icons.Outlined.Info,
            onClick = onAboutClick,
        )
    }
}

private enum class ThemeOption(val mode: ThemeMode, val titleRes: Int, val summaryRes: Int?) {
    System(ThemeMode.System, R.string.feature_settings_theme_system, R.string.feature_settings_theme_system_summary),
    Light(ThemeMode.Light, R.string.feature_settings_theme_light, null),
    Dark(ThemeMode.Dark, R.string.feature_settings_theme_dark, null),
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    KembarTheme(dynamicColor = false) {
        SettingsScreen(
            uiState = SettingsUiState.Content(themeMode = ThemeMode.System, dynamicColor = true),
            dynamicColorSupported = true,
            onEvent = {},
            onAboutClick = {},
        )
    }
}
