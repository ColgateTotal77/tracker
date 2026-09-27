package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductModal(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onUpdate: (alias: String) -> Unit,
) {
    var nameInput by remember { mutableStateOf(product.alias) }

    val dimensions = LocalDimensions.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        CardWrapper {
            Column {
                Text(
                    text = product.alias,
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

                Button(
                    onClick = {
                        val alias = nameInput.trim()
                        if (alias.isNotEmpty()) onUpdate(alias)
                    },
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Save Product")
                }
            }
        }
    }
}
