package com.colgateTotal77.tracker.screens.operations

import androidx.compose.ui.res.stringResource
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
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteExpenseProductModal(
    expenseProduct: ExpenseProductWithProduct,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val dimensions = LocalDimensions.current

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        CardWrapper(modifier = Modifier.padding(dimensions.screenPadding)) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)) {
                Text(
                    stringResource(R.string.delete_product),
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    "Are you sure that you want to delete \"${expenseProduct.product.alias}\" from this expense?",
                    style = MaterialTheme.typography.bodyMedium,
                )

                CustomButton(
                    onClick = onDelete,
                    buttonText = stringResource(R.string.delete_product),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
