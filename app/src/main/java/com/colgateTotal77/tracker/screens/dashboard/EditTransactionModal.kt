package com.colgateTotal77.tracker.screens.dashboard

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionModal(
    transaction: TransactionEntity,
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onUpdate: (amountMinor: Int, currency: Currency, selectedMarket: MarketEntity?, date: Long?) -> Unit,
) {
    var amountInput by remember { mutableStateOf(formatMoney(transaction.amountMinor)) }
    var currency by remember { mutableStateOf(transaction.currency) }
    var selectedMarket by remember { mutableStateOf(markets.find { it.id == transaction.marketId}) }

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

    val formatter = java.text.SimpleDateFormat("ddMMyyyyHHmm", java.util.Locale.getDefault())
    var dateTimeInput by remember { mutableStateOf(formatter.format(java.util.Date(transaction.date))) }
    val isDateValid = remember(dateTimeInput) {
        if (dateTimeInput.length < 12) false
        else {
            try {
                val formatter = java.text.SimpleDateFormat("ddMMyyyyHHmm", java.util.Locale.getDefault())
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
                Text(
                    "Transaction",
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = transaction.source == TransactionSource.MANUAL,
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

                MarketDropdown(
                    markets = displayMarkets,
                    selectedMarketId = selectedMarket?.id,
                    onSelect = { selectedMarket = it },
                    onNameChange = { market ->
                        Log.d("Transaction", market.toString())
                        selectedMarket = market
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing)
                )

                Button(
                    onClick = {
                        val parsedDateMillis = formatter.parse(dateTimeInput)!!.time
                        val amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt()
                        onUpdate(amountMinor, currency, selectedMarket, parsedDateMillis)
                    },
                    enabled = isDateValid && amountInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Transaction")
                }
            }
        }
    }
}