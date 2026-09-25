package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.transaction.TransactionDraft
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.screens.dashboard.Camera.Camera
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val displayMarkets = remember(markets, selectedMarket) {
        val updatedMarkets = markets.map {
            if (it.id == selectedMarket?.id) selectedMarket!! else it
        }

        val validMarkets = updatedMarkets.filter {
            it.id == selectedMarket?.id || !it.name.isNullOrBlank()
        }

        if (selectedMarket?.id == 0 && updatedMarkets.none { it.id == 0 }) {
            validMarkets + selectedMarket!!
        } else validMarkets
    }

    val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
    var dateTimeInput by remember { mutableStateOf(formatter.format(Date())) }
    val isDateValid = remember(dateTimeInput) {
        if (dateTimeInput.length < 12) false
        else {
            try {
                val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
                formatter.isLenient = false
                formatter.parse(dateTimeInput) != null
            } catch (e: Exception) {
                false
            }
        }
    }

    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Add Transaction",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { isCameraOpen = true }) {
                        Icon(Icons.Default.QrCode2, contentDescription = "Open Camera")
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )

                Dropdown(
                    items = Currency.entries,
                    selected = currency,
                    onSelect = { selectedCurrency ->
                        selectedCurrency?.let { currency = it }
                    },
                    itemText = { it.dropdownText },
                    itemName = "Currency",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )

                DropdownInput(
                    items = displayMarkets,
                    itemName = "Market",
                    inputLabel = "Rename market",
                    itemText = { it?.name ?: "" },
                    selected = selectedMarket,
                    onSelect = { selectedMarket = it },
                    isInputEnabled = selectedMarket != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                    onCreateNewItem = { name ->
                        selectedMarket = MarketEntity(tin = null, name = name)
                    },
                    leadingIcon = if (selectedMarket != null) {
                        { Icon(Icons.Default.Edit, contentDescription = "Rename market") }
                    } else null,
                    onInputDone = { name ->
                        selectedMarket = selectedMarket?.copy(name = name)
                    },
                )

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing)
                )

                Button(
                    onClick = {
                        val transactionDraft = TransactionDraft(
                            amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt(),
                            currency = currency,
                            market = selectedMarket?.let {
                                when {
                                    it.id != 0 -> MarketChoice.Existing(it)
                                    !it.name.isNullOrBlank() -> MarketChoice.New(it.name!!)
                                    else -> MarketChoice.None
                                }
                            } ?: MarketChoice.None,
                            date = formatter.parse(dateTimeInput)!!.time,
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
