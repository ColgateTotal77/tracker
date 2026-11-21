package com.colgateTotal77.tracker.screens.products

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.MeasureUnit
import com.colgateTotal77.tracker.core.database.product.PricePoint
import com.colgateTotal77.tracker.core.formatMoney
import com.colgateTotal77.tracker.core.ui.vico.bottomAxis
import com.colgateTotal77.tracker.core.ui.vico.rememberMarker
import com.colgateTotal77.tracker.core.ui.vico.startAxis
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ProductChart(pricePoints: List<PricePoint>, measureUnit: MeasureUnit) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(pricePoints) {
        modelProducer.runTransaction {
            lineModel {
                series(pricePoints.map { it.unitPriceMinor.toDouble() / 100.0 })
            }
        }
    }

    val dateTimeFormatter = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val bottomAxisFormatter = remember(pricePoints) {
        CartesianValueFormatter { _, value, _ ->
            pricePoints.getOrNull(value.toInt())?.date?.let { dateLong ->
                dateTimeFormatter.format(Date(dateLong))
            } ?: ""
        }
    }

    val markerValueFormatter = remember(pricePoints) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            val entry =
                (targets.firstOrNull() as? LineCartesianLayerMarkerTarget)?.points?.firstOrNull()?.entry
                    ?: return@ValueFormatter ""

            val point = pricePoints.getOrNull(entry.x.toInt())
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

    val maxY = remember(pricePoints) {
        (pricePoints.maxOfOrNull { it.unitPriceMinor } ?: 0) * 1.1 / 100.0
    }

    val scrollState =  rememberVicoScrollState(
        scrollEnabled = true,
        initialScroll = Scroll.Absolute.End,
    )

    val decorationToFindXStart = remember { object : Decoration {
        var plotLeft = 0f
        var plotRight = 0f

        override fun drawUnderLayers(context: CartesianDrawingContext) {
            plotLeft = context.layerBounds.left
            plotRight = context.layerBounds.right
        }
    } }

    ProvideVicoTheme(rememberM3VicoTheme()) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxY)
                ),
                startAxis = startAxis(CartesianValueFormatter.decimal(suffix = " \u20b4")),
                bottomAxis = bottomAxis(bottomAxisFormatter, true),
                marker = rememberMarker(valueFormatter = markerValueFormatter),
                decorations = listOf(decorationToFindXStart)
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(scrollState) {
                    coroutineScope {
                        var scrollJob: Job? = null
                        val zoneWidth = 30.dp.toPx()
                        val step = 12f

                        awaitPointerEventScope {
                            while (true) {
                                val changes =
                                    awaitPointerEvent(PointerEventPass.Initial).changes.first()

                                val atLeftEdge = changes.pressed && changes.position.x < decorationToFindXStart.plotLeft + zoneWidth
                                val atRightEdge = changes.pressed && changes.position.x > decorationToFindXStart.plotRight - zoneWidth
                                val direction = when { atLeftEdge -> -1; atRightEdge -> 1; else -> 0 }

                                if (scrollJob == null && direction != 0) scrollJob = this@coroutineScope.launch {
                                    while (true) {
                                        scrollState.scroll(Scroll.Relative.pixels(step * direction))
                                        delay(16.milliseconds)
                                    }
                                } else if (direction == 0) {
                                    scrollJob?.cancel()
                                    scrollJob = null;
                                }

                                if (changes.positionChanged()) changes.consume()
                            }
                        }
                    }
                },
            scrollState = scrollState,
        )
    }
}