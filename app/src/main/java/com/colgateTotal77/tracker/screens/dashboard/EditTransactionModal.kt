package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.market.MarketChoice
import com.colgateTotal77.tracker.core.database.market.MarketEntity
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.enums.TransactionSource
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import com.colgateTotal77.tracker.core.ui.DropdownInput
import com.colgateTotal77.tracker.core.ui.AppToast
import com.colgateTotal77.tracker.core.ui.ToastType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionModal(
    transaction: TransactionEntity,
    markets: List<MarketEntity>,
    onDismiss: () -> Unit,
    onUpdate: (amountMinor: Int, currency: Currency, selectedMarket: MarketChoice, date: Long?) -> Unit,
) {
    var dropdownTouched by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf(formatMoney(transaction.amountMinor)) }
    var currency by remember { mutableStateOf(transaction.currency) }
    var selectedMarket by remember { mutableStateOf(markets.find { it.id == transaction.marketId}) }

    val displayMarkets = remember(markets, selectedMarket) {
        val updatedMarkets = markets.map { if (it.id == selectedMarket?.id) selectedMarket!! else it }
        val validMarkets = updatedMarkets.filter { it.id == selectedMarket?.id || !it.name.isNullOrBlank() }

        if (selectedMarket?.id == 0 && updatedMarkets.none { it.id == 0 }) validMarkets + selectedMarket!!
        else validMarkets
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
    val duplicateMarketMessage = stringResource(R.string.duplicate_name, stringResource(R.string.market))

    AppModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetGesturesEnabled = !dropdownTouched,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
            ) {
                Text(
                    "Transaction",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = transaction.source == TransactionSource.MANUAL,
                    modifier = Modifier.fillMaxWidth(),
                )

                Dropdown(
                    onTouchChange = { dropdownTouched = it },
                    items = Currency.entries,
                    selected = currency,
                    onSelect = { selectedCurrency ->
                        selectedCurrency?.let { currency = it }
                    },
                    itemText = { it.dropdownText },
                    itemName = stringResource(R.string.currency),
                    modifier = Modifier.fillMaxWidth(),
                )

                DropdownInput(
                    onTouchChange = { dropdownTouched = it },
                    items = displayMarkets,
                    itemName = stringResource(R.string.market),
                    inputLabel = stringResource(R.string.rename_market),
                    itemText = { it?.name ?: "" },
                    selected = selectedMarket,
                    onSelect = { selectedMarket = it },
                    modifier = Modifier.fillMaxWidth(),
                    onInputDone = { newName ->
                        val duplicate = markets.any {
                            it.id != selectedMarket?.id &&
                                it.name.orEmpty().trim().equals(newName, ignoreCase = true)
                        }
                        if (duplicate) {
                            AppToast.show(duplicateMarketMessage, ToastType.Error)
                            false
                        } else {
                            selectedMarket = selectedMarket?.copy(name = newName)
                                ?: MarketEntity(tin = null, name = newName)
                            true
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (selectedMarket == null) Icons.Default.Add else Icons.Default.Edit,
                            contentDescription = stringResource(
                                if (selectedMarket == null) R.string.create_market else R.string.rename_market
                            )
                        )
                    },
                )

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                )

                CustomButton(
                    onClick = {
                        val parsedDateMillis = formatter.parse(dateTimeInput)!!.time
                        val amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt()

                        val market = selectedMarket?.let { m ->
                            when {
                                m.id == 0 && m.name != null -> MarketChoice.New(m.name!!)
                                m.id > 0 -> MarketChoice.Existing(m)
                                else -> MarketChoice.None
                            }
                        } ?: MarketChoice.None

                        onUpdate(amountMinor, currency, market, parsedDateMillis)
                    },
                    buttonText = stringResource(R.string.save_transaction),
                    enabled = isDateValid && amountInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
