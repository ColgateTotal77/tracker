package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.ProgressBarPreferences
import com.colgateTotal77.tracker.core.enums.DateFilter
import com.colgateTotal77.tracker.core.filterDecimal
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Change Target",
                modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                style = MaterialTheme.typography.titleLarge,
            )

            TextField(
                value = amountInput,
                onValueChange = { amountInput = it.filterDecimal() },
                placeholder = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.listItemSpacing),
            )

            Dropdown(
                items = DateFilter.entries,
                selected = selectedFilter,
                onSelect = { newFilter ->
                    selectedFilter = newFilter ?: DateFilter.Month
                },
                itemText = { filter -> filter.label },
                itemName = "Time Period",
                modifier = Modifier.padding(bottom = dimensions.listItemSpacing)
            )

            Button(
                onClick = {
                    onProgressBarPreferences(
                        ProgressBarPreferences(
                            (amountInput.toDoubleOrNull() ?: 0.0) * 100,
                            selectedFilter
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Text("Save Progress")
            }
        }
    }
}