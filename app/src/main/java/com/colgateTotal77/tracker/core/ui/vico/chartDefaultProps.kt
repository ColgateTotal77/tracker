package com.colgateTotal77.tracker.core.ui.vico

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun startAxis(
    valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
): VerticalAxis<Axis.Position.Vertical.Start> {
    return VerticalAxis.rememberStart(
        line = null,
        tick = null,
        guideline = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ),
        valueFormatter = valueFormatter,
        itemPlacer = VerticalAxis.ItemPlacer.step({ 2.5 }),
    )
}

@Composable
fun bottomAxis(
    valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal(),
    addExtremeLabelPadding: Boolean = false,
    label: TextComponent = rememberAxisLabelComponent(),
): HorizontalAxis<Axis.Position.Horizontal.Bottom> {
    return HorizontalAxis.rememberBottom(
        line = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ),
        label = label,
        tick = null,
        guideline = null,
        valueFormatter = valueFormatter,
        itemPlacer = remember(addExtremeLabelPadding) {
            val base = HorizontalAxis.ItemPlacer.aligned(addExtremeLabelPadding = addExtremeLabelPadding)
            object : HorizontalAxis.ItemPlacer by base {
                override fun getLabelValues(
                    context: CartesianDrawingContext,
                    visibleXRange: ClosedFloatingPointRange<Double>,
                    fullXRange: ClosedFloatingPointRange<Double>,
                    maxLabelWidth: Float,
                ) = base.getLabelValues(context, visibleXRange, fullXRange, maxLabelWidth).filter { it >= 0 }

                override fun getWidthMeasurementLabelValues(
                    context: CartesianMeasuringContext,
                    layerDimensions: CartesianLayerDimensions,
                    fullXRange: ClosedFloatingPointRange<Double>,
                ) = base.getWidthMeasurementLabelValues(context, layerDimensions, fullXRange).filter { it >= 0 }

                override fun getHeightMeasurementLabelValues(
                    context: CartesianMeasuringContext,
                    layerDimensions: CartesianLayerDimensions,
                    fullXRange: ClosedFloatingPointRange<Double>,
                    maxLabelWidth: Float,
                ) = base.getHeightMeasurementLabelValues(context, layerDimensions, fullXRange, maxLabelWidth).filter { it >= 0 }
            }
        }
    )
}