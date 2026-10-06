package com.colgateTotal77.tracker.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.colgateTotal77.tracker.R

private val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
    Font(R.font.noto_sans_italic, FontWeight.Normal, FontStyle.Italic),
)

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
    val typography = Typography().let { defaults ->
        Typography(
            displayLarge = defaults.displayLarge.copy(fontFamily = NotoSans),
            displayMedium = defaults.displayMedium.copy(fontFamily = NotoSans),
            displaySmall = defaults.displaySmall.copy(fontFamily = NotoSans),
            headlineLarge = defaults.headlineLarge.copy(fontFamily = NotoSans),
            headlineMedium = defaults.headlineMedium.copy(fontFamily = NotoSans),
            headlineSmall = defaults.headlineSmall.copy(fontFamily = NotoSans),
            titleLarge = defaults.titleLarge.copy(fontFamily = NotoSans),
            titleMedium = defaults.titleMedium.copy(fontFamily = NotoSans),
            titleSmall = defaults.titleSmall.copy(fontFamily = NotoSans),
            bodyLarge = defaults.bodyLarge.copy(fontFamily = NotoSans),
            bodyMedium = defaults.bodyMedium.copy(fontFamily = NotoSans),
            bodySmall = defaults.bodySmall.copy(fontFamily = NotoSans),
            labelLarge = defaults.labelLarge.copy(fontFamily = NotoSans),
            labelMedium = defaults.labelMedium.copy(fontFamily = NotoSans),
            labelSmall = defaults.labelSmall.copy(fontFamily = NotoSans),
        )
    }

    MaterialTheme(shapes = shapes, typography = typography) {
        CompositionLocalProvider(LocalDimensions provides dimensions) {
            content()
        }
    }
}
