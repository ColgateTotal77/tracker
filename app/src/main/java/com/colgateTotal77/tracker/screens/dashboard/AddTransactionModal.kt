package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.rememberDatePickerState
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.screens.dashboard.Camera.Camera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionModal(
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onAdd: (TransactionDraft) -> Unit,
) {
    var isCameraOpen by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(Currency.UAH) }
    var selectedMarket by remember { mutableStateOf<MarketEntity?>(null) }
    var customMarket by remember { mutableStateOf<MarketEntity?>(null) }
    val displayMarkets = remember(markets, customMarket) {
        if (customMarket != null) markets + customMarket!! else markets
    }
    val date = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Add Transaction",
                        modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { isCameraOpen = true }) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Open Camera")
                    }
                }
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                Dropdown(
                    items = Currency.entries,
                    selected = currency,
                    onSelect = { currency = it },
                    displayText = { it.dropdownText },
                    itemName = "Currency",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                Dropdown(
                    items = displayMarkets.filter { it.name != null },
                    selected = selectedMarket,
                    onSelect = { selectedMarket = it },
                    displayText = { it.name.orEmpty() },
                    itemName = "Market",
                    onCreateNewItem = { newMarketName ->
                        val newMarket = MarketEntity(
                            tin = newMarketName,
                            name = newMarketName
                        )

                        customMarket = newMarket
                        selectedMarket = newMarket
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                DatePicker(
                    state = date,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                Button(
                    onClick = {
                        val transactionDraft = TransactionDraft(
                            amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt(),
                            currency = currency,
                            market = selectedMarket?.let {
                                when {
                                    it.id != 0 -> MarketChoice.Existing(it.id)
                                    it.name != null -> MarketChoice.New(it.name)
                                    else -> MarketChoice.None
                                }
                            } ?: MarketChoice.None,
                            date = date.selectedDateMillis ?: System.currentTimeMillis(),
                            source = TransactionSource.MANUAL,
                        )

                        onAdd(transactionDraft)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Transaction")
                }
            }
        }
    }

    if (isCameraOpen) {
        Dialog(
            onDismissRequest = { isCameraOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                Camera(
                    onAdd = onAdd,
                    onClose = { isCameraOpen = false },
                )
            }
        }
    }
}
