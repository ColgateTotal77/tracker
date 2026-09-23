package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.ui.Dropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketDropdown(
    markets: List<MarketEntity>,
    selectedMarketId: Int?,
    onSelect: (MarketEntity?) -> Unit,
    onNameChange: (MarketEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRenaming by remember { mutableStateOf(false) }

    val selected = remember(selectedMarketId, markets) {
        markets.find { it.id == selectedMarketId }
    }

    if (isRenaming && selected != null) {
        RenameField(
            market = selected,
            otherNames = markets.mapNotNull { it.name },
            modifier = modifier,
            onNameChange = { market ->
                onNameChange(market)
            },
            onBackToDropdown = { isRenaming = false }
        )
    } else {
        Dropdown(
            items = markets,
            selected = selected,
            onSelect = { onSelect(it) },
            displayText = { it.name.orEmpty() },
            itemName = "Market",
            onCreateNewItem = { name ->
                val newMarket = MarketEntity(tin = null, name = name)
                onSelect(newMarket)
            },
            leadingIcon = if (selected != null) {
                {
                    IconButton(onClick = { isRenaming = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename market")
                    }
                }
            } else null,
            modifier = modifier,
        )
    }
}

@Composable
private fun RenameField(
    market: MarketEntity,
    otherNames: List<String>,
    modifier: Modifier = Modifier,
    onNameChange: (MarketEntity) -> Unit,
    onBackToDropdown: () -> Unit
) {
    var nameInput by remember(market.id) { mutableStateOf(market.name.orEmpty()) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val trimmed = nameInput.trim()
    val isUnchanged = trimmed.equals(market.name?.trim(), ignoreCase = true)
    val isDuplicate = otherNames.any { it.equals(trimmed, ignoreCase = true) && !isUnchanged }
    val isValid = trimmed.isNotEmpty() && !isUnchanged && !isDuplicate

    LaunchedEffect(market.id) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = nameInput,
        onValueChange = { nameInput = it },
        label = { Text("Rename market") },
        singleLine = true,
        supportingText = {
            when {
                isUnchanged -> Text("Name unchanged", style = MaterialTheme.typography.bodySmall)
                isDuplicate -> Text("Market with this name already exists", style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardActions = KeyboardActions(onDone = {
            if(!isValid) return@KeyboardActions
            onNameChange(market.copy(name = trimmed))
            keyboardController?.hide()
            onBackToDropdown()
        }),
        trailingIcon = {
            if (isValid) {
                IconButton(onClick = {
                    onNameChange(market.copy(name = trimmed))
                    keyboardController?.hide()
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save name")
                }
            } else {
                IconButton(onClick = {
                    keyboardController?.hide()
                    onBackToDropdown()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel renaming")
                }
            }
        },
        modifier = modifier.focusRequester(focusRequester),
    )
}