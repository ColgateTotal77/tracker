package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductWithProduct
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTransactionProductModal(
    transactionProduct: TransactionProductWithProduct,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val dimensions = LocalDimensions.current

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        CardWrapper {
            Column {
                Text(
                    "Delete Product",
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    "Are you sure that you want to delete \"${transactionProduct.product.alias}\" from this transaction?",
                    modifier = Modifier.padding(bottom = dimensions.listItemSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Delete Product")
                }
            }
        }
    }
}
