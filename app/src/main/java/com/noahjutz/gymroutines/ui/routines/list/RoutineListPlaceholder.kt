package com.noahjutz.gymroutines.ui.routines.list

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer

@ExperimentalMaterialApi
@Composable
fun RoutineListPlaceholder() {
    Column {
        repeat(3) {
            ListItem {
                Text(
                    "A".repeat((5..15).random()),
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    )
                )
            }
        }
    }
}