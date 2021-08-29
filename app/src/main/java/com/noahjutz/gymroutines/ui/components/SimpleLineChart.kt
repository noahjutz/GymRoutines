package com.noahjutz.gymroutines.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SimpleLineChart(
    modifier: Modifier,
    data: List<Pair<Float, Float>>,
    color: Color = colors.primary,
) {
    check(data.isNotEmpty()) { "data passed to SimpleLineChart must not be empty" }

    Canvas(modifier) {
        val minX = data.minOf { it.first }
        val maxX = data.maxOf { it.first }
        val minY = data.minOf { it.second }
        val maxY = data.maxOf { it.second }

        val offsets = data.map { (x, y) ->
            val xAdjusted = ((x - minX) / (maxX - minX)) * size.width
            val yAdjusted = (1 - ((y - minY) / (maxY - minY))) * size.height
            Offset(xAdjusted, yAdjusted)
        }

        drawPath(
            path = Path().apply {
                moveTo(offsets.first().x, offsets.first().y)
                for (offset in offsets) {
                    lineTo(offset.x, offset.y)
                }
            },
            color = color,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}