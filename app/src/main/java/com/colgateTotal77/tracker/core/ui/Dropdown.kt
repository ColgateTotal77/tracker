package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

interface NamedItem {
    val name: String?
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : NamedItem> Dropdown(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    displayText: (T) -> String,
    itemName: String,
    modifier: Modifier = Modifier,
    onCreateNewItem: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val currentDisplayText = if (selected != null) displayText(selected) else "Not Selected"

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { displayText(it).contains(searchQuery, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            isExpanded = it
            if (!it) {
                searchQuery = ""
                newItemText = ""
            }
        },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = currentDisplayText,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            label = { Text("Select $itemName") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                isExpanded = false
                searchQuery = ""
                newItemText = ""
            },
        ) {
            if (items.size > 6) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true
                )
                HorizontalDivider()
            }

            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayText(item)) },
                    onClick = {
                        onSelect(item)
                        isExpanded = false
                    },
                )
            }

            if (onCreateNewItem != null) {
                HorizontalDivider()

                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Add new $itemName") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (newItemText.isBlank()) return@IconButton
                                if (newItemText.lowercase().trim() in items.mapNotNull { it.name?.lowercase()?.trim() }) {
                                    //toast
                                    return@IconButton
                                }
                                onCreateNewItem(newItemText)
                                newItemText = ""
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item")
                        }
                    }
                )
            }
        }
    }
}