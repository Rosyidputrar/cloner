package io.github.rosyidputrar.kembar.feature.manager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import io.github.rosyidputrar.kembar.core.ui.component.KembarEmptyState
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme

/**
 * Beranda. In this phase the clone registry doesn't exist yet, so the screen always shows its
 * empty state; the clone list and the "Buat clone" action arrive with the app picker and engine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feature_manager_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        KembarEmptyState(
            title = stringResource(R.string.feature_manager_empty_title),
            body = stringResource(R.string.feature_manager_empty_body),
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun HomeScreenPreview() {
    KembarTheme(dynamicColor = false) {
        HomeScreen()
    }
}
