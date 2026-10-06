package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductWithProduct
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomIconButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions

@Composable
fun TransactionProductCard(
    item: TransactionProductWithProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dimensions = LocalDimensions.current
    val baseQuantityText = "${formatProductMeasure(item.transactionProduct.quantity, item.product.alias)} × ${formatMoney(item.transactionProduct.unitPriceMinor)}"

    CardWrapper {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensions.elementSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)
            ) {
                Text(
                    text = item.product.alias,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (item.transactionProduct.isManuallyCreated) "$baseQuantityText • Created manually" else baseQuantityText,                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatMoney(item.transactionProduct.totalMinor),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(dimensions.elementSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.edit_transaction_product),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                CustomIconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_transaction_product),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
