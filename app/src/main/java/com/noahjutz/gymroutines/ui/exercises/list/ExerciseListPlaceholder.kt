package com.noahjutz.gymroutines.ui.exercises.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
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
fun ExerciseListPlaceholder() {
    Column {
        Box(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(percent = 100))
                .placeholder(visible = true)
        )
        repeat(10) {
            ListItem {
                Text(
                    "A".repeat((5..15).random()),
                    modifier = Modifier
                        .placeholder(visible = true)
                )
            }
        }
    }
}
