package com.noahjutz.gymroutines.ui.routines.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.material.placeholder

@ExperimentalMaterialApi
@Composable
fun RoutineListPlaceholder() {
    Column {
        Box(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(percent = 100))
                .placeholder(visible = true)
        )
        repeat(3) {
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
