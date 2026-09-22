package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CardWrapper(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth().padding(4.dp), // Outer margin
            shape = RoundedCornerShape(8.dp),
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth().padding(4.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}