package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.colgateTotal77.tracker.R

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
                    CustomIconButton(
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
    val isUnchanged = trimmed == itemText(selected).trim()
    val isDuplicate = otherNames.any { it == trimmed && !isUnchanged }
    val isValid = trimmed.isNotEmpty() && !isUnchanged && !isDuplicate

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text(inputLabel) },
        singleLine = true,
        supportingText = {
            when {
                isUnchanged -> Text(stringResource(R.string.name_unchanged), style = MaterialTheme.typography.bodySmall)
                isDuplicate -> Text(stringResource(R.string.duplicate_name, itemName), style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            if(!isValid) return@KeyboardActions
            onInputDone(trimmed)
            onBackToDropdown()
        }),
        trailingIcon = {
            if (isValid) {
                CustomIconButton(onClick = {
                    onInputDone(trimmed)
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_name))
                }
            } else {
                CustomIconButton(onClick = {
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel_renaming))
                }
            }
        },
        modifier = modifier.focusRequester(focusRequester),
    )
}
