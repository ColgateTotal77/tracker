package com.colgateTotal77.tracker.screens.operations

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.database.expense.ExpenseWithProducts
import com.colgateTotal77.tracker.core.database.expense_product.ExpenseProductWithProduct
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.formatTimestamp
import com.colgateTotal77.tracker.core.getMeasureUnit
import com.colgateTotal77.tracker.core.ui.ActionDropdownMenu
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
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
fun ExpenseCardExpandable(
    expenseItem: ExpenseWithProducts,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddExpenseProduct: (nextPosition: Int) -> Unit,
    onEditExpenseProduct: (expenseProduct: ExpenseProductWithProduct) -> Unit,
    onDeleteExpenseProduct: (expenseProduct: ExpenseProductWithProduct) -> Unit,
) {
    val dimensions = LocalDimensions.current
    val expense = expenseItem.expense

    CardWrapper(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cornerRadius))
                    .clickable { onExpandedChange() }
                    .padding(dimensions.contentPadding),
                horizontalArrangement = Arrangement.spacedBy(dimensions.spacingMedium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensions.leadingIconSize)
                        .clip(RoundedCornerShape(dimensions.cornerRadius))
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
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = expenseItem.market?.name.orEmpty().ifBlank { "Receipt" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row {
                        Text(
                            text = formatTimestamp(expense.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = stringResource(expense.source.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "-${formatMoney(expense.amountMinor)} ${expense.currency.name}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ActionDropdownMenu { onCloseMenu ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit)) },
                        onClick = {
                            onCloseMenu()
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            onCloseMenu()
                            onDelete()
                        }
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    expenseItem.items.forEach { item ->
                        ExpenseProductCard(
                            item,
                            onEdit = { onEditExpenseProduct(item) },
                            onDelete = { onDeleteExpenseProduct(item) }
                        )
                    }

                    CustomButton(
                        onClick = {
                            val nextPosition = (expenseItem.items.maxOfOrNull { it.expenseProduct.position } ?: -1) + 1
                            onAddExpenseProduct(nextPosition)
                        },
                        buttonText = stringResource(R.string.add_product),
                        icon = Icons.Rounded.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
