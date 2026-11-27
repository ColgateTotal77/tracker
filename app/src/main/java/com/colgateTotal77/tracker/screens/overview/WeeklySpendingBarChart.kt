package com.colgateTotal77.tracker.screens.overview

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.enums.DateSwitchers
import com.colgateTotal77.tracker.core.enums.TimeRange
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.colgateTotal77.tracker.core.ui.vico.BarChart
import com.colgateTotal77.tracker.core.ui.vico.DailySum
import com.colgateTotal77.tracker.core.ui.vico.LabelsKey
import com.colgateTotal77.tracker.core.ui.vico.MaxValueKey
import com.colgateTotal77.tracker.core.ui.vico.ValuesKey
import com.colgateTotal77.tracker.core.ui.vico.buildChartData
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel

@Composable
fun SpendingBarChart(
    data: List<DailySum>,
    monthlyTarget: Double,
    onBarLongPress: ((TimeRange) -> Unit),
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val dimensions = LocalDimensions.current

    var selectedDateSwitcher by remember { mutableStateOf(DateSwitchers.Week) }
    var target by remember(selectedDateSwitcher) { mutableDoubleStateOf(
        when (selectedDateSwitcher) {
            DateSwitchers.Day -> monthlyTarget * 12 / 364
            DateSwitchers.Week -> monthlyTarget * 12 / 52
            DateSwitchers.Month -> monthlyTarget
        }
    ) }

    val chartData = buildChartData(data, selectedDateSwitcher)

    val modelProducer = remember { CartesianChartModelProducer() }
    val maxValue = chartData.values.takeLast(if (selectedDateSwitcher == DateSwitchers.Day) 120 else 6).max()

    LaunchedEffect(data, selectedDateSwitcher, target) {
        modelProducer.runTransaction {
            columnModel { series(chartData.values.map { minOf(it, maxValue) }) }
            extras {
                it[ValuesKey] = chartData.values
                it[LabelsKey] = chartData.labels
                it[MaxValueKey] = maxValue
            }
        }
    }

    Log.d("Temp", chartData.intervals.toString())

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            DateSwitchers.entries.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = selectedDateSwitcher.ordinal == index,
                    onClick = { selectedDateSwitcher = DateSwitchers.entries[index] },
                    shape = SegmentedButtonDefaults.itemShape(
                        index,
                        DateSwitchers.entries.size,
                        RoundedCornerShape(dimensions.cornerRadius)
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                    )
                ) {
                    Text(stringResource(value.labelRes))
                }
            }
        }
    }

    BarChart(
        modelProducer,
        modifier,
        { index -> onBarLongPress(chartData.intervals[index]) },
        target
    )
}