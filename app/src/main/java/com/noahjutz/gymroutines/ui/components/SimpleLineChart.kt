package com.noahjutz.gymroutines.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp

@Composable
fun SimpleLineChart(
    modifier: Modifier,
    data: List<Pair<Float, Float>>,
    secondaryData: List<Pair<Float, Float>> = emptyList(),
    color: Color = colors.primary,
    secondaryColor: Color = colors.onSurface.copy(alpha = 0.12f),
) {
    check(data.isNotEmpty()) { "data passed to SimpleLineChart must not be empty" }

    Canvas(modifier) {
        val minX = minOf(data.minOf { it.first }, secondaryData.minOf { it.first })
        val maxX = maxOf(data.maxOf { it.first }, secondaryData.maxOf { it.first })
        val minY = minOf(data.minOf { it.second }, secondaryData.minOf { it.second })
        val maxY = maxOf(data.maxOf { it.second }, secondaryData.maxOf { it.second })

        val offsets = data.map { (x, y) ->
            val xAdjusted = ((x - minX) / (maxX - minX)) * size.width
            val yAdjusted = (1 - ((y - minY) / (maxY - minY))) * size.height
            Offset(xAdjusted, yAdjusted)
        }

        val secondaryOffsets = secondaryData.map { (x, y) ->
            val xAdjusted = ((x - minX) / (maxX - minX)) * size.width
            val yAdjusted = (1 - ((y - minY) / (maxY - minY))) * size.height
            Offset(xAdjusted, yAdjusted)
        }

        clipRect {
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

            if (secondaryData.isNotEmpty()) {
                drawPath(
                    path = Path().apply {
                        moveTo(secondaryOffsets.first().x, secondaryOffsets.first().y)
                        for (offset in secondaryOffsets) {
                            lineTo(offset.x, offset.y)
                        }
                    },
                    color = secondaryColor,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
