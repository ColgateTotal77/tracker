package com.colgateTotal77.tracker.core.ui.vico

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun startAxis(
    valueFormatter: CartesianValueFormatter =
        CartesianValueFormatter.decimal(),
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
    valueFormatter: CartesianValueFormatter =
        CartesianValueFormatter.decimal(),
    addExtremeLabelPadding: Boolean = false
): HorizontalAxis<Axis.Position.Horizontal.Bottom> {
    return HorizontalAxis.rememberBottom(
        line = rememberLineComponent(
            fill = Fill(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ),
        tick = null,
        guideline = null,
        valueFormatter = valueFormatter,
        itemPlacer = HorizontalAxis.ItemPlacer.aligned(addExtremeLabelPadding = addExtremeLabelPadding),
    )
}