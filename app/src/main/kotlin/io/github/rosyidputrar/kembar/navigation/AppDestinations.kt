package io.github.rosyidputrar.kembar.navigation

import kotlinx.serialization.Serializable

/** First-run slides. */
@Serializable
data object OnboardingDestination

/**
 * Compatibility and terms disclaimer. With [readOnly] the user is re-reading it from
 * "Tentang" and there is nothing to accept.
 */
@Serializable
data class DisclaimerDestination(val readOnly: Boolean)
