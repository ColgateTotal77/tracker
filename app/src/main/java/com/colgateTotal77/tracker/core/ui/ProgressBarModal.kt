package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressBarModal(
    onDismiss: () -> Unit,
    onChangeProgress: (amount: Double) -> Unit,
) {
    var amountInput by remember { mutableStateOf("") }
    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column {
            Text(
                "Change Target",
                modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                style = MaterialTheme.typography.titleLarge,
            )
            TextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                placeholder = { Text("Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimensions.listItemSpacing),
            )
            Button(
                onClick = { onChangeProgress(amountInput.toDoubleOrNull() ?: 0.0) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save Progress")
            }
        }
    }
}