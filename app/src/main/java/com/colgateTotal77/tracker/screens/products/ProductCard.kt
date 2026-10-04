package com.colgateTotal77.tracker.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.LaunchedEffect
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
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.database.product.PricePoint
import com.colgateTotal77.tracker.core.database.product.ProductFromQuery
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.getMeasureUnit
import com.colgateTotal77.tracker.core.ui.CardWrapper
import com.colgateTotal77.tracker.core.ui.CustomButton
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget

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

    val history by remember(expanded, product.id) {
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
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemSpacing),
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
                    verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)
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
                    verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)
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
                    verticalArrangement = Arrangement.spacedBy(dimensions.elementSpacing)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (history.isNotEmpty()) {
                        Text(
                            text = "Price History",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val modelProducer = remember { CartesianChartModelProducer() }
                        LaunchedEffect(history) {
                            modelProducer.runTransaction {
                                lineSeries {
                                    series(history.map { it.unitPriceMinor.toDouble() / 100.0 })
                                }
                            }
                        }

                        val dateTimeFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                        val bottomAxisFormatter = remember(history) {
                            CartesianValueFormatter { _, value, _ ->
                                history.getOrNull(value.toInt())?.date?.let { dateLong ->
                                    dateTimeFormatter.format(Date(dateLong))
                                } ?: ""
                            }
                        }

                        val markerLabel = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
                        val markerValueFormatter = remember(history) {
                            DefaultCartesianMarker.ValueFormatter { _, targets ->
                                val entry =
                                    (targets.firstOrNull() as? LineCartesianLayerMarkerTarget)?.points?.firstOrNull()?.entry
                                        ?: return@ValueFormatter ""

                                val point = history.getOrNull(entry.x.toInt())
                                buildString {
                                    append(formatMoney((entry.y * 100).toInt()))
                                    if (point == null) return@buildString
                                    append(" · ")
                                    append(formatChartProductMeasure(measureUnit, point.quantity))
                                    append(" · ")
                                    append(dateTimeFormatter.format(Date(point.date)))
                                }
                            }
                        }
                        val marker = rememberDefaultCartesianMarker(
                            label = markerLabel,
                            valueFormatter = markerValueFormatter,
                        )

                        val maxY = remember(history) {
                            (history.maxOfOrNull { it.unitPriceMinor } ?: 0) * 1.1 / 100.0
                        }

                        ProvideVicoTheme(theme = rememberM3VicoTheme()) {
                            CartesianChartHost(
                                chart = rememberCartesianChart(
                                    rememberLineCartesianLayer(
                                        rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxY)
                                    ),
                                    startAxis = VerticalAxis.rememberStart(),
                                    bottomAxis = HorizontalAxis.rememberBottom(
                                        valueFormatter = bottomAxisFormatter,
                                    ),
                                    marker = marker,
                                ),
                                modelProducer = modelProducer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensions.elementSpacing),
                        modifier = Modifier.padding(top = dimensions.elementSpacing)
                    ) {
                        CustomButton(
                            onClick = onEdit,
                            buttonText = "Edit Product",
                            icon = Icons.Rounded.Edit,
                            modifier = Modifier.weight(1f)
                        )

                        CustomButton(
                            onClick = onArchive,
                            buttonText = "Archive Product",
                            icon = Icons.Rounded.Archive,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}