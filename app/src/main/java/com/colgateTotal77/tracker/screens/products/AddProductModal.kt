package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductModal(
    onDismiss: () -> Unit,
    onAdd: (name: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }

    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Text(
                    "Add Product",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Product name") },
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        val name = nameInput.trim()
                        if (name.isNotEmpty()) onAdd(name)
                    },
                    buttonText = "Save Product",
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
