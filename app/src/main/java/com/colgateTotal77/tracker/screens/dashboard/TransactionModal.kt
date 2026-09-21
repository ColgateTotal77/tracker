package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.core.database.transaction.TransactionEntity
import com.colgateTotal77.tracker.core.enums.Currency
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt
import com.colgateTotal77.tracker.core.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionModal(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onUpdate: (amountMinor: Int, currency: Currency, date: Long?) -> Unit,
    onDelete: () -> Unit,
) {
    var amountInput by remember { mutableStateOf(formatMoney(transaction.amountMinor)) }
    var currency by remember { mutableStateOf(transaction.currency) }
    val date = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Input,
        initialSelectedDateMillis = transaction.date
    )
    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper {
            Column {
                Text(
                    "Transaction",
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                    style = MaterialTheme.typography.titleLarge,
                )
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
                DatePicker(
                    state = date,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                Button(
                    onClick = {
                        val amountMinor = ((amountInput.toDoubleOrNull() ?: 0.0) * 100).roundToInt()
                        onUpdate(amountMinor, currency, date.selectedDateMillis)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Transaction")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete")
                }
            }
        }
    }
}