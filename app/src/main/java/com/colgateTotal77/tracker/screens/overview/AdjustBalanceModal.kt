package com.colgateTotal77.tracker.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.operation.CurrencyBalance
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.filterDecimalWithMinus
import com.colgateTotal77.tracker.core.parseInputToMinor
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.formatMoney


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceModal(
    balances: Map<Currency, Long>,
    onDismiss: () -> Unit,
    onAdd: (List<CurrencyBalance>) -> Unit,
) {
    val uahBalance = balances[Currency.UAH] ?: 0L
    val usdBalance = balances[Currency.USD] ?: 0L
    val eurBalance = balances[Currency.EUR] ?: 0L
    var uahBalanceInput by remember { mutableStateOf(formatMoney(uahBalance)) }
    var usdBalanceInput by remember { mutableStateOf(formatMoney(usdBalance)) }
    var eurBalanceInput by remember { mutableStateOf(formatMoney(eurBalance)) }
    val adjustmentUahAmountMinor = remember(uahBalanceInput, uahBalance) {
        runCatching {
            parseInputToMinor(uahBalanceInput)?.let { Math.subtractExact(it, uahBalance) }
        }.getOrNull() ?: 0L
    }
    val adjustmentUsdAmountMinor = remember(usdBalanceInput, usdBalance) {
        runCatching {
            parseInputToMinor(usdBalanceInput)?.let { Math.subtractExact(it, usdBalance) }
        }.getOrNull() ?: 0L
    }
    val adjustmentEurAmountMinor = remember(eurBalanceInput, eurBalance) {
        runCatching {
            parseInputToMinor(eurBalanceInput)?.let { Math.subtractExact(it, eurBalance) }
        }.getOrNull() ?: 0L
    }

    val dimensions = LocalDimensions.current

    AppModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    stringResource(R.string.change_balance),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = uahBalanceInput,
                    onValueChange = { uahBalanceInput = it.filterDecimalWithMinus() },
                    label = { Text(stringResource(R.string.new_balance, Currency.UAH.code)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = usdBalanceInput,
                    onValueChange = { usdBalanceInput = it.filterDecimalWithMinus() },
                    label = { Text(stringResource(R.string.new_balance, Currency.USD.code)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = eurBalanceInput,
                    onValueChange = { eurBalanceInput = it.filterDecimalWithMinus() },
                    label = { Text(stringResource(R.string.new_balance, Currency.EUR.code)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        val adjustments = listOf(
                            CurrencyBalance(Currency.UAH, adjustmentUahAmountMinor),
                            CurrencyBalance(Currency.USD, adjustmentUsdAmountMinor),
                            CurrencyBalance(Currency.EUR, adjustmentEurAmountMinor),
                        ).filter { it.amountMinor != 0L }
                        onAdd(adjustments)
                    },
                    buttonText = stringResource(R.string.save_balance),
                    enabled = adjustmentUahAmountMinor != 0L || adjustmentUsdAmountMinor != 0L || adjustmentEurAmountMinor != 0L,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
