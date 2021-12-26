package com.noahjutz.gymroutines.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@ExperimentalMaterialApi
@Composable
fun WorkoutBottomSheet(navToWorkoutScreen: () -> Unit) {
    Column(
        modifier = Modifier
            .height(60.dp)
            .clickable(onClick = navToWorkoutScreen)
    ) {
        Row(
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = "Workout in progress",
                style = MaterialTheme.typography.h6
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ExpandLess, null, modifier = Modifier.padding(end = 8.dp))
        }
    }
}
