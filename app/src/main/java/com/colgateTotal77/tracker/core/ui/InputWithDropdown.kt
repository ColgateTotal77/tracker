package com.colgateTotal77.tracker.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.colgateTotal77.tracker.core.filterDecimal
import com.colgateTotal77.tracker.core.ui.theme.dimensions

@Composable
fun <T> InputWithDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    selectedInDropdown: T,
    dropdownItems: List<T>,
    onDropdownSelect: (T) -> Unit,
    dropdownItemText: (T) -> String,
    label: String,
    dropdownContentDescription: String,
    modifier: Modifier = Modifier,
) {
    val dimensions = MaterialTheme.dimensions
    var isDropdownExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filterDecimal()) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VerticalDivider(modifier = Modifier.height(dimensions.verticalDividerHeight))

                Box {
                    TextButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier.padding(horizontal = dimensions.iconContainerPadding),
                        shape = RoundedCornerShape(dimensions.cornerRadius),
                    ) {
                        Text(dropdownItemText(selectedInDropdown))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = dropdownContentDescription)
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        dropdownItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(dropdownItemText(item)) },
                                onClick = {
                                    onDropdownSelect(item)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}