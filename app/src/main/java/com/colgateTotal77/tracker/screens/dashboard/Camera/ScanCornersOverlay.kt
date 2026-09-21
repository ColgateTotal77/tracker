package com.colgateTotal77.tracker.screens.dashboard.Camera

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private const val SCAN_WINDOW_FRACTION = 0.65f
private const val CORNER_LENGTH_FRACTION = 0.18f

@Composable
fun ScanCornersOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val side = minOf(size.width, size.height) * SCAN_WINDOW_FRACTION
        val windowSize = Size(side, side)
        val topLeft = Offset(
            (size.width - side) / 2f,
            (size.height - side) / 2f
        )
        val rect = Rect(topLeft, windowSize)
        val cornerLen = side * CORNER_LENGTH_FRACTION

        val stroke = Stroke(
            width = 4.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val accentColor = Color.White

        val paths = listOf(
            // top-left
            Path().apply {
                moveTo(rect.left, rect.top + cornerLen)
                lineTo(rect.left, rect.top)
                lineTo(rect.left + cornerLen, rect.top)
            },
            // top-right
            Path().apply {
                moveTo(rect.right - cornerLen, rect.top)
                lineTo(rect.right, rect.top)
                lineTo(rect.right, rect.top + cornerLen)
            },
            // bottom-right
            Path().apply {
                moveTo(rect.right, rect.bottom - cornerLen)
                lineTo(rect.right, rect.bottom)
                lineTo(rect.right - cornerLen, rect.bottom)
            },
            // bottom-left
            Path().apply {
                moveTo(rect.left + cornerLen, rect.bottom)
                lineTo(rect.left, rect.bottom)
                lineTo(rect.left, rect.bottom - cornerLen)
            },
        )
        paths.forEach { drawPath(it, color = accentColor, style = stroke) }
    }
}
