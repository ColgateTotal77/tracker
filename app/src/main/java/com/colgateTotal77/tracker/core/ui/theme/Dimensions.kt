package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.material.search.SearchBar

data class Dimensions(
    val spacingMedium: Dp = 16.dp,
    val spacingSmall: Dp = 8.dp,
    val navigationBarHeight: Dp = 64.dp,
    val navigationBarHeightAndPadding: Dp = navigationBarHeight + spacingSmall,
    val searchBarHeightAndPadding: Dp = 64.dp + spacingSmall,
    val screenHorizontalPadding: Dp = 16.dp,
    val screenPadding: PaddingValues = PaddingValues(start = screenHorizontalPadding, end = screenHorizontalPadding, top = 0.dp, bottom = navigationBarHeightAndPadding),
    val screenBottomPadding: Dp = 64.dp + spacingMedium,
    val segmentedButtonHeight: Dp = 40.dp + spacingMedium,
    val contentPadding: Dp = 12.dp,
    val cornerRadius: Dp = 12.dp,
    val leadingIconSize: Dp = 48.dp,
    val iconContainerPadding: Dp = 4.dp,
    val verticalDividerHeight: Dp = 34.dp,
)

@Composable
fun PaddingValues.addPadding(
    top: Dp = 0.dp,
    bottom: Dp = 0.dp
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateStartPadding(layoutDirection),
        top = this.calculateTopPadding() + top,
        end = this.calculateEndPadding(layoutDirection),
        bottom = this.calculateBottomPadding() + bottom
    )
}

val LocalDimensions: ProvidableCompositionLocal<Dimensions> =
    staticCompositionLocalOf { Dimensions() }