package com.noahjutz.gymroutines.ui.workout.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

@ExperimentalMaterialApi
@Composable
fun WorkoutInsightsPlaceholder() {
    Column {
        Text(
            "Charts",
            Modifier.padding(horizontal = 30.dp, vertical = 16.dp),
            style = typography.h4
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(30.dp))
                .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer())
        )
        Text(
            "History",
            Modifier.padding(horizontal = 30.dp, vertical = 16.dp),
            style = typography.h4
        )
        repeat(4) {
            ListItem {
                Text(
                    "A".repeat((5..15).random()),
                    Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    )
                )
            }
        }
    }
}