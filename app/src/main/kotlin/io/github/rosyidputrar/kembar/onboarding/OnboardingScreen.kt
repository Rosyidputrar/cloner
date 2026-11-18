package io.github.rosyidputrar.kembar.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.ControlPointDuplicate
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.rosyidputrar.kembar.R
import io.github.rosyidputrar.kembar.core.ui.component.KembarPrimaryButton
import io.github.rosyidputrar.kembar.core.ui.component.KembarTextButton
import io.github.rosyidputrar.kembar.core.ui.component.ObserveEffects
import io.github.rosyidputrar.kembar.core.ui.component.readableWidth
import io.github.rosyidputrar.kembar.core.ui.theme.KembarMotion
import io.github.rosyidputrar.kembar.core.ui.theme.KembarTheme
import io.github.rosyidputrar.kembar.navigation.OnboardingDestination
import kotlinx.coroutines.launch

fun NavGraphBuilder.onboardingScreen(onFinished: () -> Unit) {
    composable<OnboardingDestination> {
        OnboardingRoute(onFinished = onFinished)
    }
}

internal enum class OnboardingSlide(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
) {
    ManyAccounts(Icons.Outlined.ControlPointDuplicate, R.string.onboarding_1_title, R.string.onboarding_1_body),
    SeparateData(Icons.Outlined.Lock, R.string.onboarding_2_title, R.string.onboarding_2_body),
    AlwaysMarked(Icons.AutoMirrored.Outlined.Label, R.string.onboarding_3_title, R.string.onboarding_3_body),
    NotEveryApp(Icons.Outlined.ReportProblem, R.string.onboarding_4_title, R.string.onboarding_4_body),
}

@Composable
private fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    ObserveEffects(viewModel.effects) { effect ->
        when (effect) {
            OnboardingUiEffect.NavigateNext -> onFinished()
        }
    }
    OnboardingScreen(onFinish = { viewModel.onEvent(OnboardingUiEvent.Finish) })
}

@Composable
internal fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slides = OnboardingSlide.entries
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.lastIndex
    val spacing = KembarTheme.spacing

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = spacing.sm),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isLastPage) {
                    KembarTextButton(
                        text = stringResource(R.string.onboarding_skip),
                        onClick = onFinish,
                        modifier = Modifier.testTag(OnboardingTestTags.SKIP),
                    )
                } else {
                    // Keeps the pager at the same height on every page.
                    Spacer(Modifier.height(spacing.minTouchTarget))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                SlideContent(slide = slides[page])
            }

            PageIndicator(pagerState = pagerState, modifier = Modifier.padding(vertical = spacing.lg))

            KembarPrimaryButton(
                text = stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next),
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .readableWidth()
                    .padding(horizontal = spacing.xl)
                    .padding(bottom = spacing.xl)
                    .testTag(OnboardingTestTags.PRIMARY),
            )
        }
    }
}

@Composable
private fun SlideContent(slide: OnboardingSlide) {
    val spacing = KembarTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .readableWidth()
                .padding(horizontal = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = slide.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp),
                )
            }
            Text(
                text = stringResource(slide.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(slide.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PageIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    val description = stringResource(
        R.string.onboarding_page_position,
        pagerState.currentPage + 1,
        pagerState.pageCount,
    )
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(KembarTheme.spacing.sm),
    ) {
        repeat(pagerState.pageCount) { index ->
            val selected = index == pagerState.currentPage
            val color by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(KembarMotion.DURATION_SHORT, easing = KembarMotion.EasingStandard),
                label = "pageIndicatorColor",
            )
            Box(
                modifier = Modifier
                    .size(if (selected) 10.dp else 8.dp)
                    .background(color, CircleShape),
            )
        }
    }
}

internal object OnboardingTestTags {
    const val SKIP = "onboarding_skip"
    const val PRIMARY = "onboarding_primary"
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun OnboardingScreenPreview() {
    KembarTheme(dynamicColor = false) {
        OnboardingScreen(onFinish = {})
    }
}
