package com.colgateTotal77.tracker.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownOnIconButton(
    items: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    itemText: @Composable (T) -> String,
    icon: (@Composable () -> Unit),
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        CustomIconButton(
            onClick = { expanded = !expanded },
            bgColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
        ) { icon() }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            matchAnchorWidth = false,
        ) {
            items.forEach { item ->
                val isSelected = selected == item

                DropdownMenuItem(
                    text = { Text(text = itemText(item)) },
                    onClick = {
                        if (isSelected) onSelect(null)
                        else onSelect(item)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
    }
}
