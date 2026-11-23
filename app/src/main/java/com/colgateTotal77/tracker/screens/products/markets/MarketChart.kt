package com.colgateTotal77.tracker.screens.products.markets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.colgateTotal77.tracker.core.ui.vico.BarChart
import com.colgateTotal77.tracker.core.ui.vico.ChartData
import com.colgateTotal77.tracker.core.ui.vico.MaxValueKey
import com.colgateTotal77.tracker.core.ui.vico.RealValuesKey
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker

private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter { context, targets ->
    val realValues = context.model.extraStore[RealValuesKey]
    "${realValues[targets.first().x.toInt()]} \u20b4"
}

@Composable
fun MarketBarChart(
    data: ChartData,
    modifier: Modifier = Modifier
) {
    if (data.values.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnModel { series(data.values) }
            extras {
                it[RealValuesKey] = data.values
                it[MaxValueKey] = data.values.max()
            }
        }
    }
    BarChart(modelProducer, data, MarkerValueFormatter, modifier)
}