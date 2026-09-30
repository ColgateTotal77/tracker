package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Theme(content: @Composable () -> Unit) {
    val dimensions = Dimensions()
    val shape = RoundedCornerShape(dimensions.cornerRadius)
    val shapes = Shapes(
        extraSmall = shape,
        small = shape,
        medium = shape,
        large = shape,
        extraLarge = shape,
    )

    MaterialTheme(shapes = shapes) {
        CompositionLocalProvider(LocalDimensions provides dimensions) {
            content()
        }
    }
}
