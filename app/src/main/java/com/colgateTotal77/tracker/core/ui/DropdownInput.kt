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
    onInputDone: (name: String) -> Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    onTouchChange: (Boolean) -> Unit = {},
) {
    var isInputOpen by remember { mutableStateOf(false) }

    if (isInputOpen) {
        RenameField(
            selected = selected,
            itemText = itemText,
            inputLabel = inputLabel,
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
            leadingIcon = if (leadingIcon != null) {
                {
                    CustomIconButton(onClick = { isInputOpen = true }) {
                        leadingIcon()
                    }
                }
            } else null,
            modifier = modifier,
            onTouchChange = onTouchChange,
        )
    }
}

@Composable
private fun <T> RenameField(
    selected: T,
    inputLabel: String,
    itemText: (T) -> String,
    modifier: Modifier = Modifier,
    onInputDone: (name: String) -> Boolean,
    onBackToDropdown: () -> Unit
) {
    var nameInput by remember(selected) { mutableStateOf(itemText(selected)) }
    val focusRequester = remember { FocusRequester() }

    val trimmed = nameInput.trim()
    val isUnchanged = trimmed == itemText(selected).trim()
    val isValid = trimmed.isNotEmpty() && !isUnchanged

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text(inputLabel) },
        singleLine = true,
        supportingText = {
            if (isUnchanged) {
                Text(stringResource(R.string.name_unchanged), style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            if (isValid && onInputDone(trimmed)) onBackToDropdown()
        }),
        trailingIcon = {
            if (isValid) {
                CustomIconButton(onClick = {
                    if (onInputDone(trimmed)) onBackToDropdown()
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
