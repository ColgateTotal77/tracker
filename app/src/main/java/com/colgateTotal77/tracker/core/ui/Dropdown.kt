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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

private val DropdownPanelShape = RoundedCornerShape(4.dp)

private val DropdownItemHeight = 48.dp
private const val MaxVisibleDropdownItems = 6
private val MaxPanelHeight = DropdownItemHeight * MaxVisibleDropdownItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : NamedItem> Dropdown(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    displayText: (T) -> String,
    itemName: String,
    modifier: Modifier = Modifier,
    onCreateNewItem: ((String) -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    fun collapse() {
        isExpanded = false
        searchQuery = ""
        newItemText = ""
    }

    val currentDisplayText = if (selected != null) displayText(selected) else "Not Selected"

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { displayText(it).contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = currentDisplayText,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                label = { Text("Select $itemName") },
                leadingIcon = leadingIcon,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isExpanded) collapse() else isExpanded = true
                    }
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
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
                                collapse()
                            },
                        )
                    }

                    if (onCreateNewItem != null) {
                        OutlinedTextField(
                            value = newItemText,
                            onValueChange = { newItemText = it },
                            label = { Text("Add new $itemName") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (newItemText.isBlank()) return@IconButton
                                        if (newItemText.lowercase().trim() in items.map { displayText(it).lowercase().trim() }) {
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
    }
}
