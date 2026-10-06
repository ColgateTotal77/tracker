package com.colgateTotal77.tracker.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
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
import com.colgateTotal77.tracker.screens.dashboard.ProgressBarPreferences
import com.colgateTotal77.tracker.core.ui.theme.dimensions
import com.colgateTotal77.tracker.screens.dashboard.ProgressBarModal

data class ProgressBarState(
    val settings: ProgressBarPreferences = ProgressBarPreferences(),
    val currentSpending: Long = 0L,
    val targetForFilter: Double = 0.0
)

@Composable
fun ProgressBar(
    progressState: ProgressBarState,
    label: String,
    color: Color,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    updateProgressBarPreferences: (settings: ProgressBarPreferences) -> Unit,
) {
    val dimensions = MaterialTheme.dimensions
    var isModalOpen by remember { mutableStateOf(false) }
    val rawProgress =
        if (progressState.targetForFilter > 0.0) {
            (progressState.currentSpending / progressState.targetForFilter).toFloat().coerceIn(0.0f, 1.0f)
        } else 0.0f

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(delayMillis = 600),
    )

    CardWrapper(onClick = { isModalOpen = true }) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(dimensions.cornerRadius)),
                color = color,
                trackColor = trackColor,
            )
        }
    }

    if (isModalOpen) {
        ProgressBarModal(
            settings = progressState.settings,
            onDismiss = { isModalOpen = false },
            onProgressBarPreferences = { settings ->
                isModalOpen = false
                updateProgressBarPreferences(settings)
            },
        )
    }
}