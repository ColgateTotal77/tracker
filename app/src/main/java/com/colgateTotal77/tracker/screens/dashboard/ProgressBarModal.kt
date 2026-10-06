package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.enums.DateFilter
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.Dropdown
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressBarModal(
    settings: ProgressBarPreferences,
    onDismiss: () -> Unit,
    onProgressBarPreferences: (settings: ProgressBarPreferences) -> Unit,
) {
    var amountInput by remember { mutableStateOf(if (settings.targetBudget > 0) (settings.targetBudget / 100).toString() else "") }
    var selectedFilter by remember { mutableStateOf(settings.selectedFilter) }

    val dimensions = LocalDimensions.current
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Text(
                    stringResource(R.string.change_target),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filterDecimal() },
                    label = { Text(stringResource(R.string.amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Dropdown(
                    items = DateFilter.entries,
                    selected = selectedFilter,
                    onSelect = { newFilter ->
                        selectedFilter = newFilter ?: DateFilter.Month
                    },
                    itemText = { filter -> context.getString(filter.labelRes) },
                    itemName = stringResource(R.string.time_period),
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        onProgressBarPreferences(
                            ProgressBarPreferences(
                                (amountInput.toDoubleOrNull() ?: 0.0) * 100,
                                selectedFilter
                            )
                        )
                    },
                    buttonText = stringResource(R.string.save_progress),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
