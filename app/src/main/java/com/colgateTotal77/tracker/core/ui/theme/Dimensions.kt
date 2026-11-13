package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val navigationBarHeightAndPadding: Dp = 76.dp, //64 + 12
    val screenHorizontalPadding: Dp = 16.dp,
    val screenPadding: PaddingValues = PaddingValues(start = screenHorizontalPadding, end = screenHorizontalPadding, top = 0.dp, bottom = navigationBarHeightAndPadding),
    val screenBottomPadding: Dp = 64.dp,
    val contentPadding: Dp = 12.dp,
    val spacingMedium: Dp = 16.dp,
    val spacingSmall: Dp = 8.dp,
    val cornerRadius: Dp = 12.dp,
    val leadingIconSize: Dp = 48.dp,
    val iconContainerPadding: Dp = 4.dp,
    val verticalDividerHeight: Dp = 34.dp,
)

val LocalDimensions: ProvidableCompositionLocal<Dimensions> =
    staticCompositionLocalOf { Dimensions() }

val MaterialTheme.dimensions: Dimensions
    @Composable
    get() = LocalDimensions.current
