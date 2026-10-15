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
import com.colgateTotal77.tracker.core.filterDecimalWithMinus
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.formatMoney


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceModal(
    currentBalance: Long,
    onDismiss: () -> Unit,
    onAdd: (adjustmentAmountMinor: Int) -> Unit,
) {
    var newBalance by remember { mutableStateOf(formatMoney(currentBalance)) }
    val adjustmentAmountMinor = remember(newBalance, currentBalance) {
        runCatching {
            newBalance.toBigDecimalOrNull()
                ?.movePointRight(2)
                ?.subtract(currentBalance.toBigDecimal())
                ?.intValueExact()
        }.getOrNull()
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
                    value = newBalance,
                    onValueChange = { newBalance = it.filterDecimalWithMinus() },
                    label = { Text(stringResource(R.string.new_balance)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        val adjustment = adjustmentAmountMinor ?: return@CustomButton
                        onAdd(adjustment)
                    },
                    buttonText = stringResource(R.string.save_balance),
                    enabled = adjustmentAmountMinor != null && adjustmentAmountMinor != 0,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
