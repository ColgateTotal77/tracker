package com.colgateTotal77.tracker.screens.products

import androidx.compose.ui.res.stringResource
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
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
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
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Text(
                    text = product.alias,
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                CustomButton(
                    onClick = {
                        val alias = nameInput.trim()
                        if (alias.isNotEmpty()) onUpdate(alias)
                    },
                    buttonText = stringResource(R.string.save_product),
                    enabled = nameInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
