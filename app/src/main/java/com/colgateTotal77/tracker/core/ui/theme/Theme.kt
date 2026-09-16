package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Theme(content: @Composable () -> Unit) {
    val dimensions = Dimensions()
    CompositionLocalProvider(LocalDimensions provides dimensions) {
        content()
    }
}