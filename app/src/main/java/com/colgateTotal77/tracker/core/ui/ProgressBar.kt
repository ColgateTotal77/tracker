package com.colgateTotal77.tracker.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProgressBar(
    current: Double,
    target: Double,
    label: String,
    color: Color,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onUpdateTarget: (Double) -> Unit,
) {
    var isModalOpen by remember { mutableStateOf(false) }
    val rawProgress = (current / target).toFloat().coerceIn(0.0f, 1.0f)

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(delayMillis = 600),
    )

    CardWrapper(onClick = { isModalOpen = true }) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = trackColor,
            )
        }
    }

    if (isModalOpen) {
        ProgressBarModal(
            onDismiss = { isModalOpen = false },
            onChangeProgress = { newTarget ->
                isModalOpen = false
                onUpdateTarget(newTarget)
            },
        )
    }
}