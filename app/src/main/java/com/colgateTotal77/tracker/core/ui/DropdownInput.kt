package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownInput(
    items: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    itemText: (T?) -> String,
    itemName: String,
    inputLabel: String,
    isInputEnabled: Boolean,
    onInputDone: (name: String) -> Unit,
    modifier: Modifier = Modifier,
    onCreateNewItem: ((name: String) -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    var isInputOpen by remember { mutableStateOf(false) }

    if (isInputOpen && isInputEnabled) {
        RenameField(
            selected = selected,
            itemName = itemName,
            itemText = itemText,
            inputLabel = inputLabel,
            otherNames = items.mapNotNull { itemText(it) },
            modifier = modifier,
            onInputDone = onInputDone,
            onBackToDropdown = { isInputOpen = false }
        )
    } else {
        Dropdown(
            items = items,
            selected = selected,
            onSelect = { onSelect(it) },
            itemText = itemText,
            itemName = itemName,
            onCreateNewItem = onCreateNewItem,
            leadingIcon = if (leadingIcon != null) {
                {
                    IconButton(
                        onClick = { isInputOpen = true },
                        enabled = isInputEnabled
                    ) {
                        leadingIcon()
                    }
                }
            } else null,
            modifier = modifier,
        )
    }
}

@Composable
private fun <T> RenameField(
    selected: T,
    itemName: String,
    inputLabel: String,
    itemText: (T) -> String,
    otherNames: List<String>,
    modifier: Modifier = Modifier,
    onInputDone: (name: String) -> Unit,
    onBackToDropdown: () -> Unit
) {
    var nameInput by remember(selected) { mutableStateOf(itemText(selected)) }
    val focusRequester = remember { FocusRequester() }

    val trimmed = nameInput.trim()
    val isUnchanged = trimmed.equals(itemText(selected).trim(), ignoreCase = true)
    val isDuplicate = otherNames.any { it.equals(trimmed, ignoreCase = true) && !isUnchanged }
    val isValid = trimmed.isNotEmpty() && !isUnchanged && !isDuplicate

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text(inputLabel) },
        singleLine = true,
        supportingText = {
            when {
                isUnchanged -> Text("Name unchanged", style = MaterialTheme.typography.bodySmall)
                isDuplicate -> Text("$itemName with this name already exists", style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            if(!isValid) return@KeyboardActions
            onInputDone(trimmed)
            onBackToDropdown()
        }),
        trailingIcon = {
            if (isValid) {
                IconButton(onClick = {
                    onInputDone(trimmed)
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save name")
                }
            } else {
                IconButton(onClick = {
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel renaming")
                }
            }
        },
        modifier = modifier.focusRequester(focusRequester),
    )
}