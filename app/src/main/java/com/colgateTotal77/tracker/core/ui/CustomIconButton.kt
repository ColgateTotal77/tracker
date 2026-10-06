package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.ui.theme.dimensions

@Composable
fun CustomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.Transparent,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dimensions = MaterialTheme.dimensions

    Surface(
        onClick = onClick,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .defaultMinSize(
                minWidth = 40.dp,
                minHeight = 40.dp
            ),
        shape = RoundedCornerShape(dimensions.cornerRadius),
        color = bgColor,
        enabled = enabled
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
