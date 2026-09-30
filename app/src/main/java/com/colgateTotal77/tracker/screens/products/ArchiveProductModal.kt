package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.colgateTotal77.tracker.core.database.product.ProductEntity
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveProductModal(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onArchive: (id: Int) -> Unit,
) {
    val dimensions = LocalDimensions.current

    Dialog(onDismissRequest = onDismiss) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)) {
                Text(
                    "Archive Product",
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    "Are you sure that you want to archive “${product.alias}”?",
                    style = MaterialTheme.typography.bodyMedium,
                )

                CustomButton(
                    onClick = { onArchive(product.id) },
                    buttonText = "Archive Product",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
