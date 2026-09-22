package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.filterDecimal

private fun convertQuantityString(currentValue: String, fromUnit: MeasureUnit, toUnit: MeasureUnit): String {
    if (fromUnit == toUnit) return currentValue
    val value = currentValue.toDoubleOrNull() ?: return currentValue

    val newValue = when (fromUnit) {
        MeasureUnit.G -> value / 1000.0
        MeasureUnit.KG, MeasureUnit.PIECE -> if (toUnit == MeasureUnit.G) value * 1000.0 else value
    }

    return if (newValue % 1 == 0.0) newValue.toInt().toString() else newValue.toString()
}

@Composable
fun QuantityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    selectedUnit: MeasureUnit,
    onUnitChange: (MeasureUnit) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Quantity"
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filterDecimal()) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier,
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VerticalDivider(modifier = Modifier.height(32.dp))

                Box {
                    TextButton(
                        onClick = { unitMenuExpanded = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(selectedUnit.label)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Unit")
                    }

                    DropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false }
                    ) {
                        MeasureUnit.entries.forEach { targetUnit ->
                            DropdownMenuItem(
                                text = { Text(targetUnit.label) },
                                onClick = {
                                    val convertedValue = convertQuantityString(value, selectedUnit, targetUnit)
                                    onValueChange(convertedValue)
                                    onUnitChange(targetUnit)
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}