package com.colgateTotal77.tracker.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.database.transaction.TransactionWithProducts
import com.colgateTotal77.tracker.core.database.transaction_product.TransactionProductWithProduct
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.formatTimestamp
import com.colgateTotal77.tracker.core.getMeasureUnit
import com.colgateTotal77.tracker.core.ui.ActionDropdownMenu
import com.colgateTotal77.tracker.core.ui.CardWrapper
import java.text.DecimalFormat

fun formatProductMeasure(quantity: Int, name: String): String {
    val df = DecimalFormat("#.###")

    val measureUnit = getMeasureUnit(quantity, name)

    return when (measureUnit) {
        MeasureUnit.PIECE -> "${quantity / 1000}x"
        MeasureUnit.KG -> "${df.format(quantity / 1000.0)} kg"
        MeasureUnit.G -> "$quantity g"
    }
}

@Composable
fun TransactionCard(
    transactionItem: TransactionWithProducts,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddTransactionProduct: (nextPosition: Int) -> Unit,
    onEditTransactionProduct: (transactionProduct: TransactionProductWithProduct) -> Unit,
    onDeleteTransactionProduct: (transactionProduct: TransactionProductWithProduct) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val transaction = transactionItem.transaction

    CardWrapper {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = transactionItem.market?.name.orEmpty().ifBlank { "Receipt" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = transaction.source.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatTimestamp(transaction.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "-${formatMoney(transaction.amountMinor)} ${transaction.currency.name}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ActionDropdownMenu { onCloseMenu ->
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onCloseMenu()
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onCloseMenu()
                            onDelete()
                        }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    transactionItem.items.forEach { item ->
                        TransactionProductCard(
                            item,
                            onEdit = { onEditTransactionProduct(item) },
                            onDelete = { onDeleteTransactionProduct(item) }
                        )
                    }

                    AddTransactionProductButton(
                        onClick = {
                            val nextPosition = (transactionItem.items.maxOfOrNull { it.transactionProduct.position } ?: -1) + 1
                            onAddTransactionProduct(nextPosition)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}