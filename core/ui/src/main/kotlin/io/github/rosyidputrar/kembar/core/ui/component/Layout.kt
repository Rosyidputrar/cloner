package io.github.rosyidputrar.kembar.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val ReadableMaxWidth = 640.dp

/**
 * Fills the available width up to a comfortable reading width. On tablets and unfolded
 * foldables, text and forms stay readable instead of stretching across the screen.
 */
fun Modifier.readableWidth(): Modifier = this
    .widthIn(max = ReadableMaxWidth)
    .fillMaxWidth()
