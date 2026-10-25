package com.colgateTotal77.tracker.screens.operations.Exchange

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.exchange.ExchangeEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.parseInputToMinor
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import com.colgateTotal77.tracker.core.ui.InputWithDropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.screens.overview.ExchangeRatesPreferences
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditExchangeModal(
    exchange: ExchangeEntity,
    exchangeRates: ExchangeRatesPreferences,
    onDismiss: () -> Unit,
    onUpdate: (
        fromAmountMinor: Long,
        fromCurrency: Currency,
        toAmountMinor: Long,
        toCurrency: Currency,
        date: Long
    ) -> Unit,
) {
    val dimensions = LocalDimensions.current

    var fromAmountInput by remember { mutableStateOf<String?>(formatMoney(exchange.fromAmountMinor)) }
    var fromCurrency by remember { mutableStateOf(exchange.fromCurrency) }

    var toAmountInput by remember { mutableStateOf<String?>(formatMoney(exchange.toAmountMinor)) }
    var toCurrency by remember { mutableStateOf(exchange.toCurrency) }

    val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
    var dateTimeInput by remember { mutableStateOf(formatter.format(Date(exchange.date))) }
    val isDateValid = remember(dateTimeInput) {
        dateTimeInput.length == 12 && try {
            val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(dateTimeInput) != null
        } catch (e: Exception) {
            false
        }
    }

    val displayedFromAmount = fromAmountInput
        ?: parseInputToMinor(toAmountInput)?.let {
            calculateAmountInput(it, toCurrency, fromCurrency, exchangeRates)
        }

    val displayedToAmount = toAmountInput
        ?: parseInputToMinor(fromAmountInput)?.let {
            calculateAmountInput(it, fromCurrency, toCurrency, exchangeRates)
        }

    val fromAmountMinor = parseInputToMinor(displayedFromAmount)?.takeIf { it > 0 }
    val toAmountMinor = parseInputToMinor(displayedToAmount)?.takeIf { it > 0 }

    val effectiveRate = remember(fromAmountMinor, toAmountMinor) {
        if (fromAmountMinor == null || toAmountMinor == null) null
        else BigDecimal.valueOf(toAmountMinor)
            .divide(BigDecimal.valueOf(fromAmountMinor), 6, RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString()
    }

    AppModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(stringResource(R.string.edit_exchange), style = MaterialTheme.typography.titleLarge)

                InputWithDropdown(
                    value = displayedFromAmount ?: "",
                    onValueChange = {
                        fromAmountInput = it
                        if (toAmountInput == "") toAmountInput = null
                    },
                    selectedInDropdown = fromCurrency,
                    dropdownItems = Currency.entries,
                    onDropdownSelect = { newCurrency ->
                        if (newCurrency == toCurrency) toCurrency = fromCurrency
                        fromCurrency = newCurrency
                    },
                    dropdownItemText = { it.dropdownTextSmall },
                    label = stringResource(R.string.exchange_you_pay),
                    dropdownContentDescription = stringResource(R.string.select_item, stringResource(R.string.currency)),
                )

                InputWithDropdown(
                    value = displayedToAmount ?: "",
                    onValueChange = {
                        toAmountInput = it
                        if (fromAmountInput == "") fromAmountInput = null
                    },
                    selectedInDropdown = toCurrency,
                    dropdownItems = Currency.entries,
                    onDropdownSelect = { newCurrency ->
                        if (newCurrency == fromCurrency) fromCurrency = toCurrency
                        toCurrency = newCurrency
                    },
                    dropdownItemText = { it.dropdownTextSmall },
                    label = stringResource(R.string.exchange_you_receive),
                    dropdownContentDescription = stringResource(R.string.select_item, stringResource(R.string.currency)),
                )

                if (effectiveRate != null) {
                    Text(
                        stringResource(R.string.exchange_rate, fromCurrency.code, effectiveRate, toCurrency.code),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                )

                CustomButton(
                    onClick = {
                        if (fromAmountMinor == null || toAmountMinor == null) return@CustomButton

                        val date = formatter.parse(dateTimeInput)!!.time
                        onUpdate(fromAmountMinor, fromCurrency, toAmountMinor, toCurrency, date)
                    },
                    buttonText = stringResource(R.string.save_exchange),
                    enabled = fromAmountMinor != null && toAmountMinor != null && isDateValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
