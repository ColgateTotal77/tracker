package com.colgateTotal77.tracker.screens.operations.Income

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
import com.colgateTotal77.tracker.core.database.income.IncomeEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.parseInputToMinor
import com.colgateTotal77.tracker.core.ui.AppModalBottomSheet
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.DateTimeInputField
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIncomeModal(
    income: IncomeEntity,
    onDismiss: () -> Unit,
    onUpdate: (amountMinor: Long, currency: Currency, date: Long) -> Unit,
) {
    val dimensions = LocalDimensions.current

    var amountInput by remember(income.id) { mutableStateOf(formatMoney(income.amountMinor)) }
    var currency by remember(income.id) { mutableStateOf(income.currency) }

    val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
    var dateTimeInput by remember { mutableStateOf(formatter.format(Date())) }
    val isDateValid = remember(dateTimeInput) {
        dateTimeInput.length == 12 && try {
            val formatter = SimpleDateFormat("ddMMyyyyHHmm", Locale.getDefault())
            formatter.isLenient = false
            formatter.parse(dateTimeInput) != null
        } catch (e: Exception) {
            false
        }
    }

    var dropdownTouched by remember { mutableStateOf(false) }

    val amountMinor = remember(amountInput) { parseInputToMinor(amountInput) }

    AppModalBottomSheet(onDismissRequest = onDismiss, sheetGesturesEnabled = !dropdownTouched) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(stringResource(R.string.edit_income), style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Dropdown(
                    onTouchChange = { dropdownTouched = it },
                    items = Currency.entries,
                    selected = currency,
                    onSelect = { it?.let { currency = it } },
                    itemText = { it.dropdownText },
                    itemName = stringResource(R.string.currency),
                    modifier = Modifier.fillMaxWidth(),
                )

                DateTimeInputField(
                    value = dateTimeInput,
                    onValueChange = { dateTimeInput = it },
                    isError = dateTimeInput.length == 12 && !isDateValid,
                )

                CustomButton(
                    onClick = {
                        val amount = amountMinor ?: return@CustomButton
                        val date = formatter.parse(dateTimeInput)!!.time
                        onUpdate(amount, currency, date)
                    },
                    buttonText = stringResource(R.string.save_income),
                    enabled = amountMinor != null && isDateValid,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
