package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.QuantityInputField
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionProductModal(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Int, unitPriceMinor: Int) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }
    var unitPriceInput by remember { mutableStateOf("") }

    var selectedUnit by remember { mutableStateOf(MeasureUnit.PIECE) }
    var quantityInput by remember { mutableStateOf("1") }

    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper {
            Column {
                Text(
                    "Add Product",
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Product name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )

                QuantityInputField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    selectedUnit = selectedUnit,
                    onUnitChange = { selectedUnit = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )

                OutlinedTextField(
                    value = unitPriceInput,
                    onValueChange = { unitPriceInput = it.filterDecimal() },
                    label = { Text("Unit price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = dimensions.listItemSpacing),
                )
                Button(
                    onClick = {
                        val name = nameInput.trim()
                        val rawQuantity = quantityInput.toDoubleOrNull() ?: return@Button
                        val unitPriceMinor = ((unitPriceInput.toDoubleOrNull() ?: return@Button) * 100).roundToInt()

                        val quantity =
                            if (selectedUnit != MeasureUnit.G) (rawQuantity * 1000).roundToInt()
                            else rawQuantity.roundToInt()

                        if (name.isEmpty() || quantity <= 0 || unitPriceMinor <= 0) return@Button

                        onAdd(name, quantity, unitPriceMinor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Product")
                }
            }
        }
    }
}
