package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativePaint

fun Modifier.outerShadow(
    shape: Shape,
    color: Color = Color.Black.copy(alpha = 0.25f),
    blur: Dp = 12.dp,
    offsetY: Dp = 4.dp,
): Modifier = drawWithCache {
    val path = Path().apply {
        addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
    }
    val paint = Paint().apply {
        nativePaint.apply {
            this.color = color.toArgb()
            setShadowLayer(blur.toPx(), 0f, offsetY.toPx(), color.toArgb())
        }
    }
    onDrawBehind {
        clipPath(path, ClipOp.Difference) {
            drawIntoCanvas { it.drawPath(path, paint) }
        }
    }
}