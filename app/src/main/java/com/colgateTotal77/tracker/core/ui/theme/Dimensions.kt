package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val screenPadding: Dp = 4.dp,
    val screenBottomPadding: Dp = 64.dp,
    val sectionSpacing: Dp = 0.dp,
    val listItemSpacing: Dp = 16.dp,
    val buttonPaddingHorizontal: Dp = 0.dp,
    val buttonPaddingVertical: Dp = 0.dp,
)

val LocalDimensions: ProvidableCompositionLocal<Dimensions> =
    staticCompositionLocalOf { Dimensions() }

val MaterialTheme.dimensions: Dimensions
    @Composable
    get() = LocalDimensions.current