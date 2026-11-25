package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

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
    onTouchChange: (Boolean) -> Unit = {},
    enableResetButton: Boolean = true,
    initialItem: T? = remember { selected },
    additionalBottomContent: (@Composable () -> Unit)? = null,
) {
    val currentOnTouchChange by rememberUpdatedState(onTouchChange)
    val dimensions = LocalDimensions.current
    val panelShape = RoundedCornerShape(dimensions.cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isExpanded by remember { mutableStateOf(false) }
    var newItemText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    fun collapse() {
        isExpanded = false
        searchQuery = ""
        newItemText = ""
    }

    val currentDisplayText = if (selected != null) itemText(selected) else stringResource(R.string.not_selected)

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemText(it).contains(searchQuery, ignoreCase = true) }
    }

    val blockParentScroll = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ) = Offset(x = 0f, y = available.y)
        }
    }

    LaunchedEffect(isExpanded) { currentOnTouchChange(isExpanded) }

    Column(modifier = modifier
        .fillMaxWidth()
        .nestedScroll(blockParentScroll)
    ) {
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
                interactionSource = interactionSource,
                label = { Text(stringResource(R.string.select_item, itemName)) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (enableResetButton && selected != initialItem) {
                            CustomIconButton(onClick = { onSelect(initialItem) }) {
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
                    .padding(top = dimensions.spacingSmall)
                    .border(
                        if (isFocused) 2.dp else 1.dp,
                        if (isFocused) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                        panelShape,
                    )
                    .background(MaterialTheme.colorScheme.surface, panelShape)
                    .heightIn(max = MaxPanelHeight)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (items.size > MaxVisibleDropdownItems) {
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent,
                                ),
                                placeholder = { Text(stringResource(R.string.search)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
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

                    if (additionalBottomContent != null) {
                        item {
                            Box(modifier = Modifier.padding(dimensions.contentPadding)) {
                                additionalBottomContent()
                            }
                        }
                    }
                }
            }
        }
    }
}
