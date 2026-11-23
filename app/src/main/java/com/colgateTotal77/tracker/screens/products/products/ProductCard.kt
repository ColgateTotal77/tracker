package com.colgateTotal77.tracker.screens.products.products

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
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ShoppingBag
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.colgateTotal77.tracker.R
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.database.product.ProductDao.PricePoint
import com.colgateTotal77.tracker.core.database.product.ProductFromQuery
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.getMeasureUnit
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.DecimalFormat

fun formatProductMeasure(measureUnit: MeasureUnit, quantity: Int): String {
    val df = DecimalFormat("#.###")
    return when (measureUnit) {
        MeasureUnit.PIECE -> "Bought ${quantity / 1000}x times"
        MeasureUnit.KG -> "${df.format(quantity / 1000.0)} kg"
        MeasureUnit.G -> "$quantity g"
    }
}

fun formatChartProductMeasure(measureUnit: MeasureUnit, quantity: Int): String {
    val df = DecimalFormat("#.###")
    return when (measureUnit) {
        MeasureUnit.PIECE -> "×${quantity / 1000}"
        MeasureUnit.KG -> "${df.format(quantity / 1000.0)} kg"
        MeasureUnit.G -> "$quantity g"
    }
}

@Composable
fun ProductCard(
    productFromQuery: ProductFromQuery,
    onGetProductHistory: (id: Int) -> Flow<List<PricePoint>>,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
) {
    val product = productFromQuery.product
    var expanded by remember { mutableStateOf(false) }
    val dimensions = LocalDimensions.current

    val pricePoints by remember(expanded, product.id) {
        if (expanded) onGetProductHistory(product.id)
        else flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val measureUnit = getMeasureUnit(product.purchaseCount, product.alias)

    CardWrapper(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cornerRadius))
                    .clickable { expanded = !expanded }
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
                        imageVector = Icons.Rounded.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = product.alias,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatProductMeasure(measureUnit, product.purchaseCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    Text(
                        text = formatMoney(product.lastPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatMoney(productFromQuery.spentMinor),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensions.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacingSmall)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (pricePoints.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.price_history),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ProductChart(pricePoints, measureUnit);
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacingSmall),
                        modifier = Modifier.padding(top = dimensions.spacingSmall)
                    ) {
                        CustomButton(
                            onClick = onEdit,
                            buttonText = stringResource(R.string.edit_product),
                            icon = Icons.Rounded.Edit,
                            modifier = Modifier.weight(1f)
                        )

                        CustomButton(
                            onClick = onArchive,
                            buttonText = stringResource(R.string.archive_product),
                            icon = Icons.Rounded.Archive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
