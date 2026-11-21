package com.colgateTotal77.tracker.screens.overview

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.database.expense.ExpenseDao
import com.colgateTotal77.tracker.core.ui.theme.dimensions
import com.colgateTotal77.tracker.core.ui.vico.bottomAxis
import com.colgateTotal77.tracker.core.ui.vico.rememberMarker
import com.colgateTotal77.tracker.core.ui.vico.startAxis
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class ChartData(
    val labels: List<String>,
    val values: List<Int>
)

fun buildWeeklyChartData(weeklySums: List<ExpenseDao.WeeklySum>): ChartData {
    if (weeklySums.isEmpty()) return ChartData(emptyList(), emptyList())

    val labels = mutableListOf<String>()
    val values = mutableListOf<Int>()

    val sumByWeek = weeklySums.associate { LocalDate.parse(it.weekStartDate) to it.totalAmountMinor }
    val firstWeek = sumByWeek.keys.min();
    val lastWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    var previousMonth = -1
    val monthDayFormatter = DateTimeFormatter.ofPattern("MMM. dd")
    val dayFormatter = DateTimeFormatter.ofPattern("dd")

    var weekStart = firstWeek
    while(weekStart <= lastWeek) {
        val weekEnd = weekStart.plusDays(6)

        val currentMonth = weekStart.monthValue

        if (currentMonth != previousMonth) labels.add(weekStart.format(monthDayFormatter))
        else labels.add("${weekStart.format(dayFormatter)} - ${weekEnd.format(dayFormatter)}")
        values.add(((sumByWeek[weekStart] ?: 0) / 100).toInt())

        previousMonth = currentMonth
        weekStart = weekStart.plusWeeks(1)
    }

    return ChartData(labels, values)
}
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter { context, targets ->
    val realValues = context.model.extraStore[RealValuesKey]
    "${realValues[targets.first().x.toInt()]} \u20b4"
}

private val RealValuesKey = ExtraStore.Key<List<Int>>()
private val MaxValueKey = ExtraStore.Key<Int>()

@Composable
private fun ComposeWeeklySpendingBarChart(
    modelProducer: CartesianChartModelProducer,
    data: ChartData,
    weeklyTarget: Double,
    modifier: Modifier = Modifier,
) {
    val dimensions = MaterialTheme.dimensions

    val normalColumn = rememberLineComponent(fill = Fill(
        Brush.verticalGradient(
            0.2f to MaterialTheme.colorScheme.primary,
            1f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )),
        thickness = 94.dp,
        shape = RoundedCornerShape(
            topStart = dimensions.cornerRadius,
            topEnd = dimensions.cornerRadius
        ),
    )

    val clampedColumn = rememberLineComponent(fill = Fill(
        Brush.verticalGradient(
            0f to MaterialTheme.colorScheme.error,
            0.20f to MaterialTheme.colorScheme.primary,
            1f to MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )),
        thickness = 94.dp,
        shape = RoundedCornerShape(
            topStart = dimensions.cornerRadius,
            topEnd = dimensions.cornerRadius
        ),
    )

    val columnProvider = remember(normalColumn, clampedColumn) {
        object : ColumnCartesianLayer.ColumnProvider {
            override fun getColumn(
                entry: ColumnCartesianLayerModel.Entry,
                extraStore: ExtraStore
            ): LineComponent {
                return if (extraStore[RealValuesKey][entry.x.toInt()] > extraStore[MaxValueKey]) clampedColumn
                       else normalColumn
            }

            override fun getWidestSeriesColumn(seriesKey: Any, seriesIndex: Int, extraStore: ExtraStore) = normalColumn
        }
    }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart =
                rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = columnProvider,
                        columnCollectionSpacing = 6.dp,
                        mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
                        rangeProvider = remember(weeklyTarget) {
                            object : CartesianLayerRangeProvider {
                                override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 0.0
                                override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                                    maxOf(maxY, weeklyTarget) * 1.1
                            }
                        },
                    ),
                    startAxis = startAxis(
                        valueFormatter = CartesianValueFormatter.decimal(suffix = " \u20b4"),
                    ),
                    bottomAxis = bottomAxis(
                        valueFormatter = { _, index, _ ->
                            data.labels.getOrElse(index.toInt()) { "" }
                        },
                    ),
                    marker = rememberMarker(MarkerValueFormatter),
                    markerController = CartesianMarkerController.rememberToggleOnTap(),
                    decorations = if (weeklyTarget > 0)
                        listOf(
                            HorizontalLine(
                                y = { weeklyTarget },
                                line = LineComponent(
                                    fill = Fill(MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                                    thickness = 2.dp,
                                ),
                            )
                        )
                    else emptyList(),
                ),
            modelProducer = modelProducer,
            modifier = modifier,
            zoomState = rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = remember { Zoom.x(6.0) },
            ),
            scrollState = rememberVicoScrollState(
                scrollEnabled = true,
                initialScroll = Scroll.Absolute.End,
            ),
        )
    }
}

@Composable
fun WeeklySpendingBarChart(
    data: ChartData,
    weeklyTarget: Double,
    modifier: Modifier = Modifier
) {
    if (data.values.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val maxValue = data.values.takeLast(6).max()

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnModel { series(data.values.map { minOf(it, maxValue) }) }
            extras {
                it[RealValuesKey] = data.values
                it[MaxValueKey] = maxValue
            }
        }
    }
    ComposeWeeklySpendingBarChart(modelProducer, data, weeklyTarget, modifier)
}