package com.colgateTotal77.tracker.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val DropdownPanelShape = RoundedCornerShape(4.dp)
private val DropdownItemHeight = 48.dp
private const val MaxVisibleDropdownItems = 6
private val MaxPanelHeight = DropdownItemHeight * MaxVisibleDropdownItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    items: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    itemText: (T) -> String,
    itemName: String,
    modifier: Modifier = Modifier,
    onCreateNewItem: ((name: String) -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val initialItem = remember { selected }
    var isExpanded by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    fun collapse() {
        isExpanded = false
        searchQuery = ""
        newItemText = ""
    }

    val currentDisplayText = if (selected != null) itemText(selected) else "Not Selected"

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemText(it).contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentDisplayText,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    ),
                readOnly = true,
                singleLine = true,
                label = { Text("Select $itemName") },
                leadingIcon = leadingIcon,
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selected != initialItem) {
                            IconButton(onClick = { onSelect(initialItem) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Reset selection"
                                )
                            }
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, DropdownPanelShape)
                    .background(MaterialTheme.colorScheme.surface, DropdownPanelShape)
                    .heightIn(max = MaxPanelHeight)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (items.size > 6) {
                        item {
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
                    }

                    items(filteredItems) { item ->
                        DropdownMenuItem(
                            text = { Text(itemText(item)) },
                            onClick = {
                                onSelect(item)
                                collapse()
                            },
                        )
                    }

                    if (onCreateNewItem != null) {
                        item {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                label = { Text("Add new $itemName") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (newItemText.isBlank()) return@IconButton
                                            if (newItemText.lowercase().trim() in items.map { itemText(it).lowercase().trim() }) {
                                                // toast
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
        }
    }
}