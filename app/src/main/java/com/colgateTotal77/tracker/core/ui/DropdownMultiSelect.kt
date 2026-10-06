package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.res.stringResource
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.theme.dimensions

private val DropdownItemHeight = 48.dp
private const val MaxVisibleDropdownItems = 6
private val MaxPanelHeight = DropdownItemHeight * MaxVisibleDropdownItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T, K> DropdownMultiSelect(
    items: List<T>,
    selectedKeys: Set<K>,
    key: (T) -> K,
    itemText: (T) -> String,
    itemName: String,
    onSelectionChange: (Set<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimensions = MaterialTheme.dimensions
    val panelShape = RoundedCornerShape(dimensions.cornerRadius)

    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemText(it).contains(searchQuery, ignoreCase = true) }
    }

    val displayText = if (selectedKeys.isEmpty()) stringResource(R.string.all_items, itemName)
        else if (selectedKeys.size == 1) itemText(items.first { key(it) == selectedKeys.first() })
        else stringResource(R.string.selected_count, itemName, selectedKeys.size)

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                readOnly = true,
                singleLine = true,
                label = { Text(stringResource(R.string.select_item, itemName)) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedKeys.isNotEmpty()) {
                            CustomIconButton(onClick = { onSelectionChange(emptySet()) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.reset_selection)
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
                    .padding(top = dimensions.elementSpacing)
                    .border(1.dp, MaterialTheme.colorScheme.outline, panelShape)
                    .background(MaterialTheme.colorScheme.surface, panelShape)
                    .heightIn(max = MaxPanelHeight)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (items.size > MaxVisibleDropdownItems) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = dimensions.contentPadding,
                                        vertical = dimensions.elementSpacing,
                                    ),
                                placeholder = { Text(stringResource(R.string.search)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                                singleLine = true,
                            )
                            HorizontalDivider()
                        }
                    }

                    items(filteredItems) { item ->
                        val itemKey = key(item)
                        val isSelected = itemKey in selectedKeys
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectionChange(
                                        if (isSelected) selectedKeys - itemKey
                                        else selectedKeys + itemKey
                                    )
                                }
                                .heightIn(min = DropdownItemHeight),
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    onSelectionChange(
                                        if (isSelected) selectedKeys - itemKey
                                        else selectedKeys + itemKey
                                    )
                                },
                            )
                            Text(
                                text = itemText(item),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}
