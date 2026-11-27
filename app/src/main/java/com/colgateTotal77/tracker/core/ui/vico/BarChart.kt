package com.colgateTotal77.tracker.core.ui.vico

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.enums.DateSwitchers
import com.colgateTotal77.tracker.core.enums.TimeRange
import com.colgateTotal77.tracker.core.enums.monthYearFormatter
import com.colgateTotal77.tracker.core.enums.shortDateFormatter
import com.colgateTotal77.tracker.core.enums.weekFormatter
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DailySum(
    val day: String,
    val amount: Long,
)

fun buildChartData(dailySums: List<DailySum>, dateSwitchers: DateSwitchers): ChartData {
    fun formatter(date: LocalDate): String {
        return when (dateSwitchers) {
            DateSwitchers.Day -> date.format(shortDateFormatter)
            DateSwitchers.Week -> date.weekFormatter()
            DateSwitchers.Month -> date.format(monthYearFormatter)
        }
    }

    fun getNextInterval(date: LocalDate): LocalDate {
        return when (dateSwitchers) {
            DateSwitchers.Day -> date.plusDays(1)
            DateSwitchers.Week -> date.plusWeeks(1)
            DateSwitchers.Month -> date.plusMonths(1)
        }
    }

    fun getIntervalStart(date: LocalDate): LocalDate {
        return when (dateSwitchers) {
            DateSwitchers.Day -> date
            DateSwitchers.Week -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            DateSwitchers.Month -> date.withDayOfMonth(1)
        }
    }

    val sumByInterval: MutableMap<String, Long> = mutableMapOf()

    dailySums.forEach {
        val key = formatter(LocalDate.parse(it.day))

        if (key in sumByInterval) sumByInterval[key] = sumByInterval[key]!! + it.amount
        else sumByInterval[key] = it.amount
    }

    val today = LocalDate.now()

    val labels = mutableListOf<String>()
    val values = mutableListOf<Long>()
    val intervals = mutableListOf<TimeRange>()
    var intervalStart = getIntervalStart(LocalDate.parse(dailySums.first().day))

    while (intervalStart <= today) {
        val label = formatter(intervalStart)

        values.add((sumByInterval[label] ?: 0L) / 100)
        labels.add(label)
        intervals.add(TimeRange(start = intervalStart, end = getNextInterval(intervalStart).minusDays(1)))

        intervalStart = getNextInterval(intervalStart)
    }

    return ChartData(labels, values, intervals)
}

data class ChartData(
    val labels: List<String>,
    val values: List<Long>,
    val intervals: List<TimeRange>
)

val ValuesKey = ExtraStore.Key<List<Long>>()
val LabelsKey = ExtraStore.Key<List<String>>()
val MaxValueKey = ExtraStore.Key<Long>()

@Composable
fun BarChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    onBarLongPress: ((Int) -> Unit)? = null,
    targetLineValue: Double? = null,
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter = MarkerValueFormatter,
) {
    val dimensions = LocalDimensions.current

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
                return if (extraStore[ValuesKey][entry.x.toInt()] > extraStore[MaxValueKey]) clampedColumn
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
                        rangeProvider = remember(targetLineValue) {
                            object : CartesianLayerRangeProvider {
                                override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) = 0.0
                                override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
                                    maxOf(maxY, targetLineValue ?: 0.0) * 1.1
                                override fun getMinX(minX: Double, maxX: Double, extraStore: ExtraStore) =
                                    minOf(minX, maxX - 5)
                            }
                        },
                    ),
                    startAxis = startAxis(
                        valueFormatter = CartesianValueFormatter.decimal(suffix = " \u20b4"),
                    ),
                    bottomAxis = bottomAxis(
                        valueFormatter = { context, index, _ ->
                            context.model.extraStore[LabelsKey][index.toInt()]
                        },
                        label = rememberAxisLabelComponent(lineCount = 2)

                    ),
                    marker = rememberMarker(markerValueFormatter),
                    markerController = rememberToggleOnTapLongPress(onLongPress = { index -> onBarLongPress?.invoke(index) }),
                    decorations = if (targetLineValue != null)
                        listOf(
                            HorizontalLine(
                                y = { targetLineValue },
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
                minZoom = remember { Zoom.x(6.0) },
                maxZoom = remember { Zoom.x(6.0) },
            ),
            scrollState = rememberVicoScrollState(
                scrollEnabled = true,
                initialScroll = Scroll.Absolute.End,
                autoScroll = Scroll.Absolute.End,
                autoScrollCondition = AutoScrollCondition.OnModelGrowth
            ),
        )
    }
}
