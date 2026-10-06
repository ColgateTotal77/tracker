package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.R

@Composable
fun ActionDropdownMenu(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(onCloseMenu: () -> Unit) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        CustomIconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            content { menuExpanded = false }
        }
    }
}
