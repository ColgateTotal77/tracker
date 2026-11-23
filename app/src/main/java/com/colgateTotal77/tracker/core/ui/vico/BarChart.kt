package com.colgateTotal77.tracker.core.ui.vico

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.colgateTotal77.tracker.core.ui.theme.LocalDimensions
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Scroll
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
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

data class ChartData(
    val labels: List<String>,
    val values: List<Int>
)

val RealValuesKey = ExtraStore.Key<List<Int>>()
val MaxValueKey = ExtraStore.Key<Int>()

@Composable
fun BarChart(
    modelProducer: CartesianChartModelProducer,
    data: ChartData,
    markerValueFormatter: DefaultCartesianMarker.ValueFormatter,
    modifier: Modifier = Modifier,
    targetLineValue: Double? = null,
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
                        valueFormatter = { _, index, _ ->
                            data.labels.getOrElse(index.toInt()) { "" }
                        },
                    ),
                    marker = rememberMarker(markerValueFormatter),
                    markerController = CartesianMarkerController.rememberToggleOnTap(),
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
            ),
        )
    }
}
