package com.colgateTotal77.tracker.screens.overview

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.colgateTotal77.tracker.core.database.expense.ExpenseDao
import com.colgateTotal77.tracker.core.ui.theme.dimensions
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.DayOfWeek
import java.time.YearMonth

data class ChartData(
    val labels: List<String>,
    val values: List<Int>
)

fun buildWeeklyChartData(yearMonth: YearMonth, dailySums: List<ExpenseDao.DailySum>): ChartData {
    val labels = mutableListOf<String>()
    val values = mutableListOf<Int>()

    val daysInMonth = yearMonth.lengthOfMonth()
    var currentWeekStart = 1

    while (currentWeekStart <= daysInMonth) {
        val currentDate = yearMonth.atDay(currentWeekStart)

        var currentWeekEnd = currentWeekStart
        var tempDate = currentDate
        while (tempDate.dayOfWeek != DayOfWeek.SUNDAY && currentWeekEnd < daysInMonth) {
            currentWeekEnd++
            tempDate = yearMonth.atDay(currentWeekEnd)
        }

        val label = if (currentWeekStart == currentWeekEnd) currentWeekStart.toString()
                    else "$currentWeekStart-$currentWeekEnd"
        labels.add(label)

        val weekSumMinor = dailySums
            .filter { it.dayOfMonth in currentWeekStart..currentWeekEnd }
            .sumOf { it.totalAmountMinor }

        values.add((weekSumMinor / 100).toInt())

        currentWeekStart = currentWeekEnd + 1
    }

    return ChartData(labels, values)
}

private val StartAxisValueFormatter = CartesianValueFormatter.decimal(suffix = " \u20b4")

private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ 2.5 })

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = " \u20b4")

@Composable
private fun ComposeDailyDigitalMediaUse(
    modelProducer: CartesianChartModelProducer,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val dimensions = MaterialTheme.dimensions
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider =
                        ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                fill = Fill(MaterialTheme.colorScheme.primary),
                                thickness = 48.dp,
                                shape = RoundedCornerShape(topStart = dimensions.cornerRadius, topEnd = dimensions.cornerRadius),
                            ),
                        ),
                    columnCollectionSpacing = 4.dp,
                    mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                ),
                startAxis =
                    VerticalAxis.rememberStart(
                        line = null,
                        tick = null,
                        guideline = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))),
                        valueFormatter = StartAxisValueFormatter,
                        itemPlacer = StartAxisItemPlacer,
                    ),
                bottomAxis =
                    HorizontalAxis.rememberBottom(
                        line = rememberLineComponent(fill = Fill(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))),
                        tick = null,
                        guideline = null,
                        valueFormatter = { _, index, _ ->
                            labels.getOrElse(index.toInt()) { "" }
                        },
                    ),
                marker = rememberMarker(MarkerValueFormatter),
                layerPadding = { CartesianLayerPadding(scalableStart = 16.dp, scalableEnd = 16.dp) },
                markerController = CartesianMarkerController.rememberToggleOnTap(),
            ),
        modelProducer = modelProducer,
        modifier = modifier,
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}

@Composable
fun ExpensesBarChart(data: ChartData?) {
    if (data == null) return
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnModel { series(data.values) }
        }
    }
    ComposeDailyDigitalMediaUse(modelProducer, data.labels, Modifier)
}

internal val LocalMarkersEnabled = staticCompositionLocalOf { true }

@Composable
internal fun rememberMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter =
        DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
): CartesianMarker? {
    if (!LocalMarkersEnabled.current) return null
    val labelBackgroundShape = MarkerCornerBasedShape(CircleShape)
    val labelBackground =
        rememberShapeComponent(
            fill = Fill(MaterialTheme.colorScheme.background),
            shape = labelBackgroundShape,
            strokeFill = Fill(MaterialTheme.colorScheme.outline),
            strokeThickness = 1.dp,
        )
    val label =
        rememberTextComponent(
            style =
                TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                ),
            padding = Insets(8.dp, 4.dp),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(40.dp),
        )
    val indicatorFrontComponent =
        rememberShapeComponent(Fill(MaterialTheme.colorScheme.surface), CircleShape)
    val guideline = rememberAxisGuidelineComponent()
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator =
            if (showIndicator) {
                { color ->
                    LayeredComponent(
                        back = ShapeComponent(Fill(color.copy(alpha = 0.15f)), CircleShape),
                        front =
                            LayeredComponent(
                                back = ShapeComponent(fill = Fill(color), shape = CircleShape),
                                front = indicatorFrontComponent,
                                padding = Insets(5.dp),
                            ),
                        padding = Insets(10.dp),
                    )
                }
            } else {
                null
            },
        indicatorSize = 36.dp,
        guideline = guideline,
    )
}
