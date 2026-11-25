package com.colgateTotal77.tracker.screens.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.core.database.expense.ExpenseDao
import com.colgateTotal77.tracker.core.enums.dayFormatter
import com.colgateTotal77.tracker.core.enums.monthShortFormatter
import com.colgateTotal77.tracker.core.ui.vico.BarChart
import com.colgateTotal77.tracker.core.ui.vico.ChartData
import com.colgateTotal77.tracker.core.ui.vico.MaxValueKey
import com.colgateTotal77.tracker.core.ui.vico.RealValuesKey
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

fun buildWeeklyChartData(weeklySums: List<ExpenseDao.WeeklySum>): ChartData {
    if (weeklySums.isEmpty()) return ChartData(emptyList(), emptyList())

    val labels = mutableListOf<String>()
    val values = mutableListOf<Int>()

    val sumByWeek = weeklySums.associate { LocalDate.parse(it.weekStartDate) to it.totalAmountMinor }
    val firstWeek = sumByWeek.keys.min();
    val lastWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    var previousMonth = -1

    var weekStart = firstWeek
    while(weekStart <= lastWeek) {
        val weekEnd = weekStart.plusDays(6)

        val currentMonth = weekStart.monthValue

        val range = "${weekStart.format(dayFormatter)} - ${weekEnd.format(dayFormatter)}"
        if (currentMonth != previousMonth) labels.add("$range\n${weekStart.format(monthShortFormatter)}")
        else labels.add(range)

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
    BarChart(modelProducer, data, MarkerValueFormatter, modifier, weeklyTarget)
}