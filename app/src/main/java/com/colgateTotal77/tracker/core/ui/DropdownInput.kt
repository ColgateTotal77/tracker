package com.colgateTotal77.tracker.core.ui

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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

@Composable
fun <T> DropdownInput(
    items: List<T>,
    selected: T?,
    onSelect: (T?) -> Unit,
    itemText: (T?) -> String,
    itemName: String,
    inputLabel: String,
    isInputOpen: Boolean,
    onIsInputOpenChange: (Boolean) -> Unit,
    onInputDone: (name: String) -> Boolean,
    modifier: Modifier = Modifier,
    onTouchChange: (Boolean) -> Unit = {},
    additionalBottomContent: (@Composable () -> Unit)? = null,
) {
    val initialItem = remember(selected) { selected }

    if (isInputOpen) {
        InputField(
            inputLabel = inputLabel,
            modifier = modifier,
            onInputDone = onInputDone,
            onBackToDropdown = { onIsInputOpenChange(false) }
        )
    } else {
        Dropdown(
            items = items,
            selected = selected,
            onSelect = { onSelect(it) },
            itemText = itemText,
            itemName = itemName,
            modifier = modifier,
            onTouchChange = onTouchChange,
            initialItem = initialItem,
            additionalBottomContent = additionalBottomContent
        )
    }
}

@Composable
private fun InputField(
    inputLabel: String,
    modifier: Modifier = Modifier,
    onInputDone: (name: String) -> Boolean,
    onBackToDropdown: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val trimmed = nameInput.trim()
    val isValid = trimmed.isNotEmpty()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text(inputLabel) },
        singleLine = true,
        supportingText = {
            if (!isValid) Text(stringResource(R.string.field_required), style = MaterialTheme.typography.bodySmall)
        },
        keyboardActions = KeyboardActions(onDone = {
            if (isValid && onInputDone(trimmed)) onBackToDropdown()
        }),
        trailingIcon = {
            if (isValid) {
                CustomIconButton(onClick = { if (onInputDone(trimmed)) onBackToDropdown() }) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_name))
                }
            } else {
                CustomIconButton(onClick = { onBackToDropdown() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
            }
        },
        modifier = modifier.focusRequester(focusRequester),
    )
}
